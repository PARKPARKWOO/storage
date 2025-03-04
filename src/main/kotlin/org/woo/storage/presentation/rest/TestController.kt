package org.woo.storage.presentation.rest

import kotlinx.coroutines.reactive.awaitFirst
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.woo.storage.business.FileDocumentService
import org.woo.storage.business.RetrieveFacade
import org.woo.storage.domain.file.FileDocument

@RestController
@RequestMapping("/api/v1/image")
class TestController(
    private val retrieveFacade: RetrieveFacade,
) {
    @GetMapping("/{path}")
    suspend fun download(@PathVariable("path") path: String): ResponseEntity<Resource> {
        val resource = retrieveFacade.retrieveResource(path)
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"${fileDocument.fileName}\"")
            .contentType(MediaType.parseMediaType(mimeType))
            .body(resource)
    }
}