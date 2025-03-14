package org.woo.storage.adapter.out.persistence.mysql

import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.woo.storage.domain.metadata.VideoMetadata
import reactor.core.publisher.Mono

interface VideoMetadataRepository: ReactiveCrudRepository<VideoMetadata, String> {
}