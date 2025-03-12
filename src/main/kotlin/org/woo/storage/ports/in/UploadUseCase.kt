package org.woo.storage.ports.`in`

interface UploadUseCase {
    suspend fun upload(fileData: ByteArray, fileId: Long, chunkIndex: Int)

    suspend fun metadata(
        fineName: String, uploadedBy: String, chunkSize: Int, contentLength: Long, applicationId: Long, fileId: Long
    )
}