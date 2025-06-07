package org.woo.storage.ports.`in`


interface DeleteUseCase {
    suspend fun file(fileId: Long)

    suspend fun metadata(
        fileOriginName: String, fileId: Long,
    )
}