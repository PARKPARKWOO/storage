package org.woo.storage.adapter.`in`.rest

import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.woo.storage.application.RetrieveFacade
import org.woo.storage.domain.metadata.Metadata

@RestController
@RequestMapping("/api/v1/image")
class TestController(
    private val retrieveFacade: RetrieveFacade,
) {
    @GetMapping("/{path}")
    suspend fun download(@PathVariable("path") path: String): ResponseEntity<Resource> {
        val result: Pair<Resource, Metadata> = retrieveFacade.retrieveResource(path)
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"${result.second.fileName}\"")
            .contentType(MediaType.parseMediaType(result.second.contentType))
            .body(result.first)
    }
}