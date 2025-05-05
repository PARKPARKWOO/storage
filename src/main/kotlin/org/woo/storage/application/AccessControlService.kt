package org.woo.storage.application

import dto.Passport
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

    suspend fun verifyAccess(passport: Passport?, metadata: Metadata) {
        val accessLevel = metadata.accessLevel
        if (accessLevel == PUBLIC_ACCESS) return
        if (passport == null) throw AccessDeniedException("access denied is not public resource")
        if (passport.role == Role.ROLE_ADMIN) {
            return
        }

        val resourceApplicationId = metadata.applicationId
        val userId = passport.userId
        val userContext = passport.requireUserContext()
        val isUploadedBy = userId.toString() == metadata.uploadedBy
        val hasPermission = resourceApplicationId == passport.signInApplicationId
                && userContext.accessLevel >= metadata.accessLevel
        if (isUploadedBy || hasPermission) return
        throw AccessDeniedException("access denied ${metadata.fileId} for $userId")
    }
}