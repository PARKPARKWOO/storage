package org.woo.storage.domain.metadata

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("metadata_type")
class MetadataType(
    @Id
    @Column("file_id")
    val fileId: Long = 0,
    @Column("type")
    val type: String,
): Persistable<Long> {
    companion object {
        fun create(type: ContentType): MetadataType = MetadataType(type = type.name)
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