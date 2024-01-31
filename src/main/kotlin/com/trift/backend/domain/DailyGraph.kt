package com.trift.backend.domain

data class DailyGraph(
    val nthDay: Int,
    val graphPlaceNodes: List<Any>,
    val graphPlaceEdges: List<Any>
)

data class Node (
    val id: String,
    val type: String,
    val position: NodePosition,
    val data: Any
)

data class NodePosition (
    val x: Number,
    val y: Number
)