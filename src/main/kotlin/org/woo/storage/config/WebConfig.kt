package org.woo.storage.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsConfigurationSource
import org.springframework.web.cors.reactive.CorsWebFilter
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource
import org.springframework.web.reactive.config.WebFluxConfigurer
import org.springframework.web.reactive.result.method.annotation.ArgumentResolverConfigurer
import org.woo.storage.adapter.`in`.rest.AuthenticationUserArgumentResolver

@Configuration
class WebConfig(
    private val authenticationUserArgumentResolver: AuthenticationUserArgumentResolver
): WebFluxConfigurer {
    @Bean
    fun corsWebFilter(): CorsWebFilter {
        val config = CorsConfiguration().apply {
            // 모든 Origin 허용
            addAllowedOriginPattern("*")
            // 모든 HTTP 메서드 허용 (GET, POST, PUT, DELETE, etc)
            addAllowedMethod("*")
            // 모든 헤더 허용
            addAllowedHeader("*")
            // 필요 시 쿠키/자격증명 허용
            allowCredentials = true
            // pre-flight 요청의 캐시 유효시간 설정 (초)
            maxAge = 3600L
        }

        val source: CorsConfigurationSource = UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", config)
        }
        return CorsWebFilter(source)
    }

    override fun configureArgumentResolvers(configurer: ArgumentResolverConfigurer) {
        configurer.addCustomResolver(authenticationUserArgumentResolver)
    }
}