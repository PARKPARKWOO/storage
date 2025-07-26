package org.woo.storage.application.event

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.event.EventListener
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.stereotype.Component
import org.woo.apm.log.log
import org.woo.storage.application.FileDocumentService
import org.woo.storage.application.facade.FileDeleteFacade

@Component
class FileEventListener(
    @Qualifier("fileEventTaskExecutor")
    private val executor: ThreadPoolTaskExecutor,
    private val deleteFacade: FileDeleteFacade,
) {
    private val scope = CoroutineScope(
        executor.asCoroutineDispatcher() + SupervisorJob()
    )

    @EventListener
    fun listen(event: FileDeleteEvent) {
        scope.launch {
            log().info("Deleting ${event.fileId}")
            deleteFacade.delete(event.fileId)
        }
    }
}