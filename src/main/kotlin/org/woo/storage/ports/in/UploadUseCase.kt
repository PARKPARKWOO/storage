package org.woo.storage.ports.`in`

interface UploadUseCase {
    suspend fun upload(fileData: ByteArray)

    suspend fun metadata(fineName: String)
}