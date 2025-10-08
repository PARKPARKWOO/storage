package org.woo.storage.config

import io.minio.BucketExistsArgs
import io.minio.MakeBucketArgs
import io.minio.MinioAsyncClient
import io.minio.MinioClient
import kotlinx.coroutines.flow.map
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.woo.storage.ports.out.AuthGrpcUseCase

@Configuration
@EnableConfigurationProperties(MinioProps::class)
class MinioConfig(
    private val authGrpcUseCase: AuthGrpcUseCase,
) {
    @Bean
    fun client(props: MinioProps): MinioClient = MinioClient.builder()
        .endpoint(props.endpoint)
        .credentials(props.accessKey, props.secretKey)
        .build()

    @Bean
    fun aClient(props: MinioProps): MinioAsyncClient =
        MinioAsyncClient.builder()
            .endpoint(props.endpoint)
            .credentials(props.accessKey, props.secretKey)
            .build()

    @Bean
    fun ensureBucket(minioClient: MinioClient, asyncClient: MinioAsyncClient, props: MinioProps) = CommandLineRunner {
//        if (props.bucket.isBlank()) return@CommandLineRunner
        getApplicationInfo().map {
            val bucket = BucketExistsArgs.builder()
                .bucket(it.name)
                .build()
            val exists = minioClient.bucketExists(bucket)
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(it.name).build())
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
    var secretKey: String = "",
    var bucket: String = "",
    var secure: Boolean = true,
    var connectTimeoutMs: Long = 3000,
    var writeTimeoutMs: Long = 30000,
    var readTimeoutMs: Long = 30000,
    var partSizeMb: Int = 10,
    var selfSignedPemPath: String? = null,
)