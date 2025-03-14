package org.woo.storage.application.facade

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
import org.woo.storage.application.FileDocumentService
import org.woo.storage.application.ShortUrlService
import org.woo.storage.domain.metadata.Metadata

@Service
class RetrieveFacade(
    private val fileDocumentService: FileDocumentService,
    private val shortUrlService: ShortUrlService,
) {
    suspend fun retrieveResource(path: String): Pair<Resource, Metadata> = coroutineScope {
        val resourceId = shortUrlService.getResourceId(path)

        val metadataJob = async {
            TODO()
        }
        val resourceJob = async {
            fileDocumentService.findById(resourceId.toLong(), 0)
//            TODO()
        }
        Pair(resourceJob.await(), metadataJob.await())
    }
}