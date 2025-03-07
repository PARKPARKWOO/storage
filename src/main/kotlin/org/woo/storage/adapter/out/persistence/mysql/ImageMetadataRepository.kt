package org.woo.storage.adapter.out.persistence.mysql

import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.woo.storage.domain.metadata.ImageMetadata
import reactor.core.publisher.Mono

interface ImageMetadataRepository : ReactiveCrudRepository<ImageMetadata, String> {
    fun findByResourceId(resourceId: String): Mono<ImageMetadata?>
}