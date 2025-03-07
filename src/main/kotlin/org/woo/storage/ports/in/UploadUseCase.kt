package org.woo.storage.ports.`in`

interface UploadUseCase {
    suspend fun upload(fileData: ByteArray, fileName: String)
}