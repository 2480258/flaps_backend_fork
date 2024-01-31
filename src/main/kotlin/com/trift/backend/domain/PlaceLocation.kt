package com.trift.backend.domain

import com.querydsl.core.annotations.QueryProjection

data class PlaceLocation @QueryProjection constructor(
    val placeId: Long,
    val latitude: Double?,
    val longitude: Double?
)