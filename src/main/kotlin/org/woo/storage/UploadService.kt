package org.woo.storage

import kotlinx.coroutines.reactive.awaitSingle
import org.bson.types.Binary
import org.springframework.core.io.buffer.DefaultDataBufferFactory
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.gridfs.ReactiveGridFsTemplate
import org.springframework.http.MediaType
import org.springframework.http.MediaTypeFactory
import org.springframework.stereotype.Service
import org.springframework.util.MimeType
import reactor.core.publisher.Flux

@Service
class UploadService(
    private val gridFsTemplate: ReactiveGridFsTemplate,
    private val mongoTemplate: ReactiveMongoTemplate,
) {
    companion object {
        private const val FILE_SIZE_THRESHOLD: Long = 1048576
    }

    suspend fun storeFile(fileName: String, fileBytes: ByteArray): String {
        return if (fileBytes.size < FILE_SIZE_THRESHOLD) {
            storeInDocument(fileName, fileBytes)
        } else {
            storeInGridFs(fileName, fileBytes)
        }
    }

    private suspend fun storeInDocument(fileName: String, fileBytes: ByteArray): String {
        val mediaType = extractContentType(fileName)
        val fileDocument = FileDocument(
            fileName = fileName,
            contentType = mediaType,
            fileData = Binary(fileBytes),
        )
        val saved = mongoTemplate.save(fileDocument).awaitSingle()
        return saved.id ?: throw Exception("저장 실패")
    }

    private suspend fun storeInGridFs(fileName: String, fileBytes: ByteArray): String {
        val factory = DefaultDataBufferFactory()
        val dataBuffer = factory.wrap(fileBytes)
        val storedId = gridFsTemplate.store(Flux.just(dataBuffer), fileName, fileName.substringAfterLast("."))
            .awaitSingle()
        return storedId.toString()
    }

    private suspend fun extractContentType(fileName: String): String =
        MediaTypeFactory.getMediaType(fileName)
            .orElse(MediaType.APPLICATION_OCTET_STREAM)
            .toString()
}