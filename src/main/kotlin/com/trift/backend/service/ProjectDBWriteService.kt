package com.trift.backend.service

import com.querydsl.core.QueryResults
import com.trift.backend.domain.*
import com.trift.backend.repository.*
import org.hibernate.graph.GraphParser
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.sql.Timestamp
import java.util.*

interface ProjectDBWriteService {
    fun findProject(projectId: Long): Project
    fun transferProject(srcUserId: Long, destUserId: TriftUser) : List<Long>

    fun copyProject(oldproject: Project, projectName: String, destUserId: TriftUser, copyMask: ProjectCopyMask, startAt: Long, endAt: Long) : Project

    fun listProject(userId: Long, pageable: Pageable): QueryResults<Project>

    fun deleteAllProjectByUserId(userId: Long)

    fun deleteProject(projectId: Long): Project

    fun saveProject(project: ProjectCreation, country: Country, user: TriftUser) : Project
}

@Component
class ProjectDBWriteServiceImpl : ProjectDBWriteService {

    @Autowired
    private lateinit var graphRepository: GraphRepository;

    @Autowired
    private lateinit var graphHistoryRepository: GraphHistoryRepository;

    @Autowired
    private lateinit var projectHistoryRepository: ProjectHistoryRepository

    @Autowired
    private lateinit var projectRepository: ProjectRepository

    private val action_CopyProject = "프로젝트 복사"
    private val action_CreateProject = "프로젝트 생성"
    private val action_DeleteProject = "프로젝트 삭제"
    private val action_CreateGraph = "그래프 생성"
    private val action_DeleteGraph = "그래프 삭제"
    private val action_TransferProject = "그래프 소유권 이동"


    @Transactional(propagation = Propagation.MANDATORY)
    override fun findProject(projectId: Long): Project {
        val project = projectRepository.findByProjectId(projectId)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "해당 프로젝트를 찾을 수 없습니다.")

        return project
    }

    @Transactional(propagation = Propagation.MANDATORY)
    override fun transferProject(srcUserId: Long, destUserId: TriftUser): List<Long> {
        val projects = projectRepository.findAllProjectIdByUserId(srcUserId)

        projects.map {
            findProject(it)
        }.forEach {
            it.apply {
                this.owner = destUserId
            }

            projectRepository.save(it)

            val projectHistory = ProjectHistory().apply {
                this.project = it
                this.action = "$action_TransferProject ($srcUserId -> ${destUserId.userId})"
            }

            projectHistoryRepository.save(projectHistory)
        }

        return projects
    }

    @Transactional(propagation = Propagation.MANDATORY)
    override fun copyProject(oldproject: Project, projectName: String, destUserId: TriftUser, copyMask: ProjectCopyMask, startAt: Long, endAt: Long): Project {
        var newProject = Project().apply {
            this.name = projectName
            this.owner = destUserId

            this.status = Project.Status.OK

            this.startAt = Date(Timestamp(startAt).time)
            this.endAt = Date(Timestamp(endAt).time)

            this.country = oldproject.country

            this.status = Project.Status.OK
        }

        val projectHistory = ProjectHistory().apply {
            this.project = newProject
            this.action = action_CopyProject
        }

        newProject = projectRepository.saveAndFlush(newProject) // flush to create CreationTimeStamp

        val graphList = (0.. getDateDiff(newProject)).map { nthDay ->
            if(copyMask.findMappedNthDay(nthDay) == null) {
                Graph(
                    project = newProject,
                    nthDay = nthDay,
                    graphNodes = listOf(),
                    graphEdges = listOf(),
                    status = Graph.Status.OK
                )
            } else {
                val oldGraph = graphRepository.findByProjectAndNthDay(oldproject, copyMask.findMappedNthDay(nthDay)!!) ?: throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Referenced a non-exist graph during copyproject()")

                Graph(
                    project = newProject,
                    nthDay = nthDay,
                    graphNodes = oldGraph.graphNodes,
                    graphEdges = oldGraph.graphEdges,
                    status = Graph.Status.OK
                )
            }
        }

        val graphHistoryList = graphList.map { graph -> GraphHistory(
            graph = graph,
            action = action_CreateGraph,
            graphEdges = graph.graphEdges,
            graphNodes = graph.graphNodes
        ) }


        projectHistoryRepository.save(projectHistory)
        graphRepository.saveAll(graphList)
        graphHistoryRepository.saveAll(graphHistoryList)

        return newProject
    }


    @Transactional(propagation = Propagation.MANDATORY)
    override fun listProject(userId: Long, pageable: Pageable): QueryResults<Project> {
        return projectRepository.findProjectInfoByUserId(userId, pageable)
    }

    @Transactional(propagation = Propagation.MANDATORY)
    override fun deleteAllProjectByUserId(userId: Long) {
        val id = projectRepository.findAllProjectIdByUserId(userId)
        deleteAllProject(id)
    }

    private fun deleteAllProject(projectIds: List<Long>) {
        projectIds.forEach {
            deleteProject(it)
        }
    }

    @Transactional(propagation = Propagation.MANDATORY)
    override fun deleteProject(projectId: Long): Project {
        val project = projectRepository.findByProjectId(projectId)
        project!!.status = Project.Status.EXPIRED

        val projectHistory = ProjectHistory().apply {
            this.project = project
            this.action = action_DeleteProject
        }

        val graphList = graphRepository.findAllByProject(project).map { graph ->
            graph.status = Graph.Status.EXPIRED
            graph
        }
        val graphHistoryList = graphList.map { graph -> GraphHistory(
            graph = graph,
            action = action_DeleteGraph,
        )}

        projectHistoryRepository.save(projectHistory)
        graphRepository.saveAll(graphList)
        graphHistoryRepository.saveAll(graphHistoryList)

        return project
    }


    @Transactional(propagation = Propagation.MANDATORY)
    override fun saveProject(
        project: ProjectCreation,
        country: Country,
        user: TriftUser
    ): Project {
        var newProject = Project().apply {
            this.name = project.name
            this.owner = user
            this.status = Project.Status.OK
            this.startAt = Date(Timestamp(project.startTimeStamp).time)
            this.endAt = Date(Timestamp(project.endTimeStamp).time)
            this.country = country
        }

        val projectHistory = ProjectHistory().apply {
            this.project = newProject
            this.action = action_CreateProject
        }

        val dateDiff = getDateDiff(newProject)
        val graphList = (0.. dateDiff).toList().map { nthDay -> Graph(
            project = newProject,
            nthDay = nthDay,
            graphNodes = listOf(),
            graphEdges = listOf(),
            status = Graph.Status.OK
        ) }
        val graphHistoryList = graphList.map { graph -> GraphHistory(
            graph = graph,
            action = action_CreateGraph,
            graphEdges = graph.graphEdges,
            graphNodes = graph.graphNodes
        ) }

        newProject = projectRepository.saveAndFlush(newProject) // flush to create CreationTimeStamp
        projectHistoryRepository.save(projectHistory)
        graphRepository.saveAll(graphList)
        graphHistoryRepository.saveAll(graphHistoryList)

        return newProject
    }

    private fun getDateDiff(newProject: Project) =
        ((newProject.endAt!!.time - newProject.startAt!!.time) / (1000 * 60 * 60 * 24)).toInt()
}