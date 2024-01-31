package com.trift.backend.web.dto

import com.trift.backend.domain.PlaceBrief

class PlaceBriefResponse constructor(
    val placeId: Long,
    val isLiked: Boolean,
    val addedTimestamp: Long?,
    name: String?,
    thumbnail: String?,
    description: String?,
    type: String?,
    latitude: Double?,
    longitude: Double?,
    rating: Double?,
    reviewPerScore: Map<String, Any>?
) {
    companion object {
        fun from(placeBrief: PlaceBrief): PlaceBriefResponse {
            return PlaceBriefResponse(
                placeBrief.placeId,
                placeBrief.isLiked,
                placeBrief.added_date?.time,
                placeBrief.name,
                placeBrief.thumbnail,
                placeBrief.description,
                placeBrief.type,
                placeBrief.latitude,
                placeBrief.longitude,
                placeBrief.rating,
                placeBrief.reviewPerScore
            )
        }
    }

    val info: PlaceBriefResponseInfo

    init {
        info = PlaceBriefResponseInfo(name, thumbnail, description, type, latitude, longitude, rating, reviewPerScore)
    }

}

data class PlaceBriefResponseInfo constructor(
    val name: String?,
    val thumbnail: String?,
    val description: String?,
    val type: String?,
    val latitude: Double?,
    val longitude: Double?,
    val rating: Double?,
    val reviewPerScore: Map<String, Any>?
)