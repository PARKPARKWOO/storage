package org.woo.storage.presentation.rest

import kotlinx.coroutines.reactive.awaitFirst
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.woo.storage.domain.FileDocument
import org.woo.storage.business.UploadService

@RestController
@RequestMapping("/api/test")
class TestController(
    private val mongoTemplate: ReactiveMongoTemplate,
    val uploadService: UploadService,
) {
    @GetMapping
    suspend fun download(): ResponseEntity<Resource> {
        val fileDocument = mongoTemplate.findAll(FileDocument::class.java).awaitFirst()
        val fileBytes: ByteArray = fileDocument.fileData.data

        val resource = ByteArrayResource(fileBytes)

        val mimeType = fileDocument.contentType.ifEmpty { "application/octet-stream" }

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"${fileDocument.fileName}\"")
            .contentType(MediaType.parseMediaType(mimeType))
            .body(resource)
    }
}