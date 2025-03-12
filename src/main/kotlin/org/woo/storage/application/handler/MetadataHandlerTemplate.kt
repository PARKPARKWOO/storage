package org.woo.storage.application.handler

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.http.MediaType
import org.springframework.http.MediaTypeFactory
import org.woo.storage.adapter.out.persistence.mysql.MetadataTypeRepository
import org.woo.storage.application.dto.MetadataDto
import org.woo.storage.domain.metadata.MetadataType

abstract class MetadataHandlerTemplate(
    private val metadataTypeRepository: MetadataTypeRepository,
) {
    protected abstract suspend fun saveMetadata(dto: MetadataDto)

    private suspend fun saveMetadataType(dto: MetadataDto) {
        val metadataType = MetadataType(dto.fileId, dto.contentType)
        metadataTypeRepository.save(metadataType).awaitSingle()
    }

    protected suspend fun get() {

    }

    suspend fun save(dto: MetadataDto): Long = coroutineScope {
        saveMetadataType(dto)
        saveMetadata(dto)
        1L
    }
}