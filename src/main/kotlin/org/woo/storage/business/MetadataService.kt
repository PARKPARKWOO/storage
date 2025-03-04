package org.woo.storage.business

import org.springframework.stereotype.Service
import org.woo.storage.domain.metadata.Metadata

@Service
class MetadataService {
    suspend fun getMetadata(resourceId: String): Metadata{
        TODO()
    }
}