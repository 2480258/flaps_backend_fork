package com.trift.backend.web.dto

import com.fasterxml.jackson.annotation.JsonInclude
import org.geojson.GeoJsonObject


@JsonInclude(JsonInclude.Include.NON_NULL)
data class CommonResponse<T> constructor(val resultCode: Int, val message: String, val logId: Int, val data: T)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class CommonResponseWithGeoJsonObject constructor(val resultCode: Int, val message: String, val logId: Int, val data: GeoJsonObject)

data class CommonResponseWithoutData constructor(val resultCode: Int, val message: String, val logId: Int)