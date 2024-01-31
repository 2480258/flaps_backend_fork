package com.trift.backend.web.controller

import com.trift.backend.service.LoginService
import com.trift.backend.web.dto.CommonResponse
import com.trift.backend.web.dto.CommonResponseWithoutData
import com.trift.backend.web.dto.LoginResponse
import com.trift.backend.web.dto.UserInfoResponse
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.security.SecurityScheme
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/v1/login")
class LoginController {
    @Autowired
    lateinit var loginService: LoginService

    @PostMapping("/guest")
    fun loginAsGuest() : ResponseEntity<CommonResponseWithoutData> {
        val tokens = loginService.guestLogin()
        val headers = HttpHeaders()

        headers.add("Set-Cookie", "refreshToken=${tokens.second.refreshToken}; Path=/; HttpOnly; SameSite=None; Secure")
        headers.add("Set-Cookie", "accessToken=${tokens.first.accessToken}; Path=/; SameSite=None; Secure")

        val entity = ResponseEntity.status(HttpStatus.CREATED)
            .headers(headers)
            .body(CommonResponseWithoutData(HttpStatus.CREATED.value(), "게스트로 로그인 완료", 0))

        return entity
    }

    @PostMapping("/naver")
    fun loginAsNaverUser(@RequestParam code: String, @RequestParam state: String) : ResponseEntity<CommonResponseWithoutData> {
        val tokens = loginService.naverLogin(code, state)
        val headers = HttpHeaders()
        headers.add("Set-Cookie", "refreshToken=${tokens.second.refreshToken}; Path=/; HttpOnly; SameSite=None; Secure")
        headers.add("Set-Cookie", "accessToken=${tokens.first.accessToken}; Path=/; SameSite=None; Secure")

        val entity = ResponseEntity.status(HttpStatus.CREATED)
            .headers(headers)
            .body(CommonResponseWithoutData(HttpStatus.CREATED.value(), "네이버 계정으로 로그인 완료", 0))

        return entity
    }

    @PostMapping("/admin")
    fun loginAsAdmin() : ResponseEntity<CommonResponseWithoutData>{
        val tokens = loginService.adminLogin()
        val headers = HttpHeaders()
        headers.add("Set-Cookie", "refreshToken=${tokens.second.refreshToken}; Path=/; HttpOnly; SameSite=None; Secure")
        headers.add("Set-Cookie", "accessToken=${tokens.first.accessToken}; Path=/; SameSite=None; Secure")

        val entity = ResponseEntity.status(HttpStatus.CREATED)
            .headers(headers)
            .body(CommonResponseWithoutData(HttpStatus.CREATED.value(), "어드민으로 로그인 완료", 0))

        return entity
    }

}