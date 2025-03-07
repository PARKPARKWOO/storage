package org.woo.storage.domain.metadata

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("video_metadata")
class VideoMetadata(
    @Id
    @Column("resource_id")
    override val resourceId: String,
    @Column("content_length")
    override val contentLength: Long,
    @Column("content_type")
    override val contentType: String,
    override val createdAt: LocalDateTime = LocalDateTime.now(),
    override val fileName: String
): Metadata {
}