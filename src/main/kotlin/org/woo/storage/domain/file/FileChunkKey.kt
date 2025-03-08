package org.woo.storage.domain.file

import org.springframework.data.cassandra.core.cql.PrimaryKeyType
import org.springframework.data.cassandra.core.mapping.PrimaryKey
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn

@PrimaryKeyClass
data class FileChunkKey(
    @PrimaryKeyColumn(name = "file_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    val fileId: Long,
    @PrimaryKeyColumn(name = "chunk_index", ordinal = 1, type = PrimaryKeyType.CLUSTERED)
    val chunkIndex: Int,
)
