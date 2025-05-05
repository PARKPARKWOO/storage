package org.woo.storage.adapter.`in`.rest

import annotation.AuthenticationUser
import com.fasterxml.jackson.databind.ObjectMapper
import constant.AuthConstant
import constant.AuthConstant.AUTHORIZATION_HEADER
import dto.Passport
import dto.UserContext
import exception.AuthException
import exception.ErrorCode
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.mono
import kotlinx.coroutines.runBlocking
import org.springframework.core.MethodParameter
import org.springframework.stereotype.Component
import org.springframework.web.reactive.BindingContext
import org.springframework.web.reactive.result.method.HandlerMethodArgumentResolver
import org.springframework.web.server.ServerWebExchange
import org.woo.storage.application.exception.BusinessException
import org.woo.storage.ports.out.AuthGrpcUseCase
import reactor.core.publisher.Mono

@Component
class AuthenticationUserArgumentResolver(
    private val objectMapper: ObjectMapper,
    private val authGrpcUseCase: AuthGrpcUseCase,
) : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter): Boolean =
        parameter.hasParameterAnnotation(AuthenticationUser::class.java)

    override fun resolveArgument(
        parameter: MethodParameter,
        bindingContext: BindingContext,
        exchange: ServerWebExchange
    ): Mono<Any?> {
        val ann = parameter.getParameterAnnotation(AuthenticationUser::class.java)
        val headerValue = exchange.request.headers.getFirst("X-User-Passport")
        if (headerValue.isNullOrBlank()) {
            return if (ann != null && ann.isRequired) {
                Mono.error(AuthException(ErrorCode.FORBIDDEN, null))
            } else {
                Mono.empty()
            }
        }

        val passportMono = Mono.fromCallable { objectMapper.readValue(headerValue, Passport::class.java) }

        return if (ann != null) {
            passportMono.flatMap { passport ->
                val token = exchange.request.headers
                    .getFirst(AUTHORIZATION_HEADER)
                    .orEmpty()
                authGrpcUseCase.getUserContext(token)
                    .map { userCtx ->
                        passport.copy(userContext = userCtx)
                    }
            }.cast(Any::class.java)
        } else {
            passportMono.cast(Any::class.java)
        }
    }
}