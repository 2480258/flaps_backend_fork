package com.trift.backend.repository

import com.querydsl.core.QueryResults
import com.trift.backend.domain.QSampleProject
import com.trift.backend.domain.QSampleProjectInfo
import com.trift.backend.domain.SampleProject
import com.trift.backend.domain.SampleProjectInfo
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

interface SampleProjectRepository : SampleProjectRepositoryCustom, JpaRepository<SampleProject, Long> {
}

interface SampleProjectRepositoryCustom {
    fun getSampleByCountry(countyId: Long) : QueryResults<SampleProjectInfo>
}

class SampleProjectRepositoryCustomImpl : SampleProjectRepositoryCustom, QuerydslRepositorySupport(SampleProject::class.java) {
    @Transactional(propagation = Propagation.MANDATORY)
    override fun getSampleByCountry(countyId: Long): QueryResults<SampleProjectInfo> {
        val qSampleProject = QSampleProject.sampleProject

        val result = from(qSampleProject)
            .select(QSampleProjectInfo(qSampleProject.project.projectId, qSampleProject.project, qSampleProject.extraInfo))
            .where(qSampleProject.project.country.countryId.eq(countyId))
            .fetchResults()

        return result
    }
}