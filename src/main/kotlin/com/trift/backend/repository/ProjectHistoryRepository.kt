package com.trift.backend.repository

import com.trift.backend.domain.ProjectHistory
import org.springframework.data.jpa.repository.JpaRepository

interface ProjectHistoryRepository : JpaRepository<ProjectHistory, Long>