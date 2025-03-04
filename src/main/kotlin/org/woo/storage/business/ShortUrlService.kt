package org.woo.storage.business

import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.stereotype.Service
import org.woo.storage.domain.ShortUrlRepository
import org.woo.util.generator.ShortUrl
import org.woo.storage.domain.short_url.ShortUrl as ShortUrlEntity

@Service
class ShortUrlService(
    val shortUrlRepository: ShortUrlRepository,
) {
    companion object {
        const val CACHE_URL_LENGTH = 6
        const val PERSISTENT_URL_LENGTH = 7
    }
    suspend fun generateShortUrl(isTemporary: Boolean, resourceId: String) {
        val shortUrl = if (isTemporary) {
            ShortUrl.generateByBase62(CACHE_URL_LENGTH)
        } else {
            ShortUrl.generateByBase62(PERSISTENT_URL_LENGTH)
        }
        val shortUrlEntity = ShortUrlEntity(shortUrl = shortUrl, resourceId = resourceId)
        shortUrlRepository.save(shortUrlEntity).awaitSingle()
    }

    suspend fun getResourceId(shortUrl: String): String {
        return shortUrlRepository.findByShortUrl(shortUrl).awaitSingleOrNull()?.resourceId
            ?: throw RuntimeException("not exist resource $shortUrl")
    }
}