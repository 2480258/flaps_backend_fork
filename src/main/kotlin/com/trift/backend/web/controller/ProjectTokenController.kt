package com.trift.backend.web.controller

import com.trift.backend.service.LoginService
import com.trift.backend.service.ProjectAuthService
import com.trift.backend.service.ProjectService
import com.trift.backend.web.dto.CommonResponse
import com.trift.backend.web.dto.ProjectTokenResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.security.SecurityScheme
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/v1")
@SecurityScheme(type = SecuritySchemeType.HTTP, scheme = "bearer", name = "jwtAuth")
class ProjectTokenController {

    @Autowired
    lateinit var projectService: ProjectService

    @Operation(security = [SecurityRequirement(name = "jwtAuth")])
    @PostMapping("/project/token/{projectId}")
    fun createToken(@PathVariable projectId: Long): ResponseEntity<CommonResponse<ProjectTokenResponse>> {

        val response = ProjectTokenResponse(projectService.createProjectToken(projectId).accessToken)

        val entity = ResponseEntity(CommonResponse(HttpStatus.CREATED.value(), "프로젝트 토큰 생성", 0, response), HttpStatus.CREATED)
        return entity
    }

    @Operation(security = [SecurityRequirement(name = "jwtAuth")])
    @PostMapping("/project/writetoken/{projectId}")
    fun createWriteToken(@PathVariable projectId: Long): ResponseEntity<CommonResponse<ProjectTokenResponse>> {

        val response = ProjectTokenResponse(projectService.createWriteProjectToken(projectId).accessToken)

        val entity = ResponseEntity(CommonResponse(HttpStatus.CREATED.value(), "프로젝트 토큰 생성", 0, response), HttpStatus.CREATED)
        return entity
    }
}