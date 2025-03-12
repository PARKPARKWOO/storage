package org.woo.storage.application.factory

import org.springframework.http.MediaType
import org.springframework.http.MediaTypeFactory
import org.springframework.stereotype.Component
import org.woo.storage.application.dto.MetadataDto
import org.woo.storage.application.handler.MetadataHandlerTemplate
import org.woo.storage.domain.metadata.ContentType

@Component
class MetadataFactory(
    private val handlers: List<MetadataHandlerTemplate>,
) {
    suspend fun getHandler(fileName: String): MetadataHandlerTemplate {
        val mediaType = extractMediaType(fileName)
        val contentType = extractContentType(mediaType)
        return handlers.find { }
    }

    suspend fun createDto(fileName: String, uploadedBy: String, chunkSize: Int, contentLength: Long, fileId: Long): MetadataDto {
        val mediaType = extractMediaType(fileName)
        val contentType = extractContentType(mediaType)
        return when (contentType) {
            ContentType.IMAGE -> MetadataDto
                .toImage(
                    uploadedBy = uploadedBy,
                    chunkSize = chunkSize,
                    contentType = mediaType.toString(),
                    contentLength = contentLength,
                    fileOriginName = fileName,
                    fileId = fileId,
                )

            ContentType.VIDEO ->  MetadataDto
                .toVideo(
                    uploadedBy = uploadedBy,
                    chunkSize = chunkSize,
                    contentType = mediaType.toString(),
                    contentLength = contentLength,
                    fileOriginName = fileName,
                    fileId = fileId,
                )
            ContentType.FILE ->  MetadataDto
                .toFile(
                    uploadedBy = uploadedBy,
                    chunkSize = chunkSize,
                    contentType = mediaType.toString(),
                    contentLength = contentLength,
                    fileOriginName = fileName,
                    fileId = fileId,
                )
        }
    }

    private suspend fun extractMediaType(fileName: String): MediaType =
        MediaTypeFactory.getMediaType(fileName)
            .orElse(MediaType.APPLICATION_OCTET_STREAM)

    private suspend fun extractContentType(mediaType: MediaType): ContentType = when {
        mediaType.type.equals("image", ignoreCase = true) -> ContentType.IMAGE
        mediaType.type.equals("video", ignoreCase = true) -> ContentType.VIDEO
        else -> ContentType.FILE
    }
}