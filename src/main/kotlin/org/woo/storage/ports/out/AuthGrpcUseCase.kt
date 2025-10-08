package org.woo.storage.ports.out

import dto.UserContext
import kotlinx.coroutines.flow.Flow
import org.woo.auth.grpc.ApplicationProto
import reactor.core.publisher.Mono

interface AuthGrpcUseCase {
    fun getUserContext(token: String): Mono<UserContext>

    fun getApplicationInfo(): Flow<ApplicationProto.ApplicationInfoResponse>
}