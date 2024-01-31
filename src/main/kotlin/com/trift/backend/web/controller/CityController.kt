package com.trift.backend.web.controller

import com.trift.backend.domain.CityInfo
import com.trift.backend.service.CityService
import com.trift.backend.web.dto.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/v1")
class CityController {
    @Autowired
    private lateinit var cityService: CityService

    @GetMapping("/city")
    fun listCity(): ResponseEntity<CommonResponse<List<CityInfo>>> {
        val list = cityService.listCityWithPlaces()

        val entity = ResponseEntity.status(HttpStatus.OK)
            .body(CommonResponse(HttpStatus.OK.value(), "도시 불러오기 완료", 0, list))

        return entity
    }
}