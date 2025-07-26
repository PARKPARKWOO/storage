package org.woo.storage.application.facade

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.stereotype.Service
import org.woo.storage.application.FileDocumentService
import org.woo.storage.application.factory.MetadataFactory
import org.woo.storage.ports.`in`.DeleteUseCase

@Service
class FileDeleteFacade(
    private val fileDocumentService: FileDocumentService,
    private val metadataFactory: MetadataFactory,
) : DeleteUseCase{
    override suspend fun delete(fileId: Long): Unit = coroutineScope {
        launch {
            val handler = metadataFactory.getHandler(fileId)
            handler.delete(fileId)
        }
        launch {
            fileDocumentService.deleteFileByFileId(fileId).awaitSingle()
        }
    }
}