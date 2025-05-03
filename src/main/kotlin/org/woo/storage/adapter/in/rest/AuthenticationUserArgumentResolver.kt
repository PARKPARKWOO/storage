package org.woo.storage.adapter.`in`.rest

import annotation.AuthenticationUser
import com.fasterxml.jackson.databind.ObjectMapper
import constant.AuthConstant
import dto.UserContext
import exception.AuthException
import exception.ErrorCode
import org.springframework.core.MethodParameter
import org.springframework.stereotype.Component
import org.springframework.web.reactive.BindingContext
import org.springframework.web.reactive.result.method.HandlerMethodArgumentResolver
import org.springframework.web.server.ServerWebExchange
import org.woo.storage.application.exception.BusinessException
import reactor.core.publisher.Mono

@Component
class AuthenticationUserArgumentResolver(
    private val objectMapper: ObjectMapper
) : HandlerMethodArgumentResolver{
    override fun supportsParameter(parameter: MethodParameter): Boolean =
        parameter.hasParameterAnnotation(AuthenticationUser::class.java)

    override fun resolveArgument(
        parameter: MethodParameter,
        bindingContext: BindingContext,
        exchange: ServerWebExchange
    ): Mono<Any?> {
        val ann = parameter.getParameterAnnotation(AuthenticationUser::class.java)!!
        val headerValue = exchange.request.headers.getFirst("X-User-Passport")
        val passport = headerValue
            ?.takeIf { it.isNotBlank() }
            ?.let { objectMapper.readValue(it, UserContext::class.java) }

        return when {
            passport != null -> Mono.just(passport)
            ann.isRequired    -> Mono.error(AuthException(ErrorCode.FORBIDDEN, null))
            else              -> Mono.justOrEmpty(null)
        }
    }
}