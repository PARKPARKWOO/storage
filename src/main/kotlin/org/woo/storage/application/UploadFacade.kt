package org.woo.storage.application

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Service
import org.woo.storage.application.dto.MetadataDto
import org.woo.storage.application.factory.MetadataFactory
import org.woo.storage.ports.`in`.UploadUseCase

@Service
class UploadFacade(
    private val fileDocumentService: FileDocumentService,
    private val metadataFactory: MetadataFactory,
): UploadUseCase {
    override suspend fun upload(fileData: ByteArray, fileId: Long, chunkIndex: Int): Unit = coroutineScope {
            fileDocumentService.storeFile(fileData)
    }

    override suspend fun metadata(
        fineName: String,
        uploadedBy: String,
        chunkSize: Int,
        contentLength: Long,
        applicationId: Long,
        fileId: Long
    ) {
        val handler = metadataFactory.getHandler(fineName)
        val dto = metadataFactory.createDto(fineName, uploadedBy, chunkSize, contentLength, fileId)
        handler.save(dto)
    }
}