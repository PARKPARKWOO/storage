package org.woo.storage.application.dto

import java.time.LocalDateTime

data class MetadataDto(
    val fileId: Long,
    val uploadedBy: String,
    val contentLength: Long,
    val contentType: String,
    val chunkSize: Int,
    val applicationId: Long,
    val uploadedAt: LocalDateTime = LocalDateTime.now(),
    val fileName: String,
    val pageSize: Int,
) {
    companion object {
        fun toVideo(
            uploadedBy: String,
            contentLength: Long,
            contentType: String,
            chunkSize: Int,
            fileName: String,
            fileId: Long,
            applicationId: Long,
            pageSize: Int,
        ) = MetadataDto(
            uploadedBy = uploadedBy,
            uploadedAt = LocalDateTime.now(),
            chunkSize = chunkSize,
            fileName = fileName,
            contentLength = contentLength,
            contentType = contentType,
            fileId = fileId,
            applicationId = applicationId,
            pageSize = pageSize,
        )

        fun toImage(
            uploadedBy: String,
            contentLength: Long,
            contentType: String,
            chunkSize: Int,
            fileName: String,
            fileId: Long,
            applicationId: Long,
            pageSize: Int,
        ) = MetadataDto(
            uploadedBy = uploadedBy,
            uploadedAt = LocalDateTime.now(),
            chunkSize = chunkSize,
            fileName = fileName,
            contentLength = contentLength,
            contentType = contentType,
            fileId = fileId,
            applicationId = applicationId,
            pageSize = pageSize,
        )

        fun toFile(
            uploadedBy: String,
            contentLength: Long,
            contentType: String,
            chunkSize: Int,
            fileName: String,
            fileId: Long,
            applicationId: Long,
            pageSize: Int,
        ) = MetadataDto(
            uploadedBy = uploadedBy,
            uploadedAt = LocalDateTime.now(),
            chunkSize = chunkSize,
            fileName = fileName,
            contentLength = contentLength,
            contentType = contentType,
            fileId = fileId,
            applicationId = applicationId,
            pageSize = pageSize,
        )
    }
}
