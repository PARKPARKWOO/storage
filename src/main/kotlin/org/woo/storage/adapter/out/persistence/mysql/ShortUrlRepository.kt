package org.woo.storage.adapter.out.persistence.mysql

import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.woo.storage.domain.short_url.ShortUrl
import reactor.core.publisher.Mono

interface ShortUrlRepository : ReactiveCrudRepository<ShortUrl, Long> {
    override fun <S : ShortUrl?> save(entity: S & Any): Mono<S> {
        return this.save(entity).doOnNext { it.markNotNew() }
    }
    fun findByShortUrl(shortUrl: String): Mono<ShortUrl?>
}