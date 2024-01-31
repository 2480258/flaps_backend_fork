package com.trift.backend.domain

import io.hypersistence.utils.hibernate.type.json.JsonType
import jakarta.persistence.*
import org.hibernate.annotations.Type

@Entity
class GraphHistory(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "graphId", nullable = false)
    val graph: Graph,

    @Column(nullable = false)
    val action: String,

    @Column(columnDefinition = "json")
    @Type(value = JsonType::class)
    val graphNodes: List<Any>? = null,

    @Column(columnDefinition = "json")
    @Type(value = JsonType::class)
    val graphEdges: List<Any>? = null
): HistoryBaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val graphHistoryId: Long? = null
}