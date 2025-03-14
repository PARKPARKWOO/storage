package org.woo.storage.adapter.out.persistence.mysql

import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.woo.storage.domain.metadata.MetadataType
import reactor.core.publisher.Mono

interface MetadataTypeRepository : ReactiveCrudRepository<MetadataType, String> {
}