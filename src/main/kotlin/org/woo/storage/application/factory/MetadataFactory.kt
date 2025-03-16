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
    // TODO: handler 예외처리
    suspend fun getHandler(fileName: String): MetadataHandlerTemplate {
        val mediaType = extractMediaType(fileName)
        val contentType = extractContentType(mediaType)
        return handlers.find { handler ->
            handler.isApplicable(contentType)
        } ?: throw RuntimeException()
    }

    suspend fun getHandler(contentType: ContentType): MetadataHandlerTemplate =
        handlers.find { handler -> handler.isApplicable(contentType) } ?: throw RuntimeException()

    suspend fun createDto(fileName: String, uploadedBy: String,  chunkSize: Int, contentLength: Long, fileId: Long, applicationId: Long, pageSize: Int): MetadataDto {
        val mediaType = extractMediaType(fileName)
        return when (val contentType = extractContentType(mediaType)) {
            ContentType.IMAGE -> MetadataDto
                .toImage(
                    uploadedBy = uploadedBy,
                    chunkSize = chunkSize,
                    contentType = contentType.name,
                    contentLength = contentLength,
                    fileName = fileName,
                    fileId = fileId,
                    applicationId = applicationId,
                    pageSize = pageSize,
                    mediaType = mediaType.toString()
                )

            ContentType.VIDEO ->  MetadataDto
                .toVideo(
                    uploadedBy = uploadedBy,
                    chunkSize = chunkSize,
                    contentType = contentType.name,
                    contentLength = contentLength,
                    fileName = fileName,
                    fileId = fileId,
                    applicationId = applicationId,
                    pageSize = pageSize,
                    mediaType = mediaType.toString(),
                )
            ContentType.FILE ->  MetadataDto
                .toFile(
                    uploadedBy = uploadedBy,
                    chunkSize = chunkSize,
                    contentType = contentType.name,
                    contentLength = contentLength,
                    fileName = fileName,
                    fileId = fileId,
                    applicationId = applicationId,
                    pageSize = pageSize,
                    mediaType = mediaType.toString(),
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