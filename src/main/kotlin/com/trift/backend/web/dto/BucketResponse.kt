package com.trift.backend.web.dto

import com.trift.backend.domain.PlaceBrief

data class BucketResponse constructor(val bucket: List<PlaceBrief>)