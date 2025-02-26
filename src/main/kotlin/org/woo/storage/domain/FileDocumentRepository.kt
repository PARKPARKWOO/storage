package org.woo.storage.domain

import org.springframework.data.mongodb.repository.ReactiveMongoRepository

interface FileDocumentRepository : ReactiveMongoRepository<FileDocument, String>