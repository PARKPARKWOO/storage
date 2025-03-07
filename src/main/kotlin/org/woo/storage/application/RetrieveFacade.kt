package org.woo.storage.application

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
import org.woo.storage.domain.metadata.Metadata

@Service
class RetrieveFacade(
    private val fileDocumentService: FileDocumentService,
    private val shortUrlService: ShortUrlService,
    private val metadataService: MetadataService,

    ) {
    suspend fun retrieveResource(path: String): Pair<Resource, Metadata> = coroutineScope {
        val resourceId = shortUrlService.getResourceId(path)

        val metadataJob = async {
            metadataService.getMetadata(resourceId)
        }
        val resourceJob = async {
            fileDocumentService.findById(resourceId)
        }
        Pair(resourceJob.await(), metadataJob.await())
    }
}