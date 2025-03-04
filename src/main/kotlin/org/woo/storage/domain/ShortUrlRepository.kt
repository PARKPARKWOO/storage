package org.woo.storage.domain

import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.woo.storage.domain.short_url.ShortUrl
import reactor.core.publisher.Mono

interface ShortUrlRepository : ReactiveCrudRepository<ShortUrl, Long> {
    fun findByShortUrl(shortUrl: String): Mono<ShortUrl?>
}