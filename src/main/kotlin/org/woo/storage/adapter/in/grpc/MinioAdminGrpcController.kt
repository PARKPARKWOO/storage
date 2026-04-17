package org.woo.storage.adapter.`in`.grpc

import com.example.grpc.minioadmin.DeleteObjectRequest
import com.example.grpc.minioadmin.MinioAdminServiceGrpcKt
import com.google.protobuf.Empty
import net.devh.boot.grpc.server.service.GrpcService
import org.woo.storage.application.MinioStorageService

@GrpcService
class MinioAdminGrpcController(
    private val minioStorageService: MinioStorageService,
) : MinioAdminServiceGrpcKt.MinioAdminServiceCoroutineImplBase() {
    override suspend fun deleteObject(request: DeleteObjectRequest): Empty {
        minioStorageService.deleteObject(
            bucket = request.bucket,
            objectKey = request.objectKey,
        )
        return Empty.getDefaultInstance()
    }
}
