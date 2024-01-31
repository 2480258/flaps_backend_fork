package com.trift.backend.repository

import com.querydsl.core.QueryResults
import com.querydsl.core.types.Order
import com.querydsl.core.types.OrderSpecifier
import com.trift.backend.domain.Project
import com.trift.backend.domain.QProject
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional


interface ProjectRepository : ProjectRepositoryCustom, JpaRepository<Project, Long> {

}

interface ProjectRepositoryCustom {
    @Transactional(propagation = Propagation.MANDATORY)
    fun findByProjectId(projectId: Long) : Project?
    @Transactional(propagation = Propagation.MANDATORY)
    fun findProjectInfoByUserId(userId: Long, pageable: Pageable): QueryResults<Project>

    @Transactional(propagation = Propagation.MANDATORY)
    fun findAllProjectIdByUserId(userId: Long): List<Long>
}

class ProjectRepositoryCustomImpl : ProjectRepositoryCustom, QuerydslRepositorySupport(Project::class.java) {
    @Transactional(propagation = Propagation.MANDATORY)
    override fun findByProjectId(projectId: Long) : Project? {
        val qProject = QProject.project

        val result = from(qProject)
            .where(qProject.status.eq(Project.Status.OK))
            .where(qProject.projectId.eq(projectId))
            .fetchOne()


        return result
    }
    @Transactional(propagation = Propagation.MANDATORY)
    override fun findProjectInfoByUserId(userId: Long, pageable: Pageable): QueryResults<Project> {
        val qProject = QProject.project

        val fetch = from(qProject)
            .where(qProject.status.eq(Project.Status.OK))
            .where(qProject.owner.userId.eq(userId))
            .orderBy(OrderSpecifier(Order.ASC, qProject.updateDate))
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetchResults()

        return fetch
    }

    @Transactional(propagation = Propagation.MANDATORY)
    override fun findAllProjectIdByUserId(userId: Long): List<Long> {
        val qProject = QProject.project

        val fetch = from(qProject)
            .select(qProject.projectId)
            .where(qProject.status.eq(Project.Status.OK))
            .where(qProject.owner.userId.eq(userId))
            .fetchResults()

        return fetch.results
    }


}