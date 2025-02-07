package org.woo.storage

import org.springframework.data.mongodb.repository.ReactiveMongoRepository

interface FileDocumentRepository : ReactiveMongoRepository<FileDocument, String>