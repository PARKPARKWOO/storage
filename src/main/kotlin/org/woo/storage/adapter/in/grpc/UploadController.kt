package org.woo.storage.adapter.`in`.grpc

import com.example.grpc.fileupload.FileUploadChunk
import com.example.grpc.fileupload.FileUploadRequest
import com.example.grpc.fileupload.FileUploadResponse
import com.example.grpc.fileupload.FileUploadServiceGrpc.FileUploadServiceImplBase
import io.grpc.stub.StreamObserver
import io.hypersistence.tsid.TSID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import net.devh.boot.grpc.server.service.GrpcService
import org.springframework.beans.factory.annotation.Qualifier
import org.woo.storage.ports.`in`.UploadUseCase
import java.io.ByteArrayOutputStream
import java.util.concurrent.Executor
import java.util.concurrent.atomic.AtomicBoolean

@GrpcService
class UploadController(
    private val uploadUseCase: UploadUseCase,
    @Qualifier("grpcThreadPool")
    private val grpcThreadPool: Executor,
) : FileUploadServiceImplBase() {
    private final val dispatcher = grpcThreadPool.asCoroutineDispatcher()
    val scope = CoroutineScope(dispatcher)

    override fun uploadFileStream(responseObserver: StreamObserver<FileUploadResponse>?): StreamObserver<FileUploadChunk>? {
        return object : StreamObserver<FileUploadChunk> {
            var fileName: String? = null
            val jobs = arrayOf<Job>()
            val fileId = TSID.fast().toLong()
            val metadataSaved = AtomicBoolean(false)
            override fun onNext(request: FileUploadChunk) {
                fileName = request.fileName
                val job = scope.launch {
                    val outputStream = ByteArrayOutputStream()
                    outputStream.write(request.fileData.data.toByteArray())
                    if (metadataSaved.compareAndSet(false, true)) {
                        uploadUseCase.metadata(
                            fineName = fileName ?: "unknown_file",
                            uploadedBy = request.uploadedBy,
                            contentLength = request.contentLength,
                            chunkSize = request.chunkSize,
                            applicationId = request.applicationId,
                            fileId = fileId,
                        )
                    }

                    uploadUseCase.upload(
                        fileData = outputStream.toByteArray(),
                        fileId = fileId,
                        chunkIndex = request.fileData.offset,
                    )
                }
                jobs[request.fileData.offset] = job
            }

            override fun onError(t: Throwable?) {
                //TODO: Upload 롤백 해야함
                println("Error during file upload: ${t?.message}")
            }

            override fun onCompleted() {
                scope.launch {
                    jobs.forEach { it.join() }
                    val response = FileUploadResponse.newBuilder()
                        .setMessage(fileId)
                        .build()
                    responseObserver?.onNext(response)
                    responseObserver?.onCompleted()
                }
            }
        }
    }

    override fun uploadFile(request: FileUploadRequest, responseObserver: StreamObserver<FileUploadResponse>) {
        CoroutineScope(Dispatchers.IO).launch {
            val fileId = TSID.fast().toLong()
            async {
                uploadUseCase.metadata(
                    fineName = request.fileName,
                    uploadedBy = request.uploadedBy,
                    contentLength = request.contentLength,
                    chunkSize = 1,
                    fileId = fileId,
                    applicationId = request.applicationId,
                )
            }.await()
            async {
                uploadUseCase.upload(
                    fileData = request.fileData.toByteArray(),
                    fileId = fileId,
                    chunkIndex = 0,
                )
            }.await()
            val response = FileUploadResponse.newBuilder()
                .setMessage(fileId)
                .build()
            responseObserver.onNext(response)
            responseObserver.onCompleted()
        }
    }
}