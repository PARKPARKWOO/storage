package org.woo.storage.application.dto

import java.time.LocalDateTime

data class MetadataDto(
    val fileId: Long,
    val uploadedBy: String,
    val contentLength: Long,
    val contentType: String,
    val chunkSize: Int,
    val uploadedAt: LocalDateTime = LocalDateTime.now(),
    val fileOriginName: String,
) {
    companion object {
        fun toVideo(
            uploadedBy: String,
            contentLength: Long,
            contentType: String,
            chunkSize: Int,
            fileOriginName: String,
            fileId: Long,
        ) = MetadataDto(
            uploadedBy = uploadedBy,
            uploadedAt = LocalDateTime.now(),
            chunkSize = chunkSize,
            fileOriginName = fileOriginName,
            contentLength = contentLength,
            contentType = contentType,
            fileId = fileId,
        )

        fun toImage(
            uploadedBy: String,
            contentLength: Long,
            contentType: String,
            chunkSize: Int,
            fileOriginName: String,
            fileId: Long,
        ) = MetadataDto(
            uploadedBy = uploadedBy,
            uploadedAt = LocalDateTime.now(),
            chunkSize = chunkSize,
            fileOriginName = fileOriginName,
            contentLength = contentLength,
            contentType = contentType,
            fileId = fileId,
        )

        fun toFile(
            uploadedBy: String,
            contentLength: Long,
            contentType: String,
            chunkSize: Int,
            fileOriginName: String,
            fileId: Long,
        ) = MetadataDto(
            uploadedBy = uploadedBy,
            uploadedAt = LocalDateTime.now(),
            chunkSize = chunkSize,
            fileOriginName = fileOriginName,
            contentLength = contentLength,
            contentType = contentType,
            fileId = fileId,
        )
    }
}
