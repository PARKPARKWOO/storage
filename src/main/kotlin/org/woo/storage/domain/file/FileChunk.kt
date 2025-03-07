package org.woo.storage.domain.file

import org.springframework.data.annotation.Id
import org.springframework.data.cassandra.core.mapping.Table

@Table("file_chunk")
data class FileChunk(
    @Id
    val fileId: Long,
    val chunkIndex: Int,
    val data: ByteArray,
)