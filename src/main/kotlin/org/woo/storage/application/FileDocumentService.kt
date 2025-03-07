package org.woo.storage.application

import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource
import org.springframework.core.io.buffer.DefaultDataBufferFactory
import org.springframework.stereotype.Service
import org.woo.storage.domain.file.FileChunk
import reactor.core.publisher.Flux

@Service
class FileDocumentService(
    val mongoTemplate: ReactiveMongoTemplate,
    val gridFsTemplate: ReactiveGridFsTemplate,
) {
    companion object {
        private const val FILE_SIZE_THRESHOLD: Long = 1048576
    }

    suspend fun findById(id: String): Resource {
        val fileChunk = mongoTemplate.findById(id, FileChunk::class.java).awaitSingle()
        // FileDocument에서 파일 데이터(byte array) 추출
        val fileBytes: ByteArray = fileChunk.fileData.data
        val objectId = gridFsTemplate.find(Query()).awaitSingle()
        // ByteArrayResource로 변환 (Spring Resource)
        val resource = ByteArrayResource(fileBytes)

        return resource
    }

    suspend fun storeFile(fileBytes: ByteArray): String {
        return if (fileBytes.size < FILE_SIZE_THRESHOLD) {
            storeInDocument(fileBytes)
        } else {
            storeInGridFs(fileBytes)
        }
    }

    private suspend fun storeInDocument(fileBytes: ByteArray): String {
        val fileChunk = FileChunk(
            fileData = Binary(fileBytes),
        )
        val saved = mongoTemplate.save(fileChunk).awaitSingle()
        return saved.id ?: throw Exception("저장 실패")
    }

    private suspend fun storeInGridFs(fileName: String, fileBytes: ByteArray): String {
        val factory = DefaultDataBufferFactory()
        val dataBuffer = factory.wrap(fileBytes)
        val storedId = gridFsTemplate.store(Flux.just(dataBuffer), fileName, fileName.substringAfterLast("."))
            .awaitSingle()
        return storedId.toString()
    }
}