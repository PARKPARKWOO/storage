package org.woo.storage.application.handler

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.awaitSingle
import org.flywaydb.core.experimental.MetaData
import org.springframework.http.MediaType
import org.springframework.http.MediaTypeFactory
import org.woo.storage.adapter.out.persistence.mysql.MetadataTypeRepository
import org.woo.storage.application.dto.MetadataDto
import org.woo.storage.domain.metadata.ContentType
import org.woo.storage.domain.metadata.Metadata
import org.woo.storage.domain.metadata.MetadataType

abstract class MetadataHandlerTemplate(
    private val metadataTypeRepository: MetadataTypeRepository,
) {
    protected abstract suspend fun saveMetadata(dto: MetadataDto)

    abstract suspend fun isApplicable(contentType: ContentType): Boolean

    protected abstract suspend fun getMetadata(id: Long): Metadata

    protected abstract suspend fun deleteMetadata(id: Long)

    private suspend fun saveMetadataType(dto: MetadataDto) {
        val metadataType = MetadataType(dto.fileId, dto.contentType)
        metadataTypeRepository.save(metadataType).awaitSingle()
    }

    private suspend fun deleteMetadataType(id: Long) {
        metadataTypeRepository.deleteById(id).awaitSingle()
    }

    suspend fun get(id: Long): Metadata {
        return getMetadata(id)
    }

    suspend fun save(dto: MetadataDto) = coroutineScope {
        launch {
            saveMetadataType(dto)
        }
        launch {
            saveMetadata(dto)
        }
    }

    suspend fun delete(id: Long) = coroutineScope {
        deleteMetadata(id)
        deleteMetadataType(id)
    }
}