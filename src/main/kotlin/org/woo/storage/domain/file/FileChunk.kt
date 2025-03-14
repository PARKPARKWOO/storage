package org.woo.storage.domain.file

import org.springframework.data.cassandra.core.mapping.CassandraType
import org.springframework.data.cassandra.core.mapping.PrimaryKey
import org.springframework.data.cassandra.core.mapping.Table
import java.nio.ByteBuffer

@Table("file_chunks")
data class FileChunk(
    @PrimaryKey
    val id: FileChunkKey,
    @CassandraType(type = CassandraType.Name.BLOB)
    val data: ByteBuffer,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FileChunk

        if (id != other.id) return false
        if (data != other.data) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + data.hashCode()
        return result
    }
}