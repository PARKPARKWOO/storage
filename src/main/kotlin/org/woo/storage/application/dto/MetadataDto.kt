package org.woo.storage.application.dto

import java.time.LocalDateTime

data class MetadataDto(
    val fileId: Long,
    val uploadedBy: String,
    val contentLength: Long,
    val contentType: String,
    val mediaType: String,
    val chunkSize: Int,
    val applicationId: String,
    val uploadedAt: LocalDateTime = LocalDateTime.now(),
    val fileName: String,
    val pageSize: Int,
    val accessLevel: Int,
) {
    companion object {
        fun toVideo(
            uploadedBy: String,
            contentLength: Long,
            contentType: String,
            chunkSize: Int,
            fileName: String,
            fileId: Long,
            applicationId: String,
            pageSize: Int,
            mediaType: String,
            accessLevel: Int,
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
            mediaType = mediaType,
            accessLevel = accessLevel,
        )

        fun toImage(
            uploadedBy: String,
            contentLength: Long,
            contentType: String,
            chunkSize: Int,
            fileName: String,
            fileId: Long,
            applicationId: String,
            pageSize: Int,
            mediaType: String,
            accessLevel: Int
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
            mediaType = mediaType,
            accessLevel = accessLevel,
        )

        fun toFile(
            uploadedBy: String,
            contentLength: Long,
            contentType: String,
            chunkSize: Int,
            fileName: String,
            fileId: Long,
            applicationId: String,
            pageSize: Int,
            mediaType: String,
            accessLevel: Int
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
            mediaType = mediaType,
            accessLevel = accessLevel,
        )
    }
}
