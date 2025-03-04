package org.woo.storage.domain.file

import org.bson.types.Binary
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "fileDocument")
data class FileDocument(
    @Id
    val id: String? = null,
    val fileName: String,
    val contentType: String,
    val fileData: Binary,
    val createdAt: LocalDateTime = LocalDateTime.now()
)