package com.trift.backend.web.controller

import com.trift.backend.service.DirectionService
import com.trift.backend.web.dto.CommonResponseWithGeoJsonObject
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.security.SecurityScheme
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/v1")
@SecurityScheme(type = SecuritySchemeType.HTTP, scheme = "bearer", name = "jwtAuth")
class DirectionController(
    val directionService: DirectionService
) {

    @Operation(security = [SecurityRequirement(name = "jwtAuth")])
    @GetMapping("direction")
    fun getDirection(
        @RequestParam startPlaceId: Long,
        @RequestParam endPlaceId: Long,
        @RequestParam(defaultValue = "driving-car") profile: String
    ): ResponseEntity<CommonResponseWithGeoJsonObject> {

        val direction = directionService.getDirection(startPlaceId, endPlaceId, profile)

        return ResponseEntity.status(HttpStatus.OK)
            .body(
                CommonResponseWithGeoJsonObject(
                    resultCode = HttpStatus.OK.value(),
                    message = "경로 조회 완료",
                    logId = 0,
                    data = direction
                )
            )
    }
}