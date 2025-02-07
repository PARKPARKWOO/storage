package org.woo.storage.presentation

import com.example.grpc.fileupload.FileUploadChunk
import com.example.grpc.fileupload.FileUploadProto
import com.example.grpc.fileupload.FileUploadRequest
import com.example.grpc.fileupload.FileUploadResponse
import com.example.grpc.fileupload.FileUploadServiceGrpc.FileUploadServiceImplBase
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.devh.boot.grpc.server.service.GrpcService
import org.woo.storage.UploadService

@GrpcService
class UploadController(
    private val uploadService: UploadService,
) : FileUploadServiceImplBase() {
    override fun uploadFileStream(responseObserver: StreamObserver<FileUploadResponse>): StreamObserver<FileUploadChunk>? {
        CoroutineScope(Dispatchers.IO).launch {
            responseObserver.onNext(FileUploadResponse.newBuilder().setMessage("dd").build())
            responseObserver.onCompleted()
        }
        TODO()
    }

    override fun uploadFile(request: FileUploadRequest, responseObserver: StreamObserver<FileUploadResponse>) {
        CoroutineScope(Dispatchers.IO).launch {
            val id = uploadService.storeFile(
                fileName = request.fileName,
                fileBytes = request.fileData.toByteArray()
            )
            val response = FileUploadResponse.newBuilder()
                .setMessage(id)
                .build()
            responseObserver.onNext(response)
            responseObserver.onCompleted()

        }
    }
}