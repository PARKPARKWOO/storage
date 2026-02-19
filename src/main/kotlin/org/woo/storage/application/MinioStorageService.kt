package org.woo.storage.application

import com.example.grpc.fileupload.UploadFileRequest
import com.example.grpc.fileupload.UploadFileResponse
import io.minio.GetObjectArgs
import io.minio.GetPresignedObjectUrlArgs
import io.minio.MinioAsyncClient
import io.minio.MinioClient
import io.minio.PutObjectArgs
import io.minio.RemoveObjectArgs
import io.minio.http.Method
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.fold
import org.springframework.stereotype.Service
import org.woo.storage.ports.`in`.UploadUseCase
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.nio.ByteBuffer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.coroutineScope
import org.springframework.beans.factory.annotation.Qualifier
import org.woo.storage.ports.`in`.DeleteUseCase

@Service
class MinioStorageService(
    @Qualifier("minioClient")
    private val minioClient: MinioClient,
    @Qualifier("minioAsyncClient")
    private val asyncMinioClient: MinioAsyncClient,
) : UploadUseCase {
    suspend fun getPresignUploadUrl(
        bucket: String,
        objectKey: String,
        contentType: String,
        expiry: Int,
        queryParams: Map<String, String> = emptyMap(),
    ): String {
        val args = GetPresignedObjectUrlArgs.builder()
            .method(Method.PUT)
            .bucket(bucket)
            .expiry(expiry)
            .`object`(objectKey)
            .extraQueryParams(queryParams)
            .build()
        return asyncMinioClient.getPresignedObjectUrl(args)
    }

    suspend fun getPresignDownloadUrl(
        bucket: String,
        objectKey: String,
        expiry: Int,
        queryParams: Map<String, String>,
    ): String {
        val args = GetPresignedObjectUrlArgs.builder()
            .method(Method.GET)
            .bucket(bucket)
            .expiry(expiry)
            .`object`(objectKey)
            .extraQueryParams(queryParams)
            .build()
        return asyncMinioClient.getPresignedObjectUrl(args)
    }

    suspend fun upload(bucket: String, objectKey: String, contentType: String, data: InputStream) {
        minioClient.putObject(
            PutObjectArgs.builder()
                .bucket(bucket)
                .`object`(objectKey)
                .contentType(contentType ?: "application/octet-stream")
                .stream(data.buffered(), -1, 5 * 1024 * 1024)
                .build()
        )
    }

    override suspend fun file(fileData: ByteBuffer, fileId: Long, chunkIndex: Int) {

    }

    override suspend fun metadata(
        fileOriginName: String,
        uploadedBy: String,
        chunkSize: Int,
        contentLength: Long,
        applicationId: String,
        fileId: Long,
        pageSize: Int,
        accessLevel: Int
    ) {
        TODO("Not yet implemented")
    }

    /**
     * gRPC 스트리밍을 통해 파일을 받아서 MinIO에 업로드합니다.
     * 
     * 프로토콜:
     * 1. 첫 번째 메시지는 FileUploadSpec (헤더 정보)
     * 2. 이후 메시지들은 파일 데이터 청크들
     * 
     * @param requests gRPC 스트리밍 요청 Flow
     * @return 업로드 결과 (버킷, 객체키, 크기, ETag)
     */
    suspend fun uploadStream(requests: Flow<UploadFileRequest>): UploadFileResponse {
        // 첫 번째 메시지에서 헤더 정보 추출
        var bucket = ""
        var objectKey = ""
        var contentType = ""
        var metadata = mutableMapOf<String, String>()
        var totalSize = 0L
        var isFirstMessage = true
        
        // 데이터를 버퍼에 모음
        val dataBuffer = mutableListOf<ByteArray>()
        
        requests.collect { request ->
            when {
                request.hasHeader() -> {
                    if (!isFirstMessage) {
                        throw IllegalStateException("Header must be the first message")
                    }
                    val spec = request.header
                    bucket = spec.bucket
                    objectKey = spec.objectKey
                    contentType = spec.contentType.ifBlank { "application/octet-stream" }
                    metadata = spec.metadataMap.toMutableMap()
                    isFirstMessage = false
                }
                request.hasChunk() -> {
                    if (isFirstMessage) {
                        throw IllegalStateException("First message must be header")
                    }
                    val chunkData = request.chunk.toByteArray()
                    dataBuffer.add(chunkData)
                    totalSize += chunkData.size
                }
                else -> {
                    throw IllegalStateException("Unknown request type")
                }
            }
        }
        
        if (bucket.isEmpty() || objectKey.isEmpty()) {
            throw IllegalArgumentException("Bucket and objectKey must be provided in header")
        }
        
        // 모든 청크를 하나의 바이트 배열로 합침
        val completeData = ByteArray(totalSize.toInt())
        var offset = 0
        for (chunk in dataBuffer) {
            System.arraycopy(chunk, 0, completeData, offset, chunk.size)
            offset += chunk.size
        }
        
        // MinIO에 업로드
        val inputStream = ByteArrayInputStream(completeData)
        val putArgs = PutObjectArgs.builder()
            .bucket(bucket)
            .`object`(objectKey)
            .contentType(contentType)
            .stream(inputStream, totalSize, -1)
            .apply {
                if (metadata.isNotEmpty()) {
                    userMetadata(metadata)
                }
            }
            .build()
        val result = minioClient.putObject(putArgs)
        return UploadFileResponse.newBuilder()
            .setBucket(bucket)
            .setObjectKey(objectKey)
            .setSize(totalSize)
            .setEtag(result.etag())
            .build()
    }

    /**
     * PipedStream을 사용한 대용량 파일 스트리밍 업로드 (메모리 효율적)
     * 
     * 참고: 이 방식은 전체 파일을 메모리에 로드하지 않고 스트리밍 방식으로 처리합니다.
     */
    suspend fun uploadStreamEfficient(requests: Flow<UploadFileRequest>): UploadFileResponse = coroutineScope {
        var bucket = ""
        var objectKey = ""
        var contentType = ""
        var metadata = mutableMapOf<String, String>()
        var totalSize = 0L
        
        val pipedOutputStream = PipedOutputStream()
        val pipedInputStream = PipedInputStream(pipedOutputStream, 8192 * 1024) // 8MB buffer
        
        // 백그라운드에서 MinIO 업로드 작업 시작
        val uploadJob = launch(Dispatchers.IO) {
            var isHeaderProcessed = false
            
            try {
                requests.collect { request ->
                    when {
                        request.hasHeader() -> {
                            if (isHeaderProcessed) {
                                throw IllegalStateException("Header already processed")
                            }
                            val spec = request.header
                            bucket = spec.bucket
                            objectKey = spec.objectKey
                            contentType = spec.contentType.ifBlank { "application/octet-stream" }
                            metadata = spec.metadataMap.toMutableMap()
                            isHeaderProcessed = true
                        }
                        request.hasChunk() -> {
                            if (!isHeaderProcessed) {
                                throw IllegalStateException("Header must be first")
                            }
                            val chunkData = request.chunk.toByteArray()
                            pipedOutputStream.write(chunkData)
                            totalSize += chunkData.size
                        }
                    }
                }
            } finally {
                pipedOutputStream.close()
            }
        }
        
        // MinIO 업로드를 별도 코루틴에서 실행
        val minioJob = launch(Dispatchers.IO) {
            // 헤더가 설정될 때까지 대기
            while (bucket.isEmpty()) {
                kotlinx.coroutines.delay(10)
            }
            
            val putArgs = PutObjectArgs.builder()
                .bucket(bucket)
                .`object`(objectKey)
                .contentType(contentType)
                .stream(pipedInputStream, -1, 10 * 1024 * 1024) // 10MB part size
                .apply {
                    if (metadata.isNotEmpty()) {
                        userMetadata(metadata)
                    }
                }
                .build()
            
            minioClient.putObject(putArgs)
        }
        
        // 두 작업이 모두 완료될 때까지 대기
        uploadJob.join()
        minioJob.join()
        
        UploadFileResponse.newBuilder()
            .setBucket(bucket)
            .setObjectKey(objectKey)
            .setSize(totalSize)
            .setEtag("")
            .build()
    }
}