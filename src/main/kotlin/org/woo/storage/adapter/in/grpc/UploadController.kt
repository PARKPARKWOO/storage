package org.woo.storage.adapter.`in`.grpc

import com.example.grpc.fileupload.FileUploadChunk
import com.example.grpc.fileupload.FileUploadRequest
import com.example.grpc.fileupload.FileUploadResponse
import com.example.grpc.fileupload.FileUploadServiceGrpc.FileUploadServiceImplBase
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.devh.boot.grpc.server.service.GrpcService
import org.springframework.beans.factory.annotation.Qualifier
import org.woo.storage.ports.`in`.UploadUseCase
import java.io.ByteArrayOutputStream
import java.util.concurrent.Executor

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
            val outputStream = ByteArrayOutputStream()
            var fileName: String = ""
            override fun onNext(request: FileUploadChunk?) {
                request ?: return
                // 각 청크의 데이터를 누적 (필요에 따라 chunkIndex, isLastChunk 등으로 분기 처리)
                outputStream.write(request.chunkData.toByteArray())

                scope.launch {
                    uploadUseCase.upload(outputStream.toByteArray())
                }
            }

            override fun onError(t: Throwable?) {
                println("Error during file upload: ${t?.message}")
            }

            override fun onCompleted() {
                // 모든 청크 수신 완료 후, 파일을 저장하는 로직 구현
                // 예시: 파일 저장 후 응답 전송
                val id = scope.async {
                    uploadUseCase.metadata(fileName)
                }
                val response = FileUploadResponse.newBuilder()
                    .setMessage(id)
                    .build()
                responseObserver?.onNext(response)
                responseObserver?.onCompleted()
            }
        }
    }
    override fun uploadFile(request: FileUploadRequest, responseObserver: StreamObserver<FileUploadResponse>) {
        CoroutineScope(Dispatchers.IO).launch {
            val id = async {
                uploadUseCase.upload(
                    fileData = request.fileData.toByteArray(),
                )
            }.await()
            val metadata = async {
                uploadUseCase.metadata(fineName = request.fileName)
            }
            val response = FileUploadResponse.newBuilder()
                .setMessage(id.toString())
                .build()
            responseObserver.onNext(response)
            responseObserver.onCompleted()
        }
    }
}