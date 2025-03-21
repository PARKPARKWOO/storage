package org.woo.storage.adapter.out.persistence.mysql

import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.woo.storage.domain.metadata.ImageMetadata
import reactor.core.publisher.Mono

interface ImageMetadataRepository : ReactiveCrudRepository<ImageMetadata, Long> {
    override fun <S : ImageMetadata?> save(entity: S & Any): Mono<S> {
        return this.save(entity).doOnNext { it.markNotNew() }
    }
}