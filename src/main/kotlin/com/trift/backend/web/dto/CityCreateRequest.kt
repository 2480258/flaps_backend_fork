package com.trift.backend.web.dto

data class CityCreateRequest(
    val name: String,
    val thumbnail: String,
    val longitude: Double,
    val latitude: Double,
    val isAvailable: Boolean,
    val country: Long
    )