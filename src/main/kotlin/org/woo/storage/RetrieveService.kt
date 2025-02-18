package org.woo.storage

import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.core.io.ByteArrayResource
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.stereotype.Service

@Service
class RetrieveService(
    val mongoTemplate: ReactiveMongoTemplate,
) {
    suspend fun findById(id: String) {
        val fileDocument = mongoTemplate.findById(id, FileDocument::class.java).awaitSingle()

        // FileDocument에서 파일 데이터(byte array) 추출
        val fileBytes: ByteArray = fileDocument.fileData.data

        // ByteArrayResource로 변환 (Spring Resource)
        val resource = ByteArrayResource(fileBytes)

        val mimeType = fileDocument.contentType.ifEmpty { "application/octet-stream" }
    }
}