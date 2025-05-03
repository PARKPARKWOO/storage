package org.woo.storage.application.exception

open class BusinessException(
    override val message: String,
): RuntimeException(message) {
}