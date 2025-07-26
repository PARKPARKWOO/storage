package org.woo.storage.ports.`in`


interface DeleteUseCase {
    suspend fun delete(fileId: Long)
}