package com.trift.backend.web.dto

data class RouteResponse(val routes: List<RouteResponseItem>)

data class RouteResponseItem(val from: Long, val to: Long, val time: String, val distance: Double)