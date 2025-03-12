package org.woo.storage.application.handler

import org.springframework.stereotype.Component
import org.woo.storage.adapter.out.persistence.mysql.FileMetadataRepository
import org.woo.storage.adapter.out.persistence.mysql.MetadataTypeRepository
import org.woo.storage.application.dto.MetadataDto

@Component
class FileMetadataHandler(
    private val fileMetadataRepository: FileMetadataRepository,
    private val metadataTypeRepository: MetadataTypeRepository,
): MetadataHandlerTemplate(metadataTypeRepository) {
    override suspend fun saveMetadata(dto: MetadataDto) {
        TODO("Not yet implemented")
    }
}