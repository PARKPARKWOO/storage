package org.woo.storage.application.facade

import dto.UserContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.asFlux
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.core.io.buffer.DefaultDataBufferFactory
import org.springframework.stereotype.Service
import org.woo.storage.adapter.out.persistence.mysql.MetadataTypeRepository
import org.woo.storage.application.AccessControlService
import org.woo.storage.application.FileDocumentService
import org.woo.storage.application.ShortUrlService
import org.woo.storage.application.factory.MetadataFactory
import org.woo.storage.domain.metadata.ContentType
import org.woo.storage.domain.metadata.Metadata
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import reactor.kotlin.core.publisher.toFlux

@Service
class RetrieveFacade(
    private val fileDocumentService: FileDocumentService,
    private val metadataFactory: MetadataFactory,
    private val metadataTypeRepository: MetadataTypeRepository,
    private val accessControlService: AccessControlService,
) {
    suspend fun retrieveResource(id: Long, userContext: UserContext?): Pair<Flux<DataBuffer>, Metadata> = coroutineScope {
//        val resourceId = shortUrlService.getResourceId(path)
        val metadataType = metadataTypeRepository.findById(id).awaitSingle()
        val contentType = ContentType.valueOf(metadataType.type)

        val handler = metadataFactory.getHandler(contentType)

        val metadata = handler.get(id)
        accessControlService.verifyAccess(userContext, metadata)
        val dataBufferFlow = flow {
            for (chunkIndex in 0 until metadata.pageSize) {
                val resource = fileDocumentService.findById(id, chunkIndex)
                val buffers = DataBufferUtils.read(resource, DefaultDataBufferFactory(), metadata.chunkSize)
//                    .collectList().awaitSingle()
                emitAll(buffers.asFlow())
            }
        }.flowOn(Dispatchers.IO)

        val dataBufferFlux = dataBufferFlow.asFlux()
        Pair(dataBufferFlux, metadata)
    }
}