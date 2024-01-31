package com.trift.backend.web.controller

import com.trift.backend.domain.City
import com.trift.backend.domain.CountryInfo
import com.trift.backend.service.CityService
import com.trift.backend.service.CountryService
import com.trift.backend.web.dto.*
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.security.SecurityScheme
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/v1")
@SecurityScheme(type = SecuritySchemeType.HTTP, scheme = "bearer", name = "jwtAuth")
class CountryController {
    @Autowired
    private lateinit var countryService: CountryService

    @GetMapping("/country")
    fun listCountry(): ResponseEntity<CommonResponse<List<CountryInfo>>> {
        val list = countryService.listCountry()

        val entity = ResponseEntity.status(HttpStatus.OK)
            .body(CommonResponse(HttpStatus.OK.value(), "국가 불러오기 완료", 0, list))

        return entity
    }
}