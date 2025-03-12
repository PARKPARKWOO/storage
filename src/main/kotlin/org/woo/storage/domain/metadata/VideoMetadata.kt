package org.woo.storage.domain.metadata

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("video_metadata")
class VideoMetadata(
    @Id
    @Column("resource_id")
    override val fileId: Long,
    @Column("content_length")
    override val contentLength: Long,
    @Column("content_type")
    override val contentType: String,
    @Column("uploaded_at")
    override val uploadedAt: LocalDateTime = LocalDateTime.now(),
    @Column("file_name")
    override val fileName: String,
    @Column("uploaded_by")
    override val uploadedBy: String,
    @Column("chunk_size")
    override val chunkSize: Int,
    @Column("application_id")
    override val applicationId: Long,
): Metadata {
}