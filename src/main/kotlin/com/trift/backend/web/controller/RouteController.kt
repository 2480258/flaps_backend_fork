package com.trift.backend.web.controller

import com.trift.backend.service.GoogleRouteMode
import com.trift.backend.service.GoogleRouteQOSService
import com.trift.backend.web.dto.*
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
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException


@RestController
@RequestMapping("api/v1")
@SecurityScheme(type = SecuritySchemeType.HTTP, scheme = "bearer", name = "jwtAuth")
class RouteController {

    @Autowired
    lateinit var googleRouteQOSService: GoogleRouteQOSService

    @Operation(security = [SecurityRequirement(name = "jwtAuth")])
    @GetMapping("route/left")
    fun getLeftToken() : ResponseEntity<CommonResponse<LeftTokenResponse>> {
        val leftToken = googleRouteQOSService.leftToken()
        val result = LeftTokenResponse(leftToken)


        val entity = ResponseEntity.status(HttpStatus.OK)
            .body(CommonResponse(HttpStatus.OK.value(), "남은 토큰 계산 완료", 0, result))

        return entity
    }

    @Operation(security = [SecurityRequirement(name = "jwtAuth")])
    @PostMapping("route/drive")
    fun optimizeRouteDrive(@RequestBody request: RouteRequest) : ResponseEntity<CommonResponse<RouteResponse>> {
        if(request.placeIds.size < 4) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "최적화 시 장소는 4개 이상이여야만 합니다.")
        }

        val start = request.placeIds.first()
        val end = request.placeIds.last()

        val intermediate = request.placeIds.subList(1, request.placeIds.size - 1)

        val optimizedRoute = googleRouteQOSService.sendRequestWithQOS(start, end, intermediate, GoogleRouteMode.DRIVE)

        val result = RouteResponse(optimizedRoute.routes.map {
            RouteResponseItem(it.from, it.to, it.duration, it.distance)
        })

        val entity = ResponseEntity.status(HttpStatus.OK)
            .body(CommonResponse(HttpStatus.OK.value(), "경로 탐색 완료(DRIVE)", 0, result))

        return entity
    }

    @Operation(security = [SecurityRequirement(name = "jwtAuth")])
    @PostMapping("route/walk")
    fun optimizeRouteWalk(@RequestBody request: RouteRequest) : ResponseEntity<CommonResponse<RouteResponse>> {
        val start = request.placeIds.first()
        val end = request.placeIds.last()

        val intermediate = request.placeIds.subList(1, request.placeIds.size - 1)

        val optimizedRoute = googleRouteQOSService.sendRequestWithQOS(start, end, intermediate, GoogleRouteMode.WALK)

        val result = RouteResponse(optimizedRoute.routes.map {
            RouteResponseItem(it.from, it.to, it.duration, it.distance)
        })

        val entity = ResponseEntity.status(HttpStatus.OK)
            .body(CommonResponse(HttpStatus.OK.value(), "경로 탐색 완료(WALK)", 0, result))

        return entity
    }
}