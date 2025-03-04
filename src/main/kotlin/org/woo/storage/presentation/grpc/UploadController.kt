package org.woo.storage.presentation.grpc

import com.example.grpc.fileupload.FileUploadChunk
import com.example.grpc.fileupload.FileUploadRequest
import com.example.grpc.fileupload.FileUploadResponse
import com.example.grpc.fileupload.FileUploadServiceGrpc.FileUploadServiceImplBase
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.devh.boot.grpc.server.service.GrpcService
import org.woo.storage.business.UploadFacade

@GrpcService
class UploadController(
    private val uploadFacade: UploadFacade,
) : FileUploadServiceImplBase() {
    override fun uploadFileStream(responseObserver: StreamObserver<FileUploadResponse>): StreamObserver<FileUploadChunk>? {
        CoroutineScope(Dispatchers.IO).launch {}
        TODO("stream 처리가 필요하다면 추가한다")
    }

    override fun uploadFile(request: FileUploadRequest, responseObserver: StreamObserver<FileUploadResponse>) {
        CoroutineScope(Dispatchers.IO).launch {
            val id = uploadFacade.storeFile(
                fileName = request.fileName,
                fileBytes = request.fileData.toByteArray()
            )
            val response = FileUploadResponse.newBuilder()
                .setMessage(id.toString())
                .build()
            responseObserver.onNext(response)
            responseObserver.onCompleted()
        }
    }
}