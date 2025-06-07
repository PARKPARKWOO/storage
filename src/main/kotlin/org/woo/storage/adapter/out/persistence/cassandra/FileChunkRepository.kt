package org.woo.storage.adapter.out.persistence.cassandra

import org.springframework.data.cassandra.repository.ReactiveCassandraRepository
import org.woo.storage.domain.file.FileChunk
import org.woo.storage.domain.file.FileChunkKey
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface FileChunkRepository : ReactiveCassandraRepository<FileChunk, FileChunkKey>{
    fun deleteByIdFileId(fileId: Long): Mono<Void>

    fun findByIdFileId(fileId: Long): Flux<FileChunk>
}