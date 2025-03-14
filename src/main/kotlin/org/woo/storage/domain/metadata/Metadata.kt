package org.woo.storage.domain.metadata

import java.time.LocalDateTime

interface Metadata {
    val fileId: Long
    val uploadedBy: String
    val applicationId: Long
    val contentLength: Long
    val contentType: String
    val chunkSize: Int
    val uploadedAt: LocalDateTime
    val pageSize: Int
    val fileName: String
}