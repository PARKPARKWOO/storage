package org.woo.storage.config

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.support.serializer.JsonSerializer
import org.woo.event.api.InternalApiCallEvent

@Configuration
class KafkaConfig(
    @Value("\${spring.kafka.bootstrap-servers}")
    val boostrapServers: String,
    @Value("\${spring.kafka.consumer.group-id}")
    val groupId: String,
) {
    @Bean
    fun producerFactory(): ProducerFactory<String, InternalApiCallEvent> {
        val props = generateProducerConfig()
        return DefaultKafkaProducerFactory(props)
    }

    private fun generateProducerConfig(): Map<String, Any> {
        val props: MutableMap<String, Any> = mutableMapOf()
        props[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = boostrapServers
        props[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        props[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = JsonSerializer::class.java
        return props
    }

    @Bean
    fun consumerFactory(): ConsumerFactory<String, String> {
        val props = generateConsumerConfig()
        return DefaultKafkaConsumerFactory(props)
    }

    private fun generateConsumerConfig(): Map<String, Any> {
        val props: MutableMap<String, Any> = mutableMapOf()
        props[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = boostrapServers
        props[ConsumerConfig.GROUP_ID_CONFIG] = groupId
        props[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
        props[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
        props[ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG] = true
        return props
    }

    @Bean
    fun kafkaTemplate(
        producerFactory: ProducerFactory<String, InternalApiCallEvent>
    ): KafkaTemplate<String, InternalApiCallEvent> {
        return KafkaTemplate(producerFactory)
    }
}