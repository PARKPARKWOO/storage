package org.woo.storage.presentation

import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.remove
import org.springframework.data.mongodb.gridfs.ReactiveGridFsTemplate
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.woo.storage.FileDocument
import org.woo.storage.UploadService

@RestController
@RequestMapping("/api/test")
class TestController(
    private val mongoTemplate: ReactiveMongoTemplate,
    val uploadService: UploadService,
) {
    @GetMapping
    suspend fun download(): ResponseEntity<Resource> {
        val fileDocument = mongoTemplate.findAll(FileDocument::class.java).awaitFirst()

        // FileDocument에서 파일 데이터(byte array) 추출
        val fileBytes: ByteArray = fileDocument.fileData.data

        // ByteArrayResource로 변환 (Spring Resource)
        val resource = ByteArrayResource(fileBytes)

        val mimeType = fileDocument.contentType.ifEmpty { "application/octet-stream" }

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"${fileDocument.fileName}\"")
            .contentType(MediaType.parseMediaType(mimeType))
            .body(resource)
    }
}