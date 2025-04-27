package org.woo.storage.ports.`in`

import java.io.InputStream
import java.nio.ByteBuffer

interface UploadUseCase {
    suspend fun file(fileData: ByteBuffer, fileId: Long, chunkIndex: Int)

    suspend fun metadata(
        fileOriginName: String, uploadedBy: String, chunkSize: Int, contentLength: Long, applicationId: String, fileId: Long,
        pageSize: Int,
    )
}