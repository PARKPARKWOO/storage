package org.woo.storage.adapter.out.persistence.mysql

import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.woo.storage.domain.metadata.FileMetadata
import reactor.core.publisher.Mono

interface FileMetadataRepository : ReactiveCrudRepository<FileMetadata, String> {
    override fun <S : FileMetadata?> save(entity: S & Any): Mono<S> {
        return this.save(entity).doOnNext { it.markNotNew() }
    }
}