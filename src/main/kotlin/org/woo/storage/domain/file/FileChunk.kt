package org.woo.storage.domain.file

import org.springframework.data.annotation.Id
import org.springframework.data.cassandra.core.mapping.PrimaryKey
import org.springframework.data.cassandra.core.mapping.Table

@Table("file_chunks")
data class FileChunk(
    @PrimaryKey
    val fileId: Long,
    val data: ByteArray,
)