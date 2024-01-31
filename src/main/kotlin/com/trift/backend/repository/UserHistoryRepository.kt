package com.trift.backend.repository

import com.trift.backend.domain.TriftUserHistory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

interface UserHistoryRepository : JpaRepository<TriftUserHistory, Long> {
    @Transactional(propagation = Propagation.MANDATORY)
    fun findByuserHistoryId(userId: Long): TriftUserHistory?
}