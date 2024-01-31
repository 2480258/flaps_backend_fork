package com.trift.backend.web.dto

data class CountryCreateRequest(
    val name: String,
    val thumbnail: String,
    val isAvailable: Boolean,
    val latitude: Double,
    val longitude: Double
)