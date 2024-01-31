package com.trift.backend.repository

import com.querydsl.core.types.dsl.BooleanExpression
import com.trift.backend.domain.Graph
import com.trift.backend.domain.Project
import com.trift.backend.domain.QGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport

interface GraphRepository: JpaRepository<Graph, Long>, GraphRepositoryCustom {
    fun findAllByProject(project: Project): List<Graph>
}

interface GraphRepositoryCustom {
    fun findByProjectAndNthDay(project: Project, nthDay: Int): Graph?

}

class GraphRepositoryImpl: GraphRepositoryCustom, QuerydslRepositorySupport(Graph::class.java) {
    val graph: QGraph = QGraph.graph



    override fun findByProjectAndNthDay(project: Project, nthDay: Int): Graph? {
        return from(graph)
            .where(graph.project.projectId.eq(project.projectId), nthDayEq(nthDay), available())
            .select(graph)
            .fetchOne()
    }

    private fun nthDayEq(nthDay: Int?): BooleanExpression? {
        return if(nthDay != null) graph.nthDay.eq(nthDay) else null
    }

    private fun available(): BooleanExpression? {
        return graph.status.eq(Graph.Status.OK)
    }
}