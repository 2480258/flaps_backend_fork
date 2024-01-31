package com.trift.backend.service

import com.trift.backend.domain.Project
import com.trift.backend.security.TokenDTO
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

interface ProjectAuthService {
    fun validateReadOnlyPermission(tokenDTO: TokenDTO, project: Project)

    fun validateReadWritePermission(tokenDTO: TokenDTO, project: Project)

    fun validateOwnerPermission(tokenDTO: TokenDTO, project: Project)
}

@Component
class ProjectAuthServiceImpl : ProjectAuthService {
    @Transactional(propagation = Propagation.MANDATORY)
    override fun validateReadOnlyPermission(tokenDTO: TokenDTO, project: Project) {
        val ownerId = project.owner!!.userId
        val projectId = project.projectId

        if ((ownerId == tokenDTO.userId) or (projectId == tokenDTO.projectId)) {
            return
        } else {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "권한 없음")
        }
    }

    @Transactional(propagation = Propagation.MANDATORY)
    override fun validateReadWritePermission(tokenDTO: TokenDTO, project: Project) {
        val ownerId = project.owner!!.userId

        if ((ownerId == tokenDTO.userId) or (tokenDTO.getProjectIdIfWritableOrNull() == project.projectId!!)) {
            return
        } else {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "권한 없음")
        }
    }

    override fun validateOwnerPermission(tokenDTO: TokenDTO, project: Project) {
        val ownerId = project.owner!!.userId

        if ((ownerId == tokenDTO.userId)) {
            return
        } else {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "권한 없음")
        }
    }


}