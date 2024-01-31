package com.trift.backend.service

import com.trift.backend.domain.*
import com.trift.backend.repository.GraphHistoryRepository
import com.trift.backend.repository.GraphRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

interface GraphService {
    fun getDailyGraph(projectId: Long, nthDay: Int): DailyGraph
    fun updateDailyGraph(projectId: Long, nthDay: Int, payload: DailyGraph)
}

@Service
class GraphServiceImpl (
    val graphRepository: GraphRepository,
    val projectService: ProjectService,
    val graphHistoryRepository: GraphHistoryRepository
): GraphService {

    val action_UpdateGraph = "그래프 수정"

    @Transactional
    override fun getDailyGraph(projectId: Long, nthDay: Int): DailyGraph {
        val project = projectService.findReadOnlyProject(projectId)

        val graph = graphRepository.findByProjectAndNthDay(project, nthDay) ?: throw ResponseStatusException(
            HttpStatus.NOT_FOUND, "Graph를 찾을 수 없음"
        )

        return DailyGraph(
            nthDay = graph.nthDay,
            graphPlaceNodes = graph.graphNodes,
            graphPlaceEdges = graph.graphEdges
        )
    }

    @Transactional
    override fun updateDailyGraph(projectId: Long, nthDay: Int, payload: DailyGraph) {
        val project = projectService.findReadWriteProject(projectId)

        val graph = graphRepository.findByProjectAndNthDay(project, nthDay) ?: throw ResponseStatusException(
            HttpStatus.NOT_FOUND, "Graph를 찾을 수 없음"
        )
        graph.graphNodes = payload.graphPlaceNodes
        graph.graphEdges = payload.graphPlaceEdges

        val graphHistory = GraphHistory(
            graph = graph,
            action = action_UpdateGraph,
            graphEdges = graph.graphEdges,
            graphNodes = graph.graphNodes
        )
        graphHistoryRepository.save(graphHistory)
    }
}