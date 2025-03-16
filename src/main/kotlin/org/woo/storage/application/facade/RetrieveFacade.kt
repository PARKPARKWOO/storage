package org.woo.storage.application.facade

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
import org.woo.storage.adapter.out.persistence.mysql.MetadataTypeRepository
import org.woo.storage.application.FileDocumentService
import org.woo.storage.application.ShortUrlService
import org.woo.storage.application.factory.MetadataFactory
import org.woo.storage.domain.metadata.ContentType
import org.woo.storage.domain.metadata.Metadata

@Service
class RetrieveFacade(
    private val fileDocumentService: FileDocumentService,
    private val metadataFactory: MetadataFactory,
    private val metadataTypeRepository: MetadataTypeRepository,
    private val shortUrlService: ShortUrlService,
) {
    suspend fun retrieveResource(id: Long): Pair<Resource, Metadata> = coroutineScope {
//        val resourceId = shortUrlService.getResourceId(path)
        val metadataType = metadataTypeRepository.findById(id).awaitSingle()
        val contentType = ContentType.valueOf(metadataType.type)

        val handler = metadataFactory.getHandler(contentType)

        val metadata = handler.get(id)

        val chunkResources: List<Resource> = (0 until metadata.pageSize).map { index ->
            async { fileDocumentService.findById(id, index) }
        }.awaitAll()

        val combinedBytes = chunkResources
            .map { it.inputStream.readBytes() }
            .reduce { acc, bytes -> acc + bytes }

        val finalResource: Resource = ByteArrayResource(combinedBytes)

        Pair(finalResource, metadata)
    }
}