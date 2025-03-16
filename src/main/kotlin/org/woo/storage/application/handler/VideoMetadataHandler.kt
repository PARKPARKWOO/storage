package org.woo.storage.application.handler

import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.stereotype.Component
import org.woo.storage.adapter.out.persistence.mysql.MetadataTypeRepository
import org.woo.storage.adapter.out.persistence.mysql.VideoMetadataRepository
import org.woo.storage.application.dto.MetadataDto
import org.woo.storage.domain.metadata.ContentType
import org.woo.storage.domain.metadata.Metadata
import org.woo.storage.domain.metadata.VideoMetadata

@Component
class VideoMetadataHandler(
    private val metadataTypeRepository: MetadataTypeRepository,
    private val videoMetadataRepository: VideoMetadataRepository,
): MetadataHandlerTemplate(metadataTypeRepository) {
    // TODO: 압축, 인코딩, 디코딩 로직 추가
    override suspend fun saveMetadata(dto: MetadataDto) {
        val videoEntity = VideoMetadata.from(dto)
        videoMetadataRepository.save(videoEntity).awaitSingle()
    }

    override suspend fun isApplicable(contentType: ContentType): Boolean = contentType == ContentType.VIDEO

    override suspend fun getMetadata(id: Long): Metadata = videoMetadataRepository.findById(id).awaitSingle()
}