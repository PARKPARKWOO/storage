package org.woo.storage.application

import io.minio.GetPresignedObjectUrlArgs
import io.minio.MinioAsyncClient
import io.minio.MinioClient
import io.minio.http.Method
import org.springframework.stereotype.Service
import org.woo.storage.ports.`in`.UploadUseCase
import java.nio.ByteBuffer

@Service
class MinioStorageService(
    private val minioClient: MinioClient,
    private val asyncMinioClient: MinioAsyncClient,
) : UploadUseCase {
    suspend fun getPresignUploadUrl(
        bucket: String,
        objectKey: String,
        contentType: String,
        expiry: Int,
        queryParams: Map<String, String> = emptyMap(),
    ): String {
        val args = GetPresignedObjectUrlArgs.builder()
            .method(Method.PUT)
            .bucket(bucket)
            .expiry(expiry)
            .`object`(objectKey)
            .extraQueryParams(queryParams)
            .build()
        return asyncMinioClient.getPresignedObjectUrl(args)
    }

    suspend fun getPresignDownloadUrl(
        bucket: String,
        objectKey: String,
        expiry: Int,
        queryParams: Map<String, String>,
    ): String {
        val args = GetPresignedObjectUrlArgs.builder()
            .method(Method.GET)
            .bucket(bucket)
            .expiry(expiry)
            .`object`(objectKey)
            .extraQueryParams(queryParams)
            .build()
        return asyncMinioClient.getPresignedObjectUrl(args)
    }

    override suspend fun file(fileData: ByteBuffer, fileId: Long, chunkIndex: Int) {
        TODO("Not yet implemented")
    }

    override suspend fun metadata(
        fileOriginName: String,
        uploadedBy: String,
        chunkSize: Int,
        contentLength: Long,
        applicationId: String,
        fileId: Long,
        pageSize: Int,
        accessLevel: Int
    ) {
        TODO("Not yet implemented")
    }
}