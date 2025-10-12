package org.woo.storage.adapter.`in`.grpc

import com.example.grpc.fileupload.GetPresignedDownloadUrlRequest
import com.example.grpc.fileupload.GetPresignedDownloadUrlResponse
import com.example.grpc.fileupload.GetPresignedUploadUrlRequest
import com.example.grpc.fileupload.GetPresignedUploadUrlResponse
import com.example.grpc.fileupload.StorageServiceGrpcKt
import com.example.grpc.fileupload.UploadFileRequest
import com.example.grpc.fileupload.UploadFileResponse
import com.google.protobuf.Timestamp
import io.minio.http.Method
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.devh.boot.grpc.server.service.GrpcService
import org.woo.storage.application.MinioStorageService
import java.time.Instant

@GrpcService
class MinioUploadController(
    private val minioService: MinioStorageService,
) : StorageServiceGrpcKt.StorageServiceCoroutineImplBase() {
    companion object {
        const val DEFAULT_EXPIRY_DELAY = 600
    }
    override suspend fun getPresignedDownloadUrl(request: GetPresignedDownloadUrlRequest): GetPresignedDownloadUrlResponse {
        val expiry = if (request.expirySeconds > 0) request.expirySeconds else DEFAULT_EXPIRY_DELAY
        val expiresAt = getExpiresAt(expiry)
        val qp = mutableMapOf<String, String>()
        if (request.responseContentType.isNotBlank())
            qp["response-content-type"] = request.responseContentType
        if (request.responseContentDisposition.isNotBlank())
            qp["response-content-disposition"] = request.responseContentDisposition
        val url = minioService.getPresignDownloadUrl(
            bucket = request.bucket,
            objectKey = request.objectKey,
            expiry = request.expirySeconds,
            queryParams = qp,
        )
        return GetPresignedDownloadUrlResponse.newBuilder()
            .setUrl(url)
            .setExpiresAt(expiresAt)
            .build()
    }

    override suspend fun getPresignedUploadUrl(request: GetPresignedUploadUrlRequest): GetPresignedUploadUrlResponse {
        val expiry = if (request.expirySeconds > 0) request.expirySeconds else DEFAULT_EXPIRY_DELAY

        val url = minioService.getPresignUploadUrl(
            bucket = request.spec.bucket,
            contentType = request.spec.contentType,
            objectKey = request.spec.objectKey,
            expiry = request.expirySeconds,
        )
        val expiresAt = getExpiresAt(expiry)
        return GetPresignedUploadUrlResponse.newBuilder()
            .setUrl(url)
            .setExpiresAt(expiresAt)
            .build()
    }

    override suspend fun uploadFile(requests: Flow<UploadFileRequest>): UploadFileResponse {
        return minioService.uploadStream(requests)
    }

    private fun getExpiresAt(expiry: Int): Timestamp {
        val expirySeconds = (if (expiry > 0) expiry else DEFAULT_EXPIRY_DELAY).coerceAtMost(7 * 24 * 3600)
        return Timestamp.newBuilder()
            .setSeconds(Instant.now().plusSeconds(expirySeconds.toLong()).epochSecond)
            .build()
    }
}