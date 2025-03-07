package org.woo.storage.domain.metadata

import org.springframework.http.MediaType
import java.time.LocalDateTime

interface Metadata {
    val resourceId: String
    val contentLength: Long
    val contentType: String
    val createdAt: LocalDateTime
    val fileName: String
}