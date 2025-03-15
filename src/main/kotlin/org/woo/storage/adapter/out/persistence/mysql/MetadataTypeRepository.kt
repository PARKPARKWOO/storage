package org.woo.storage.adapter.out.persistence.mysql

import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.woo.storage.domain.metadata.MetadataType
import reactor.core.publisher.Mono

interface MetadataTypeRepository : ReactiveCrudRepository<MetadataType, String> {
    override fun <S : MetadataType?> save(entity: S & Any): Mono<S> {
        return this.save(entity).doOnNext { it.markNotNew() }
    }
}