package org.woo.storage.adapter.`in`.grpc

import com.example.grpc.fileupload.FileUploadChunk
import com.example.grpc.fileupload.FileUploadRequest
import com.example.grpc.fileupload.FileUploadResponse
import com.example.grpc.fileupload.FileUploadServiceGrpcKt
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.hypersistence.tsid.TSID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import net.devh.boot.grpc.server.service.GrpcService
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.ApplicationEventPublisher
import org.woo.apm.log.log
import org.woo.storage.application.event.FileDeleteEvent
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
    private val applicationEventPublisher: ApplicationEventPublisher,
) : FileUploadServiceGrpcKt.FileUploadServiceCoroutineImplBase() {
    companion object {
        const val UNKNOWN_FILE_NAME = "unknown_file"
    }
    private final val dispatcher = grpcThreadPool.asCoroutineDispatcher()
    val scope = CoroutineScope(dispatcher)

    override fun uploadFileStream(requests: Flow<FileUploadChunk>): Flow<FileUploadResponse> = flow {
        var fileName: String? = null
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

            if (metadataSaved.compareAndSet(false, true)) {
                scope.launch(job) {
                    uploadUseCase.metadata(
                        fileOriginName = fileName ?: UNKNOWN_FILE_NAME,
                        uploadedBy = request.uploadedBy,
                        contentLength = request.contentLength,
                        chunkSize = request.chunkSize,
                        applicationId = request.applicationId,
                        fileId = fileId,
                        pageSize = request.pageSize,
                        accessLevel = request.accessLevel,
                    )
                }
            }
        }
        try {
            job.children.forEach { it.join() }
            emit(
                FileUploadResponse.newBuilder()
                    .setMessage(fileId)
                    .build()
            )
        } catch (e: Exception) {
            job.cancelAndJoin()
            val deleteEvent = FileDeleteEvent(fileId = fileId, fileOriginName = fileName ?: UNKNOWN_FILE_NAME)
            applicationEventPublisher.publishEvent(deleteEvent)
            log().warn("Error while uploading file", e)
            val status = Status.INTERNAL
                .withDescription("File upload failed: ${e.message}")
                .withCause(e)
            throw StatusRuntimeException(status)
        }
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
                pageSize = 1,
                accessLevel = 0,
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