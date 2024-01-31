package com.trift.backend.domain

import com.querydsl.core.annotations.QueryProjection
import java.util.Date

data class PlaceBrief @QueryProjection constructor(
    val placeId: Long,
    val isLiked: Boolean,
    val name: String?,
    val thumbnail: String?,
    val description: String?,
    val type: String?,
    val latitude: Double?,
    val longitude: Double?,
    val rating: Double?,
    val reviewPerScore: Map<String, Any>?,
    val added_date: Date?
)