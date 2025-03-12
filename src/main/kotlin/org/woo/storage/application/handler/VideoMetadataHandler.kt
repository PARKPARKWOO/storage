package org.woo.storage.application.handler

import org.springframework.stereotype.Component
import org.woo.storage.application.dto.MetadataDto

@Component
class VideoMetadataHandler: MetadataHandlerTemplate() {
    override suspend fun saveMetadata(dto: MetadataDto) {
        TODO("Not yet implemented")
    }

    override suspend fun saveMetadataType(dto: MetadataDto) {
        TODO("Not yet implemented")
    }
}