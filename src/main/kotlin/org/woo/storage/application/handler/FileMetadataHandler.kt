package org.woo.storage.application.handler

import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.stereotype.Component
import org.woo.storage.adapter.out.persistence.mysql.FileMetadataRepository
import org.woo.storage.adapter.out.persistence.mysql.MetadataTypeRepository
import org.woo.storage.application.dto.MetadataDto
import org.woo.storage.domain.metadata.ContentType
import org.woo.storage.domain.metadata.FileMetadata
import org.woo.storage.domain.metadata.Metadata

@Component
class FileMetadataHandler(
    private val fileMetadataRepository: FileMetadataRepository,
    private val metadataTypeRepository: MetadataTypeRepository,
): MetadataHandlerTemplate(metadataTypeRepository) {
    override suspend fun saveMetadata(dto: MetadataDto) {
        val fileMetadata = FileMetadata.from(dto)
        fileMetadataRepository.save(fileMetadata).awaitSingle()
    }

    override suspend fun isApplicable(contentType: ContentType): Boolean = contentType == ContentType.FILE
    override suspend fun getMetadata(id: Long): Metadata = fileMetadataRepository.findById(id).awaitSingle()
}