package org.woo.storage.config

import io.grpc.ConnectivityState
import io.grpc.ManagedChannel
import io.minio.BucketExistsArgs
import io.minio.MakeBucketArgs
import io.minio.MinioAsyncClient
import io.minio.MinioClient
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.woo.apm.log.log
import org.woo.storage.ports.out.AuthGrpcUseCase
import kotlin.coroutines.resume

@Configuration
@EnableConfigurationProperties(MinioProps::class)
class MinioConfig(
    private val authGrpcUseCase: AuthGrpcUseCase,
) {
    @Bean("minioClient")
    fun client(props: MinioProps): MinioClient = MinioClient.builder()
        .endpoint(props.endpoint)
        .credentials(props.accessKey, props.secretKey)
        .build()

    @Bean("minioAsyncClient")
    fun aClient(props: MinioProps): MinioAsyncClient =
        MinioAsyncClient.builder()
            .endpoint(props.endpoint)
            .credentials(props.accessKey, props.secretKey)
            .build()

    @Bean("minioInternalClient")
    fun iClient(props: MinioProps): MinioClient =
        MinioClient.builder()
            .endpoint(props.internalEndpoint)
            .credentials(props.accessKey, props.secretKey)
            .build()

    @Bean
    fun ensureBucket(
        minioProps: MinioProps,
        @Qualifier("minioInternalClient")
        minioInternalClient: MinioClient
    ) = CommandLineRunner {
//        if (props.bucket.isBlank()) return@CommandLineRunner
        log().info("Starting Create Minio Bucket")
        runBlocking {
            getApplicationInfo()
                .collect { app ->
                    val bucketName = app.id.toString().lowercase()
                    try {
                        val exists = minioInternalClient.bucketExists(
                            BucketExistsArgs.builder().bucket(bucketName).build()
                        )
                        if (!exists) {
                            minioInternalClient.makeBucket(
                                MakeBucketArgs.builder().bucket(bucketName).build()
                            )
                            log().info("Created bucket: {}", bucketName)
                        } else {
                            log().info("Bucket exists: {}", bucketName)
                        }
                    } catch (e: Exception) {
                        log().error("Failed to ensure bucket {}: {}", bucketName, e.message, e)
                    }
                }
        }
    }

    private fun getApplicationInfo() =
        authGrpcUseCase.getApplicationInfo()
}

@ConfigurationProperties(prefix = "minio")
data class MinioProps(
    var endpoint: String = "",
    var accessKey: String = "",
    var internalEndpoint: String,
    var secretKey: String = "",
    var secure: Boolean = true,
    var connectTimeoutMs: Long = 3000,
    var writeTimeoutMs: Long = 30000,
    var readTimeoutMs: Long = 30000,
    var partSizeMb: Int = 10,
    var selfSignedPemPath: String? = null,
)