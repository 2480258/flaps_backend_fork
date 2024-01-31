package com.trift.backend.web.dto

data class SampleProjectCreationRequest constructor(
    val project: ProjectCreationRequest,
    val extraInfo: Map<String, Any?>
)