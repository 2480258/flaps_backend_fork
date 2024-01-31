package com.trift.backend.web.dto

import jakarta.validation.constraints.Future
import jakarta.validation.constraints.Size

data class ProjectCreationRequest constructor(
    val name: String,
    val countryId: Long,
    val startTimeStamp: Long,
    val endTimeStamp: Long
)


data class ProjectDeletionRequest constructor(val projectId: Long)