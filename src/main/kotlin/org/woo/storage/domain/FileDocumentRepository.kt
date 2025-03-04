package org.woo.storage.domain

import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.woo.storage.domain.file.FileDocument

interface FileDocumentRepository : ReactiveMongoRepository<FileDocument, String>