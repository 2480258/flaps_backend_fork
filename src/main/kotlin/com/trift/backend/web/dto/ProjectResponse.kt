package com.trift.backend.web.dto

data class ProjectResponse constructor(
    val name: String,
    val thumbnail: String,
    val projectId:Long,
    val cityId: Long,
    val startTimestamp: Long,
    val endTimestamp: Long,
    val creationTimestamp: Long
)