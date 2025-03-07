package org.woo.storage.adapter.out.persistence.mysql

import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.woo.storage.domain.metadata.FileMetadata
import reactor.core.publisher.Mono

interface FileMetadataRepository : ReactiveCrudRepository<FileMetadata, String> {
    fun findByResourceId(resourceId: String): Mono<FileMetadata?>
}