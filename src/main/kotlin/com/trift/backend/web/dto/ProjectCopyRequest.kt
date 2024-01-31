package com.trift.backend.web.dto

data class ProjectCopyRequest(
    val projectId: Long,
    val projectName: String,
    val token: String,
    val date: Map<String, String>,
    val startAt: Long,
    val endAt: Long
)