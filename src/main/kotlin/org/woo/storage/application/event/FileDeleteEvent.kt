package org.woo.storage.application.event

data class FileDeleteEvent(
    val fileId: Long,
    val fileOriginName: String,
)
