package org.woo.storage.adapter.out.persistence.cassandra

import org.springframework.data.cassandra.repository.ReactiveCassandraRepository
import org.woo.storage.domain.file.FileChunk
import org.woo.storage.domain.file.FileChunkKey

interface FileChunkRepository : ReactiveCassandraRepository<FileChunk, FileChunkKey>{
}