package org.woo.storage.adapter.`in`.kafka

import kotlinx.coroutines.runBlocking
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component
import org.woo.apm.log.log
import org.woo.storage.application.event.FileDeleteEvent
import org.woo.storage.application.facade.FileDeleteFacade

@Component
class DeleteFileConsumer(
    val deleteFacade: FileDeleteFacade,
) {
    @KafkaListener(topics = ["\${topics.file.delete}"])
    fun listen(@Payload event: FileDeleteEvent) {
        runBlocking {
            log().info("Deleting file ${event.fileId}")
            deleteFacade.delete(event.fileId)
        }
    }
}