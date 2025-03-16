package org.woo.storage.domain.metadata

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.woo.storage.application.dto.MetadataDto
import java.time.LocalDateTime

@Table("video_metadata")
class VideoMetadata(
    @Id
    @Column("file_id")
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
    @Column("page_size")
    override val pageSize: Int,
): Metadata, Persistable<Long> {
    companion object {
        fun from(dto: MetadataDto) = VideoMetadata(
            fileId = dto.fileId,
            chunkSize = dto.chunkSize,
            contentLength = dto.contentLength,
            contentType = dto.mediaType,
            applicationId = dto.applicationId,
            uploadedAt = dto.uploadedAt,
            uploadedBy = dto.uploadedBy,
            fileName = dto.fileName,
            pageSize = dto.pageSize,
        )
    }

    @Transient
    private var newEntity: Boolean = true
    override fun getId() = fileId

    @Transient
    override fun isNew(): Boolean = newEntity

    fun markNotNew() {
        newEntity = false
    }
}