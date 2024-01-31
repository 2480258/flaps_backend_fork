package com.trift.backend.domain

import com.querydsl.core.annotations.QueryProjection

data class GooglePlaceId @QueryProjection constructor(val placeId: Long, val googlePlaceId: String)