package org.woo.storage.adapter.`in`.grpc

import com.example.grpc.filedelete.DeleteFileRequest
import com.example.grpc.filedelete.FileDeleteServiceGrpcKt
import com.google.protobuf.Empty
import net.devh.boot.grpc.server.service.GrpcService
import org.springframework.context.ApplicationEventPublisher
import org.woo.storage.application.event.FileDeleteEvent

@GrpcService
class DeleteGrpcController(
    private val eventPublisher: ApplicationEventPublisher,
): FileDeleteServiceGrpcKt.FileDeleteServiceCoroutineImplBase() {
    override suspend fun delete(request: DeleteFileRequest): Empty {
        val fileDeleteEvent = FileDeleteEvent(request.id)
        eventPublisher.publishEvent(fileDeleteEvent)
        return Empty.getDefaultInstance()
    }
}