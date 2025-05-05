package org.woo.storage.ports.out

import dto.UserContext
import reactor.core.publisher.Mono

interface AuthGrpcUseCase {
    fun getUserContext(token: String): Mono<UserContext>
}