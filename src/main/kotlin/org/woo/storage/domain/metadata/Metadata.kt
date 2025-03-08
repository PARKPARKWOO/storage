package org.woo.storage.domain.metadata

import java.time.LocalDateTime

interface Metadata {
    val fileId: Long
    val uploadedBy: String
    val contentLength: Long
    val contentType: String
    val uploadedAt: LocalDateTime
    val fileName: String
}