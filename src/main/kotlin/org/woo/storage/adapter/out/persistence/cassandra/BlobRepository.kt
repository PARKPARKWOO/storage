package org.woo.storage.adapter.out.persistence.cassandra

import org.springframework.data.cassandra.repository.ReactiveCassandraRepository
import org.woo.storage.domain.file.FileChunk

interface BlobRepository : ReactiveCassandraRepository<FileChunk, String>{
}