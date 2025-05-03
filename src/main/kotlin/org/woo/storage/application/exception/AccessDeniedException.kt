package org.woo.storage.application.exception

class AccessDeniedException(
    override val message: String,
): BusinessException(message) {
}