package org.woo.storage.adapter.`in`.grpc

import com.example.grpc.fileupload.FileUploadChunk
import com.example.grpc.fileupload.FileUploadRequest
import com.example.grpc.fileupload.FileUploadResponse
import com.example.grpc.fileupload.FileUploadServiceGrpcKt
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.hypersistence.tsid.TSID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import net.devh.boot.grpc.server.service.GrpcService
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.ApplicationEventPublisher
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.woo.apm.log.log
import org.woo.storage.application.event.FileDeleteEvent
import org.woo.storage.ports.`in`.UploadUseCase
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean

@GrpcService
class UploadController(
    private val uploadUseCase: UploadUseCase,
    @Qualifier("grpcThreadPool")
    private val grpcThreadPool: ThreadPoolTaskExecutor,
    private val applicationEventPublisher: ApplicationEventPublisher,
) : FileUploadServiceGrpcKt.FileUploadServiceCoroutineImplBase() {
    companion object {
        const val UNKNOWN_FILE_NAME = "unknown_file"
    }

    private final val dispatcher = grpcThreadPool.asCoroutineDispatcher()
    val scope = CoroutineScope(dispatcher)

    override fun uploadFileStream(requests: Flow<FileUploadChunk>): Flow<FileUploadResponse> = flow {
        val fileId = TSID.fast().toLong()
        var fileNameForCleanup: String? = null
        try {
            coroutineScope {
                val metadataSaved = AtomicBoolean(false)
                var dataReceived = false

                requests.collect { request ->
                    dataReceived = true
                    // 오류 발생 시 사용할 수 있도록 마지막으로 확인된 파일 이름 업데이트
                    fileNameForCleanup = request.fileName

                    if (metadataSaved.compareAndSet(false, true)) {
                        val fileNameForMetadata = fileNameForCleanup ?: UNKNOWN_FILE_NAME
                        launch {
                            uploadUseCase.metadata(
                                fileOriginName = fileNameForMetadata,
                                uploadedBy = request.uploadedBy,
                                contentLength = request.contentLength,
                                chunkSize = request.chunkSize,
                                applicationId = request.applicationId,
                                fileId = fileId,
                                pageSize = request.pageSize,
                                accessLevel = request.accessLevel
                            )
                        }
                    }

                    launch { // 현재 coroutineScope의 자식으로 코루틴 실행
                        val byteBuffer = request.fileData.data.asReadOnlyByteBuffer()
                        uploadUseCase.file(
                            fileData = byteBuffer,
                            fileId = fileId,
                            chunkIndex = request.fileData.offset
                        )
                    }
                }

                // collect가 끝난 후, 데이터가 전혀 수신되지 않았다면 오류 처리
                if (!dataReceived) {
                    throw StatusRuntimeException(Status.INVALID_ARGUMENT.withDescription("File upload stream was empty."))
                }
            } // coroutineScope 블록이 끝나면 모든 launch 작업이 완료됨이 보장됨

            // coroutineScope가 성공적으로 완료되면 성공 응답 emit
            emit(
                FileUploadResponse.newBuilder()
                    .setMessage(fileId)
                    .build()
            )
        } catch (e: Exception) {
            // 이미 gRPC 예외인 경우는 다시 래핑하지 않음
            if (e is StatusRuntimeException) {
                throw e
            }
            val deleteEvent = FileDeleteEvent(fileId = fileId)
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
            val fileName = request.fileName
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
}