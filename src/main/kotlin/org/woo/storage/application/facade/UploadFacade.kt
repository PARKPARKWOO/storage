package org.woo.storage.application.facade

import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Service
import org.woo.storage.application.FileDocumentService
import org.woo.storage.application.factory.MetadataFactory
import org.woo.storage.ports.`in`.UploadUseCase
import java.nio.ByteBuffer

@Service
class UploadFacade(
    private val fileDocumentService: FileDocumentService,
    private val metadataFactory: MetadataFactory,
): UploadUseCase {
    override suspend fun file(fileData: ByteBuffer, fileId: Long, chunkIndex: Int) = coroutineScope {
            fileDocumentService.storeFile(fileData, fileId, chunkIndex)
    }

    override suspend fun metadata(
        fileOriginName: String,
        uploadedBy: String,
        chunkSize: Int,
        contentLength: Long,
        applicationId: Long,
        fileId: Long,
        pageSize: Int,
    ) {
        val handler = metadataFactory.getHandler(fileOriginName)
        val dto = metadataFactory.createDto(fileOriginName, uploadedBy, chunkSize, contentLength, fileId, applicationId, pageSize)
        handler.save(dto)
    }
}