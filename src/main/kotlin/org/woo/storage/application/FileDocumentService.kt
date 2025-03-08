package org.woo.storage.application

import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource
import org.springframework.core.io.buffer.DefaultDataBufferFactory
import org.springframework.stereotype.Service
import org.woo.storage.adapter.out.persistence.cassandra.FileChunkRepository
import org.woo.storage.domain.file.FileChunk
import org.woo.storage.domain.file.FileChunkKey
import reactor.core.publisher.Flux

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
        val resource = ByteArrayResource(fileChunk.data)
        return resource
    }

    suspend fun storeFile(fileBytes: ByteArray): String {
        return if (fileBytes.size < FILE_SIZE_THRESHOLD) {
            storeInDocument(fileBytes)
        } else {
//            storeInGridFs(fileBytes)
            TODO()
        }
    }

    private suspend fun storeInDocument(fileBytes: ByteArray): String {
//        val fileChunk = FileChunk(
//
//        )
        TODO()
//        return saved.id ?: throw Exception("저장 실패")
    }
}