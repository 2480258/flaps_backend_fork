package com.trift.backend.repository

import com.trift.backend.domain.GraphHistory
import org.springframework.data.jpa.repository.JpaRepository

interface GraphHistoryRepository: JpaRepository<GraphHistory, Long> {
}