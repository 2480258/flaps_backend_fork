package com.trift.backend.service

import com.trift.backend.domain.SampleProject
import com.trift.backend.domain.SampleProjectInfo
import com.trift.backend.repository.SampleProjectRepository
import com.trift.backend.security.AccessToken
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional

data class SampleProjectInfoWithToken constructor(val info: SampleProjectInfo, val token: AccessToken)

interface SampleProjectService {
    fun getSampleByCounty(countyId: Long): List<SampleProjectInfoWithToken>

    fun saveSampleProject(
        projectCreation: ProjectCreation,
        extraInfo: Map<String, Any?>
    ): SampleProjectInfoWithToken
}

@Component
class SampleProjectServiceImpl : SampleProjectService {

    @Autowired
    private lateinit var sampleProjectRepository: SampleProjectRepository

    @Autowired
    private lateinit var projectService: ProjectService

    @Transactional(isolation = Isolation.SERIALIZABLE)
    override fun getSampleByCounty(countyId: Long): List<SampleProjectInfoWithToken> {
        val projects = sampleProjectRepository.getSampleByCountry(countyId)


        return projects.results.map {
            val token = projectService.createProjectTokenWithSkipPermissionCheck(it.projectId)

            SampleProjectInfoWithToken(it, token)
        }
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    override fun saveSampleProject(
        projectCreation: ProjectCreation,
        extraInfo: Map<String, Any?>
    ): SampleProjectInfoWithToken {

        val project = projectService.createProject(projectCreation)
        val sampleProject = sampleProjectRepository.save(SampleProject().apply {
            this.project = project
            this.extraInfo = extraInfo
        })

        return SampleProjectInfoWithToken(
            SampleProjectInfo(
                sampleProject.project!!.projectId!!,
                sampleProject.project!!,
                sampleProject.extraInfo!!
            ), projectService.createProjectTokenWithSkipPermissionCheck(sampleProject.project!!.projectId!!)
        )
    }
}