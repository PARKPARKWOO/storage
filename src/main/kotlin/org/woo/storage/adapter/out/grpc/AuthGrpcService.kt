package org.woo.storage.adapter.out.grpc

import com.google.protobuf.Empty
import dto.UserContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.devh.boot.grpc.client.inject.GrpcClient
import org.springframework.stereotype.Service
import org.woo.auth.grpc.AuthProto
import org.woo.auth.grpc.UserInfoServiceGrpcKt
import org.woo.grpc.interceptor.TokenInitializeInMetadata
import org.woo.storage.ports.out.AuthGrpcUseCase
import reactor.core.publisher.Mono

@Service
class AuthGrpcService : AuthGrpcUseCase {
    @GrpcClient("auth")
    lateinit var userInfoService: UserInfoServiceGrpcKt.UserInfoServiceCoroutineStub
    override fun getUserContext(token: String): Mono<UserContext> {
        return Mono.create { sink ->
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = userInfoService
                        .withInterceptors(TokenInitializeInMetadata(token))
                        .getUserInfoByBearer(Empty.getDefaultInstance())
                    sink.success(
                        UserContext(
                            email = response.email,
                            userName = response.name,
                            applicationRole = response.applicationRole,
                            accessLevel = response.accessLevel
                        )
                    )
                } catch (e: Exception) {
                    sink.error(e)
                }
            }
        }
    }
}