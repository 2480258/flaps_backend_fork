package com.trift.backend.web.dto

import com.trift.backend.service.ProjectInfo

data class SampleProjectResponse constructor(val projectId: Long, val token: String, val projectExtraInfo: Map<String, Any?>, val project: ProjectInfo)