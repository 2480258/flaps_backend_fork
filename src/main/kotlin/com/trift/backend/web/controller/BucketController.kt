package com.trift.backend.web.controller

import com.trift.backend.domain.PlaceBrief
import com.trift.backend.service.BucketService
import com.trift.backend.web.dto.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.security.SecurityScheme
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.olegcherednik.jackson.utils.JacksonUtils

@RestController
@RequestMapping("api/v1")
@SecurityScheme(type = SecuritySchemeType.HTTP, scheme = "bearer", name = "jwtAuth")
class BucketController {

    @Autowired
    lateinit var bucketService: BucketService

    @Operation(security = [SecurityRequirement(name = "jwtAuth")])
    @GetMapping("/bucket")
    fun getBucket(@RequestParam projectId: Long) : ResponseEntity<CommonResponse<List<PlaceBriefResponse>>> {
        val result = bucketService.getBucketItem(projectId).map {
            PlaceBriefResponse.from(it)
        }

        val entity = ResponseEntity.status(HttpStatus.OK)
            .body(CommonResponse(HttpStatus.OK.value(), "버킷 장소 조회 완료", 0, result))

        return entity
    }

    @Operation(security = [SecurityRequirement(name = "jwtAuth")])
    @PostMapping("/bucket")
    fun addPlaceToBucket(@RequestBody request: BucketAddPlaceRequest) : ResponseEntity<CommonResponse<List<PlaceBriefResponse>>> {

        val result = bucketService.addBucketItem(request.projectId, request.placeId).map {
            PlaceBriefResponse.from(it)
        }

        val entity = ResponseEntity.status(HttpStatus.CREATED)
            .body(CommonResponse(HttpStatus.CREATED.value(), "버킷에 장소 추가 완료", 0, result))

        return entity
    }

    @Operation(security = [SecurityRequirement(name = "jwtAuth")])
    @DeleteMapping("/bucket")
    fun deletePlaceFromBucket(@RequestBody request: BucketAddPlaceRequest) : ResponseEntity<CommonResponse<List<PlaceBriefResponse>>> {

        val result = bucketService.deleteBucketItem(request.projectId, request.placeId).map {
            PlaceBriefResponse.from(it)
        }

        val entity = ResponseEntity.status(HttpStatus.CREATED)
            .body(CommonResponse(HttpStatus.CREATED.value(), "버킷에서 장소 삭제 완료", 0, result))

        return entity
    }
}