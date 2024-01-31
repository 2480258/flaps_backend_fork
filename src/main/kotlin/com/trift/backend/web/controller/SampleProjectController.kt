package com.trift.backend.web.controller

import com.trift.backend.service.ProjectInfo
import com.trift.backend.service.SampleProjectService
import com.trift.backend.web.dto.CommonResponse
import com.trift.backend.web.dto.SampleProjectResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.security.SecurityScheme
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController




@RestController
@RequestMapping("api/v1")
@SecurityScheme(type = SecuritySchemeType.HTTP, scheme = "bearer", name = "jwtAuth")
class SampleProjectController {

    @Autowired
    lateinit var sampleProjectService: SampleProjectService


    @Operation(security = [SecurityRequirement(name = "jwtAuth")])
    @GetMapping("/samples/{countryId}")
    fun listSampleProject(@PathVariable countryId: Long) : ResponseEntity<CommonResponse<List<SampleProjectResponse>>> {
        val samples = sampleProjectService.getSampleByCounty(countryId)

        val entity = ResponseEntity.status(HttpStatus.OK)
            .body(CommonResponse(HttpStatus.OK.value(), "샘플 프로젝트 리스트 조회 완료", 0, samples.map {
                SampleProjectResponse(it.info.projectId, it.token.accessToken, it.info.extraInfo, ProjectInfo.from(it.info.project))
            }))

        return entity
    }

}