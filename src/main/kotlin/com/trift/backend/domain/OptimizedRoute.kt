package com.trift.backend.domain

data class OptimizedRoute(val routes: List<OptimizedRouteItem>)

data class OptimizedRouteItem(val from: Long, val to: Long, val distance: Double, val duration: String)