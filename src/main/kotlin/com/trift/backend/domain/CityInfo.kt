package com.trift.backend.domain

import com.querydsl.core.annotations.QueryProjection

data class CityInfo @QueryProjection constructor(
    val name: String,
    val thumbnail: String,
    val cityId: Long,
    val longitude: Double,
    val latitude: Double,
    val isAvailable: Boolean,
    val countryId: Long
) {
    companion object {
        fun from(it: City) =
            CityInfo(
                it.name!!,
                it.thumbnail!!,
                it.cityId!!,
                it.longitude!!,
                it.latitude!!,
                it.isAvailable!!,
                it.country!!.countryId!!
            )
    }
}