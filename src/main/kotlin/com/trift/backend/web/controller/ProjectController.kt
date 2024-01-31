package com.trift.backend.web.controller

import com.trift.backend.domain.ProjectCopyMask
import com.trift.backend.security.AccessToken
import com.trift.backend.service.ProjectCreation
import com.trift.backend.service.ProjectInfo
import com.trift.backend.service.ProjectService
import com.trift.backend.web.dto.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.security.SecurityScheme
import jakarta.validation.constraints.Max
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("api/v1")
@SecurityScheme(type = SecuritySchemeType.HTTP, scheme = "bearer", name = "jwtAuth")
class ProjectController {
    @Autowired
    lateinit var projectService: ProjectService


    @Operation(security = [SecurityRequirement(name = "jwtAuth")])
    @PostMapping("/project")
    fun createProject(@RequestBody projectCreationRequest: ProjectCreationRequest): ResponseEntity<CommonResponse<ProjectInfo>> {
        val project = projectService.createProject(
            ProjectCreation(
                projectCreationRequest.name,
                projectCreationRequest.countryId,
                projectCreationRequest.startTimeStamp,
                projectCreationRequest.endTimeStamp
            )
        )

        val entity = ResponseEntity.status(HttpStatus.CREATED)
            .body(CommonResponse(HttpStatus.CREATED.value(), "프로젝트 생성 완료", 0, ProjectInfo.from(project)))

        return entity
    }

    @Operation(security = [SecurityRequirement(name = "jwtAuth")])
    @GetMapping("/project/{projectId}")
    fun getProject(@PathVariable projectId: Long): ResponseEntity<CommonResponse<ProjectInfo>> {
        val project = projectService.findReadOnlyProjectInfo(projectId)

        val entity = ResponseEntity.status(HttpStatus.OK)
            .body(CommonResponse(HttpStatus.OK.value(), "프로젝트 조회 완료", 0, project))

        return entity
    }

    @Operation(security = [SecurityRequirement(name = "jwtAuth")])
    @GetMapping("/project")
    fun listProject(
        @RequestParam page: Int,
        @RequestParam @Max(20) size: Int
    ): ResponseEntity<CommonResponse<Page<ProjectInfo>>> {
        val pageable = PageRequest.of(page, size)
        val projects = projectService.listProject(pageable)
        val page = PageImpl(projects.data, pageable, projects.total) as Page<ProjectInfo>

        val entity = ResponseEntity.status(HttpStatus.OK)
            .body(CommonResponse(HttpStatus.OK.value(), "프로젝트 리스트 조회 완료", 0, page))

        return entity
    }

    @Operation(security = [SecurityRequirement(name = "jwtAuth")])
    @DeleteMapping("/project")
    fun deleteProject(@RequestBody request: ProjectDeletionRequest): ResponseEntity<CommonResponse<ProjectInfo>> {
        val project = projectService.deleteProject(request.projectId)

        val entity = ResponseEntity.status(HttpStatus.CREATED)
            .body(CommonResponse(HttpStatus.CREATED.value(), "프로젝트 삭제 완료", 0, project))

        return entity
    }

    @Operation(security = [SecurityRequirement(name = "jwtAuth")])
    @PostMapping("/project/convey")
    fun transferProject(@RequestBody request: ProjectConveyRequest): ResponseEntity<CommonResponse<ProjectConveyResponse>> {
        val project = projectService.transferProjectTo(AccessToken(request.user))

        val entity = ResponseEntity.status(HttpStatus.CREATED)
            .body(CommonResponse(HttpStatus.CREATED.value(), "프로젝트 이동 완료", 0, ProjectConveyResponse(project)))

        return entity
    }

    @Operation(security = [SecurityRequirement(name = "jwtAuth")])
    @PostMapping("/project/clone")
    fun copyProject(@RequestBody request: ProjectCopyRequest): ResponseEntity<CommonResponse<ProjectInfo>> {
        val intMap = request.date.entries.associateBy({ it.value.toInt() - 1 }) { it.key.toInt() - 1 }

        val project = projectService.copyProject(
            request.projectId,
            request.projectName,
            AccessToken(request.token),
            ProjectCopyMask(intMap),
            request.startAt,
            request.endAt
        )

        val entity = ResponseEntity.status(HttpStatus.CREATED)
            .body(CommonResponse(HttpStatus.CREATED.value(), "프로젝트 복사 완료", 0, project))

        return entity
    }
}