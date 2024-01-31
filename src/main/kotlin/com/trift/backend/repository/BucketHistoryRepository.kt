package com.trift.backend.repository

import com.trift.backend.domain.BucketHistory
import org.springframework.data.jpa.repository.JpaRepository

interface BucketHistoryRepository : JpaRepository<BucketHistory, Long> {
}