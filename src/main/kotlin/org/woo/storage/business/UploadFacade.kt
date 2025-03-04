package org.woo.storage.business

import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.gridfs.ReactiveGridFsTemplate
import org.springframework.stereotype.Service

@Service
class UploadFacade(
    private val gridFsTemplate: ReactiveGridFsTemplate,
    private val mongoTemplate: ReactiveMongoTemplate,
) {
}