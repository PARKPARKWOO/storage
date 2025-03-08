package org.woo.storage.application

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Service
import org.woo.storage.ports.`in`.UploadUseCase

@Service
class UploadFacade(
    private val fileDocumentService: FileDocumentService,
    private val metadataService: MetadataService,
): UploadUseCase {
    override suspend fun upload(fileData: ByteArray): Unit = coroutineScope {
            fileDocumentService.storeFile(fileData)
    }

    override suspend fun metadata(fineName: String) = coroutineScope { metadataService.save(fineName) }
}