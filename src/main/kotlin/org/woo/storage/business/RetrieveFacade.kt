package org.woo.storage.business

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
import org.springframework.util.MimeType

@Service
class RetrieveFacade(
    private val fileDocumentService: FileDocumentService,
    private val shortUrlService: ShortUrlService,
    private val metadataService: MetadataService,

    ) {
    suspend fun retrieveResource(path: String): Pair<Resource, MimeType> = coroutineScope {
        val resourceId = shortUrlService.getResourceId(path)

        val metadataJob = async {
            metadataService.getMetadata(resourceId)
        }
        val resourceJob = async {
            fileDocumentService.findById(resourceId)
        }
        MimeType.
        Pair(resourceJob.await(), metadataJob.await())
    }
}