package org.woo.storage.ports.`in`

import java.nio.ByteBuffer

interface UploadUseCase {
    suspend fun file(fileData: ByteBuffer, fileId: Long, chunkIndex: Int)

    suspend fun metadata(
        fileOriginName: String, uploadedBy: String, chunkSize: Int, contentLength: Long, applicationId: Long, fileId: Long,
        pageSize: Int,
    )
}