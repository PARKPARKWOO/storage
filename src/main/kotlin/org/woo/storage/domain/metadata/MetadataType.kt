package org.woo.storage.domain.metadata

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("metadata_type")
class MetadataType(
    @Id
    @Column("resource_id")
    val resourceId: String? = null,
    @Column("type")
    val type: String,
) {
    companion object {
        fun create(type: ContentType): MetadataType = MetadataType(type = type.name)
    }
}