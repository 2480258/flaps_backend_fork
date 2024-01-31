package com.trift.backend.web.controller

import com.trift.backend.security.RefreshToken
import com.trift.backend.service.LoginService
import com.trift.backend.web.dto.CommonResponse
import com.trift.backend.web.dto.CommonResponseWithoutData
import com.trift.backend.web.dto.UserInfoResponse
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.security.SecurityScheme
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("api/v1/")
@SecurityScheme(type = SecuritySchemeType.APIKEY, scheme = "basic", name = "Authorization")
@SecurityRequirement(name = "Authorization")
class SessionController {
    @Autowired
    lateinit var loginService: LoginService

    @PostMapping("/logout")
    fun logout() : ResponseEntity<CommonResponseWithoutData> {
        loginService.logout()

        val entity = ResponseEntity(CommonResponseWithoutData(HttpStatus.CREATED.value(), "로그아웃 완료", 0), HttpStatus.CREATED)
        return entity
    }

    @PostMapping("/refresh")
    fun refresh(@CookieValue("refreshToken") refreshToken: String) : ResponseEntity<CommonResponseWithoutData> {
        val accessToken = loginService.refresh(RefreshToken(refreshToken))

        val headers = HttpHeaders()
        headers.add("Set-Cookie", "accessToken=${accessToken.accessToken}; Path=/")

        val entity = ResponseEntity(CommonResponseWithoutData(HttpStatus.CREATED.value(), "리프레시 완료", 0), HttpStatus.CREATED)
        return entity
    }
}