package org.woo.storage.domain.short_url

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.woo.util.generator.ShortUrl

/**
 * 현재 다른 url 은 사용하지 않음
 */
@Table("short_url")
class ShortUrl(
    @Id
    @Column(value = "id")
    val id: Long = 0L,
    @Column(value = "short_url")
    val shortUrl: String,
    @Column(value = "resource_id")
    val resourceId: String,
): Persistable<Long> {
    @Transient
    private var newEntity: Boolean = true
    override fun getId() = id

    @Transient
    override fun isNew(): Boolean = newEntity

    fun markNotNew() {
        newEntity = false
    }
}