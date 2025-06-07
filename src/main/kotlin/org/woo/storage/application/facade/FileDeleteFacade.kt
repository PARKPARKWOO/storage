package org.woo.storage.application.facade

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
    override suspend fun file(fileId: Long) {
        fileDocumentService.deleteFileByFileId(fileId).awaitSingle()
    }

    override suspend fun metadata(fileOriginName: String, fileId: Long) {
        val handler = metadataFactory.getHandler(fileOriginName)
        handler.delete(fileId)
    }
}