package org.woo.storage.application.handler

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.stereotype.Component
import org.woo.storage.adapter.out.persistence.mysql.FileMetadataRepository
import org.woo.storage.adapter.out.persistence.mysql.ImageMetadataRepository
import org.woo.storage.adapter.out.persistence.mysql.MetadataTypeRepository
import org.woo.storage.application.dto.MetadataDto
import org.woo.storage.domain.metadata.ContentType
import org.woo.storage.domain.metadata.FileMetadata
import org.woo.storage.domain.metadata.ImageMetadata
import org.woo.storage.domain.metadata.Metadata

@Component
class ImageMetadataHandler(
    private val metadataTypeRepository: MetadataTypeRepository,
    private val imageMetadataRepository: ImageMetadataRepository,
): MetadataHandlerTemplate(metadataTypeRepository) {
    override suspend fun saveMetadata(dto: MetadataDto) {
        val imageMetadata = ImageMetadata.from(dto)
        imageMetadataRepository.save(imageMetadata).awaitSingle()
    }

    override suspend fun isApplicable(contentType: ContentType): Boolean = contentType == ContentType.IMAGE

    override suspend fun getMetadata(id: Long): Metadata {
        return imageMetadataRepository.findById(id).awaitSingle()
    }
}