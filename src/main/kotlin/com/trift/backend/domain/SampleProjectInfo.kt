package com.trift.backend.domain

import com.querydsl.core.annotations.QueryProjection

data class SampleProjectInfo @QueryProjection constructor(
    val projectId: Long,
    val project: Project,
    val extraInfo: Map<String, Any?>
)