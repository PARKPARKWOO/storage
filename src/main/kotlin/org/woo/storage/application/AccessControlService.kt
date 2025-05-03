package org.woo.storage.application

import dto.UserContext
import model.Role
import org.springframework.stereotype.Service
import org.woo.storage.application.exception.AccessDeniedException
import org.woo.storage.domain.metadata.Metadata

@Service
class AccessControlService {
    companion object {
        const val PUBLIC_ACCESS = 0
    }

    suspend fun verifyAccess(userContext: UserContext?, metadata: Metadata) {
        val accessLevel = metadata.accessLevel
        if (accessLevel == PUBLIC_ACCESS) return
        if (userContext == null) throw AccessDeniedException("access denied is not public resource")
        if (userContext.role == Role.ROLE_ADMIN) {
            return
        }

        val resourceApplicationId = metadata.applicationId
        val userId = userContext.userId

        val isUploadedBy = userId.toString() == metadata.uploadedBy
        val hasPermission = resourceApplicationId == userContext.signInApplicationId
                && userContext.accessLevel >= metadata.accessLevel
        if (isUploadedBy || hasPermission) return
        throw AccessDeniedException("access denied ${metadata.fileId} for $userId")
    }
}