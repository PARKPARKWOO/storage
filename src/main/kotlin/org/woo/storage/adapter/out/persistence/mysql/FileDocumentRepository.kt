package org.woo.storage.adapter.out.persistence.mysql

import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.woo.storage.domain.file.FileChunk

interface FileDocumentRepository : ReactiveMongoRepository<FileChunk, String>