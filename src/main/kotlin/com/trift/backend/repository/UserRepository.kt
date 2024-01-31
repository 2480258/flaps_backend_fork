package com.trift.backend.repository

import com.trift.backend.domain.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
import org.springframework.http.HttpStatus
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

interface UserRepository : UserRepositoryCustom, JpaRepository<TriftUser, Long> {
    @Transactional(propagation = Propagation.MANDATORY)
    fun findByuserId(userId: Long): TriftUser?
}

interface UserRepositoryCustom {
    @Transactional(propagation = Propagation.MANDATORY)
    fun findByUserEmail(email: String): TriftUser?
}

class UserRepositoryCustomImpl : UserRepositoryCustom, QuerydslRepositorySupport(TriftUser::class.java) {
    @Transactional(propagation = Propagation.MANDATORY)
    override fun findByUserEmail(email: String): TriftUser? {
        val qUser = QTriftUser.triftUser

        val user = from(qUser)
            .where(qUser.status.eq(Status.OK))
            .where(qUser.userEmail.eq(email))
            .fetch()

        if(user.count() > 2) {
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "duplicated user email")
        }

        return user.singleOrNull()
    }
}