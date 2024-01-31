package com.trift.backend.service

import com.trift.backend.domain.CountryInfo
import com.trift.backend.domain.PageableData
import com.trift.backend.domain.Project
import com.trift.backend.domain.ProjectCopyMask
import com.trift.backend.repository.CountryRepository
import com.trift.backend.repository.UserRepository
import com.trift.backend.security.AccessToken
import com.trift.backend.security.TokenDTO
import com.trift.backend.security.jwt.TokenService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.security.InvalidParameterException
import java.sql.Timestamp
import java.util.*

data class ProjectCreation constructor(
    val name: String,
    val countryId: Long,
    val startTimeStamp: Long,
    val endTimeStamp: Long
)


data class ProjectInfo constructor(
    val name: String,
    val countryId: Long,
    val startTimeStamp: Long,
    val endTimeStamp: Long,
    val creationTimestamp: Long,
    val thumbnail: String,
    val projectId: Long,
    val country: CountryInfo
) {
    companion object {

        fun from(project: Project) = ProjectInfo(
            project.name!!,
            project.country!!.countryId!!,
            project.startAt!!.time,
            project.endAt!!.time,
            project.createDate!!.time,
            project.country!!.thumbnail!!,
            project.projectId!!,
            CountryInfo.from(project.country!!)
        )
    }

}

interface ProjectService {
    @Transactional(isolation = Isolation.SERIALIZABLE)
    fun findReadOnlyProject(projectId: Long): Project

    @Transactional(isolation = Isolation.SERIALIZABLE)
    fun findReadOnlyProjectInfo(projectId: Long): ProjectInfo

    @Transactional(isolation = Isolation.SERIALIZABLE)
    fun findReadWriteProject(projectId: Long): Project

    @Transactional(isolation = Isolation.SERIALIZABLE)
    fun createProject(projectCreation: ProjectCreation): Project

    @Transactional(isolation = Isolation.SERIALIZABLE)
    fun listProject(pageable: Pageable): PageableData<ProjectInfo>

    @Transactional(isolation = Isolation.SERIALIZABLE)
    fun deleteProject(projectId: Long): ProjectInfo

    @Transactional(isolation = Isolation.SERIALIZABLE)
    fun transferProjectTo(target: AccessToken): List<Long>

    @Transactional(isolation = Isolation.SERIALIZABLE)
    fun copyProject(
        projectId: Long,
        projectName: String,
        token: AccessToken,
        copyMask: ProjectCopyMask,
        startAt: Long,
        endAt: Long
    ): ProjectInfo

    fun createProjectToken(projectId: Long): AccessToken

    fun createProjectTokenWithSkipPermissionCheck(projectId: Long): AccessToken

    fun createWriteProjectToken(projectId: Long): AccessToken
}

@Component
class ProjectServiceImpl : ProjectService {
    @Autowired
    private lateinit var authService: AuthService

    @Autowired
    private lateinit var projectAuthService: ProjectAuthService


    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var countryRepository: CountryRepository

    @Autowired
    private lateinit var projectDBWriteService: ProjectDBWriteService

    @Autowired
    private lateinit var loginService: LoginService

    @Autowired
    private lateinit var tokenService: TokenService

    @Transactional(isolation = Isolation.SERIALIZABLE)
    override fun findReadOnlyProject(projectId: Long): Project {
        return findReadOnlyProject(projectId, authService.getLoginUser())
    }

    private fun findReadOnlyProject(projectId: Long, token: TokenDTO): Project {
        val project = projectDBWriteService.findProject(projectId)
        projectAuthService.validateReadOnlyPermission(token, project)

        return project
    }

