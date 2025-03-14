package org.woo.storage.adapter.`in`.grpc

import com.example.grpc.fileupload.FileUploadChunk
import com.example.grpc.fileupload.FileUploadRequest
import com.example.grpc.fileupload.FileUploadResponse
import com.example.grpc.fileupload.FileUploadServiceGrpc.FileUploadServiceImplBase
import com.example.grpc.fileupload.FileUploadServiceGrpcKt
import io.grpc.stub.StreamObserver
import io.hypersistence.tsid.TSID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import net.devh.boot.grpc.server.service.GrpcService
import org.springframework.beans.factory.annotation.Qualifier
import org.woo.storage.ports.`in`.UploadUseCase
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.util.concurrent.Executor
import java.util.concurrent.atomic.AtomicBoolean

@GrpcService
class UploadController(
    private val uploadUseCase: UploadUseCase,
    @Qualifier("grpcThreadPool")
    private val grpcThreadPool: Executor,
) : FileUploadServiceGrpcKt.FileUploadServiceCoroutineImplBase() {
    private final val dispatcher = grpcThreadPool.asCoroutineDispatcher()
    val scope = CoroutineScope(dispatcher)

    override fun uploadFileStream(requests: Flow<FileUploadChunk>): Flow<FileUploadResponse> = flow {
        val jobs = mutableListOf<Job>()
        var fileName: String? = null
        val fileId = TSID.fast().toLong()
        val metadataSaved = AtomicBoolean(false)

        // 들어오는 각 청크를 처리
        requests.collect { request ->
            fileName = request.fileName
            // 청크 저장 작업: ByteBuffer로 변환 후 저장
            val job = scope.launch {
                val byteBuffer = ByteBuffer.wrap(request.fileData.data.toByteArray())
                uploadUseCase.file(
                    fileData = byteBuffer,
                    fileId = fileId,
                    chunkIndex = request.fileData.offset
                )
            }
            jobs.add(job)

            // 메타데이터는 한 번만 저장 (첫 청크에서)
            if (metadataSaved.compareAndSet(false, true)) {
                scope.launch {
                    uploadUseCase.metadata(
                        fileOriginName = fileName ?: "unknown_file",
                        uploadedBy = request.uploadedBy,
                        contentLength = request.contentLength,
                        chunkSize = request.chunkSize,
                        applicationId = request.applicationId,
                        fileId = fileId,
                        pageSize = request.pageSize
                    )
                }
            }
        }

        // 모든 청크 저장 작업이 완료될 때까지 대기
        jobs.forEach { it.join() }
        // 최종 응답 메시지 emit
        emit(
            FileUploadResponse.newBuilder()
                .setMessage(fileId)
                .build()
        )
    }

    override suspend fun uploadFile(request: FileUploadRequest): FileUploadResponse {
        val fileId = TSID.fast().toLong()
        val metadataJob = scope.async {
            uploadUseCase.metadata(
                fileOriginName = request.fileName,
                uploadedBy = request.uploadedBy,
                contentLength = request.contentLength,
                chunkSize = request.contentLength.toInt(),
                applicationId = request.applicationId,
                fileId = fileId,
                pageSize = 1
            )
        }
        val fileJob = scope.async {
            val byteBuffer = ByteBuffer.wrap(request.fileData.toByteArray())
            uploadUseCase.file(
                fileData = byteBuffer,
                fileId = fileId,
                chunkIndex = 0
            )
        }
        awaitAll(metadataJob, fileJob)
        return FileUploadResponse.newBuilder()
            .setMessage(fileId)
            .build()
    }
}