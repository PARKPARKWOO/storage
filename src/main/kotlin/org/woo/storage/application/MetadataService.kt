package org.woo.storage.application

import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.http.MediaType
import org.springframework.http.MediaTypeFactory
import org.springframework.stereotype.Service
import org.woo.storage.domain.ContentType
import org.woo.storage.adapter.out.persistence.mysql.FileMetadataRepository
import org.woo.storage.adapter.out.persistence.mysql.ImageMetadataRepository
import org.woo.storage.adapter.out.persistence.mysql.MetadataTypeRepository
import org.woo.storage.adapter.out.persistence.mysql.VideoMetadataRepository
import org.woo.storage.domain.metadata.Metadata

@Service
class MetadataService(
    private val metadataTypeRepository: MetadataTypeRepository,
    private val imageMetadataRepository: ImageMetadataRepository,
    private val videoMetadataRepository: VideoMetadataRepository,
    private val fileMetadataRepository: FileMetadataRepository,
) {
    suspend fun getMetadata(resourceId: String): Metadata {
        val metadataType = metadataTypeRepository.findByResourceId(resourceId).awaitSingle()
        val type = ContentType.valueOf(metadataType!!.type)
        return when (type) {
            ContentType.VIDEO -> {
                videoMetadataRepository.findByResourceId(resourceId).awaitSingle()!!
            }

            ContentType.FILE -> {
                fileMetadataRepository.findByResourceId(resourceId).awaitSingle()!!
            }
            ContentType.IMAGE -> {
                imageMetadataRepository.findByResourceId(resourceId).awaitSingle()!!
            }
        }
    }

    suspend fun save(fileName: String) {
        val extractContentType = extractContentType(fileName)

    }

    private suspend fun extractContentType(fileName: String): MediaType =
        MediaTypeFactory.getMediaType(fileName)
            .orElse(MediaType.APPLICATION_OCTET_STREAM)
}