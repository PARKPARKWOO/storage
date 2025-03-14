package org.woo.storage.application

import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
import org.woo.storage.adapter.out.persistence.cassandra.FileChunkRepository
import org.woo.storage.domain.file.FileChunk
import org.woo.storage.domain.file.FileChunkKey
import java.nio.ByteBuffer

@Service
class FileDocumentService(
    private val fileChunkRepository: FileChunkRepository,
) {
    companion object {
        private const val FILE_SIZE_THRESHOLD: Long = 1048576
    }


    suspend fun findById(id: Long, chunkIndex: Int): Resource {
        val key = FileChunkKey(id, chunkIndex)
        val fileChunk = fileChunkRepository.findById(key).awaitSingle()
        val bytes = ByteArray(fileChunk.data.remaining())
        fileChunk.data.get(bytes)
        return ByteArrayResource(bytes)
    }

    suspend fun storeFile(fileBytes: ByteBuffer, fileId: Long, chunkIndex: Int) {
        val id = FileChunkKey(fileId = fileId, chunkIndex = chunkIndex)
        val fileChunk = FileChunk(data = fileBytes, id = id)
        fileChunkRepository.save(fileChunk).awaitSingle()
    }
}