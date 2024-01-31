package com.trift.backend.domain

import com.querydsl.core.annotations.QueryProjection

data class CountryInfo @QueryProjection constructor(
    val name: String,
    val thumbnail: String,
    val countryId: Long,
    val isAvailable: Boolean,
    val latitude: Double,
    val longitude: Double
) {
    companion object {
        fun from(it: Country) =
            CountryInfo(
                it.name!!,
                it.thumbnail!!,
                it.countryId!!,
                it.isAvailable!!,
                it.latitude!!,
                it.longitude!!

            )
    }
}