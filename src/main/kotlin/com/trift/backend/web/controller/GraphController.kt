package com.trift.backend.web.controller

import com.trift.backend.domain.DailyGraph
import com.trift.backend.service.GraphService
import com.trift.backend.web.dto.CommonResponse
import com.trift.backend.web.dto.CommonResponseWithoutData
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.security.SecurityScheme
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("api/v1")
@SecurityScheme(type = SecuritySchemeType.HTTP, scheme = "bearer", name = "jwtAuth")
class GraphController(
    val graphService: GraphService
) {
    @Operation(security = [SecurityRequirement(name = "jwtAuth")])
    @GetMapping("graph")
    fun getGraph(@RequestParam projectId: Long, @RequestParam nthDay: Int): ResponseEntity<CommonResponse<DailyGraph>> {
        val graph = graphService.getDailyGraph(projectId, nthDay)

        return ResponseEntity.status(HttpStatus.OK)
            .body(CommonResponse(HttpStatus.OK.value(), "여행 그래프 조회 완료", 0, graph))
    }

    @Operation(security = [SecurityRequirement(name = "jwtAuth")])
    @PostMapping("graph")
    fun updateGraph(@RequestParam projectId: Long, @RequestParam nthDay: Int, @RequestBody dailyGraph: DailyGraph): ResponseEntity<CommonResponseWithoutData> {
        graphService.updateDailyGraph(projectId, nthDay, dailyGraph)

        return ResponseEntity.status(HttpStatus.NO_CONTENT)
            .body(CommonResponseWithoutData(HttpStatus.NO_CONTENT.value(), "여행 그래프 업데이트 완료", 0))
    }
}