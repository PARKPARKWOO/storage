package org.woo.storage.adapter.`in`.grpc

import com.example.grpc.fileupload.FileUploadChunk
import com.example.grpc.fileupload.FileUploadRequest
import com.example.grpc.fileupload.FileUploadResponse
import com.example.grpc.fileupload.FileUploadServiceGrpcKt
import com.fasterxml.jackson.databind.util.ArrayBuilders.ByteBuilder
import io.hypersistence.tsid.TSID
import jdk.internal.misc.VM
import kotlinx.coroutines.CoroutineScope
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
import java.net.URLEncoder
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
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
        var fileName: String?
        val fileId = TSID.fast().toLong()
        val metadataSaved = AtomicBoolean(false)
        val job = Job()
        requests.collect { request ->
            fileName = encodeFilename(request.fileName)
            scope.launch(job) {
                val byteBuffer = request.fileData.data.asReadOnlyByteBuffer()
                uploadUseCase.file(
                    fileData = byteBuffer,
                    fileId = fileId,
                    chunkIndex = request.fileData.offset
                )
            }

            // 메타데이터는 한 번만 저장 (첫 청크에서)
            if (metadataSaved.compareAndSet(false, true)) {
                scope.launch(job) {
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
        job.children.forEach { it.join() }

        emit(
            FileUploadResponse.newBuilder()
                .setMessage(fileId)
                .build()
        )
    }

    override suspend fun uploadFile(request: FileUploadRequest): FileUploadResponse {
        val fileId = TSID.fast().toLong()
        val metadataJob = scope.async {
            val fileName = encodeFilename(request.fileName)
            uploadUseCase.metadata(
                fileOriginName = fileName,
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

    fun encodeFilename(filename: String): String {
        return URLEncoder.encode(filename, StandardCharsets.UTF_8.toString())
    }
}