    override fun findReadOnlyProjectInfo(projectId: Long): ProjectInfo {
        val project = findReadOnlyProject(projectId)
        return ProjectInfo.from(project)
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    override fun findReadWriteProject(projectId: Long): Project {
        val project = projectDBWriteService.findProject(projectId)
        projectAuthService.validateReadWritePermission(authService.getLoginUser(), project)

        return project
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    override fun createProjectToken(projectId: Long): AccessToken {
        val project = projectDBWriteService.findProject(projectId)
        projectAuthService.validateOwnerPermission(authService.getLoginUser(), project)

        return loginService.projectTokenLogin(projectId)
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    override fun createProjectTokenWithSkipPermissionCheck(projectId: Long): AccessToken {
        val project = projectDBWriteService.findProject(projectId)

        return loginService.projectTokenLogin(projectId)
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    override fun createWriteProjectToken(projectId: Long): AccessToken {
        val project = projectDBWriteService.findProject(projectId)
        projectAuthService.validateOwnerPermission(authService.getLoginUser(), project)

        return loginService.projectWriteTokenLogin(projectId)
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    override fun listProject(pageable: Pageable): PageableData<ProjectInfo> {
        val pageOfProjects = projectDBWriteService.listProject(authService.getLoginUser().getUserIdOrThrow(), pageable)

        val projectInfo = pageOfProjects.results.map {
            ProjectInfo.from(it)
        }

        return PageableData(projectInfo, pageOfProjects.total)
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    override fun deleteProject(projectId: Long): ProjectInfo {
        val project = projectDBWriteService.findProject(projectId)
        projectAuthService.validateOwnerPermission(authService.getLoginUser(), project)

        projectDBWriteService.deleteProject(projectId)

        return ProjectInfo.from(project)
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    override fun transferProjectTo(target: AccessToken): List<Long> {
        val destUser = userRepository.findByuserId(authService.getLoginUser().getUserIdOrThrow())
            ?: throw ResponseStatusException(HttpStatus.FORBIDDEN, "잘못된 JWT 토큰입니다. 다시 로그인해주세요. (Dest)")
        val src = userRepository.findByuserId(tokenService.verifyAccessToken(target).getUserIdOrThrow())!!.userId
            ?: throw ResponseStatusException(HttpStatus.FORBIDDEN, "잘못된 JWT 토큰입니다. 다시 로그인해주세요. (Source)")

        if (src == destUser.userId) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "프로젝트를 받을 사용자와 보낼 사용자가 동일합니다.")
        }

        return projectDBWriteService.transferProject(src, destUser)
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    override fun copyProject(
        projectId: Long,
        projectName: String,
        token: AccessToken,
        copyMask: ProjectCopyMask,
        startAt: Long,
        endAt: Long
    ): ProjectInfo {
        val project = findReadOnlyProject(projectId, tokenService.verifyAccessToken(token))
        val user = userRepository.findByuserId(authService.getLoginUser().getUserIdOrThrow())
            ?: throw ResponseStatusException(HttpStatus.FORBIDDEN, "잘못된 JWT 토큰입니다. 다시 로그인해주세요.")

        val oldProjectLength = getDateDiff(project.startAt!!, project.endAt!!)
        val newProjectLength = getDateDiff(Date(Timestamp(startAt).time), Date(Timestamp(endAt).time))

        if (copyMask.getMaxDestProjectReference() > newProjectLength) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "새로 추가되는 프로젝트의 기간과 복사가 언제 일어나는지 확인해주세요. 프로젝트 길이: ${newProjectLength + 1}일, 원본 프로젝트로부터 복사되어질 날짜의 목적지 중 최댓값: ${copyMask.getMaxDestProjectReference() + 1}일차"
            )
        }

        if (copyMask.getMaxSourceProjectReference() > oldProjectLength) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "원본 프로젝트의 기간과 복사가 언제 일어나는지 확인해주세요. 프로젝트 길이: ${oldProjectLength + 1}일, 원본 프로젝트로부터 복사되어질 날짜 중 최댓값: ${copyMask.getMaxSourceProjectReference() + 1}일차"
            )
        }

        return ProjectInfo.from(projectDBWriteService.copyProject(project, projectName, user, copyMask, startAt, endAt))
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    override fun createProject(projectCreation: ProjectCreation): Project {
        val country = countryRepository.findById(projectCreation.countryId).orElseThrow {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "해당하는 국가를 찾을 수 없습니다.")
        }

        val user = userRepository.findByuserId(authService.getLoginUser().getUserIdOrThrow())
            ?: throw ResponseStatusException(HttpStatus.FORBIDDEN, "잘못된 JWT 토큰입니다. 다시 로그인해주세요.")

        if ((projectCreation.endTimeStamp - projectCreation.startTimeStamp) > 1000L * 60 * 60 * 24 * 30) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "프로젝트 기간이 너무 깁니다.")
        }

        if (((projectCreation.name.length) < 1) or (projectCreation.name.length > 15)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "프로젝트 이름이 너무 짧거나 깁니다.")
        }

        val project = projectDBWriteService.saveProject(projectCreation, country, user)

        return project
    }

    private fun getDateDiff(startAt: Date, endAt: Date): Int { //TODO: FIX
        if (endAt <= startAt) {
            throw InvalidParameterException("endAt <= startAt")
        }

        return ((endAt.time - startAt.time) / (1000 * 60 * 60 * 24)).toInt()
    }
}