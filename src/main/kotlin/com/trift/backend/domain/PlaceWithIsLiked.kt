package com.trift.backend.domain

data class PlaceWithIsLiked constructor(
    val place: Place,
    val isLiked: Boolean
)