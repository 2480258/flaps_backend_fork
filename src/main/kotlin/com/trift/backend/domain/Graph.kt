package com.trift.backend.domain

import io.hypersistence.utils.hibernate.type.json.JsonType
import jakarta.persistence.*
import org.hibernate.annotations.Type
import java.util.*

@Entity
@Table(
    indexes = [Index(name = "i_project_nthDay", columnList = "nthDay, projectId")], uniqueConstraints =
    [UniqueConstraint(columnNames = arrayOf("nthDay", "projectId"))]
)
class Graph(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "projectId", nullable = false)
    val project: Project,

    @Column(nullable = false)
    val nthDay: Int,

    @Column(nullable = false, columnDefinition = "json")
    @Type(value = JsonType::class)
    var graphNodes: List<Any>,

    @Column(nullable = false, columnDefinition = "json")
    @Type(value = JsonType::class)
    var graphEdges: List<Any>,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var status: Status
) : BaseEntity() {
    enum class Status {
        OK, EXPIRED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    val graphId: Long? = null
}