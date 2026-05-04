package org.woo.storage.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.config.WebFluxConfigurer
import org.springframework.web.reactive.result.method.annotation.ArgumentResolverConfigurer
import org.woo.storage.adapter.`in`.rest.AuthenticationUserArgumentResolver

/**
 * CORS 는 Gateway (`globalcors.cors-configurations`) 에서 단일 source 로 관리.
 * 본 모듈에서 별도 [org.springframework.web.cors.reactive.CorsWebFilter] 를 등록하면
 * Gateway 와 동일 헤더(`Access-Control-Allow-Origin`, `Access-Control-Allow-Credentials`,
 * `vary` 등)를 두 번씩 응답에 붙여 브라우저 CORS spec 위반 — 멀티값 origin 으로
 * 인식되어 모든 cross-origin fetch (e.g. mobile WebView 의 model-viewer GLB 다운로드)가
 * `has been blocked by CORS policy: ... contains multiple values` 로 reject 된다.
 *
 * 핫픽스: Storage 는 CorsWebFilter Bean 을 제공하지 않는다. Gateway 가 모든 inbound
 * 트래픽의 CORS 헤더를 단일하게 책임진다.
 */
@Configuration
class WebConfig(
    private val authenticationUserArgumentResolver: AuthenticationUserArgumentResolver,
) : WebFluxConfigurer {
    override fun configureArgumentResolvers(configurer: ArgumentResolverConfigurer) {
        configurer.addCustomResolver(authenticationUserArgumentResolver)
    }
}