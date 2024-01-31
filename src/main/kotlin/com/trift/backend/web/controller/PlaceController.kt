package com.trift.backend.web.controller

import com.trift.backend.domain.PlaceBrief
import com.trift.backend.repository.PlaceSearch
import com.trift.backend.service.PlaceDetail
import com.trift.backend.service.PlaceService
import com.trift.backend.web.dto.CommonResponse
import com.trift.backend.web.dto.PlaceBriefResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.security.SecurityScheme
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/v1")
@SecurityScheme(type = SecuritySchemeType.HTTP, scheme = "bearer", name = "jwtAuth")
class PlaceController {
    @Autowired
    lateinit var placeService: PlaceService

    @Operation(security = [SecurityRequirement(name = "jwtAuth")])
    @GetMapping("/place/nearby")
    fun getNearByPlace(
        @RequestParam placeId: Long,
        @RequestParam projectId: Long
    ) : ResponseEntity<CommonResponse<List<PlaceBrief>>> {

        val result = placeService.getNearbyPlace(placeId, projectId)
        val entity = ResponseEntity.status(HttpStatus.OK)
            .body(CommonResponse(HttpStatus.OK.value(), "주변 장소 검색 완료", 0, result))

        return entity
    }

    @Operation(security = [SecurityRequirement(name = "jwtAuth")])
    @GetMapping("/place")
    fun getPlacelist(
        @RequestParam(required = false) searchTerm: String?,
        @RequestParam(required = false) category: String?,
        @RequestParam(required = false) city: Long?,
        @RequestParam projectId: Long,
        @RequestParam page: Int,
        @RequestParam size: Int
    ): ResponseEntity<CommonResponse<Page<PlaceBriefResponse>>> {
        val pageable = PageRequest.of(page, size)
        val result = placeService.getPlacelist(searchTerm, category, city, projectId, pageable)
        val pageResult = PageImpl(result.data.map {
            PlaceBriefResponse.from(it)
        }, pageable, result.total) as Page<PlaceBriefResponse>

        val entity = ResponseEntity.status(HttpStatus.OK)
            .body(CommonResponse(HttpStatus.OK.value(), "장소 검색 완료", 0, pageResult))

        return entity
    }

    @Operation(security = [SecurityRequirement(name = "jwtAuth")])
    @GetMapping("/place/detail")
    fun getPlaceDetail(
        @RequestParam placeId: Long,
        @RequestParam projectId: Long
    ): ResponseEntity<CommonResponse<PlaceDetail>> {
        val result = placeService.getPlaceDetail(placeId, projectId)

        val entity = ResponseEntity.status(HttpStatus.OK)
            .body(CommonResponse(HttpStatus.OK.value(), "장소 디테일 조회 완료", 0, result))

        return entity
    }
}