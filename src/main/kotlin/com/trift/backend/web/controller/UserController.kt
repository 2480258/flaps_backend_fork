package com.trift.backend.web.controller

import com.trift.backend.domain.Role
import com.trift.backend.security.jwt.TokenService
import com.trift.backend.service.AuthService
import com.trift.backend.web.dto.CommonResponse
import com.trift.backend.web.dto.CommonResponseWithoutData
import com.trift.backend.web.dto.LoginResponse
import com.trift.backend.web.dto.UserInfoResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.security.SecurityScheme
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("api/v1/user")
@SecurityScheme(type = SecuritySchemeType.HTTP, scheme = "bearer", name = "jwtAuth")
class UserController {
    @Autowired
    lateinit var authService: AuthService

    @Operation(security = [SecurityRequirement(name = "jwtAuth")])
    @GetMapping("/info")
    fun getUserInfo() : ResponseEntity<CommonResponse<UserInfoResponse>> {
        val tokenDTO = authService.getLoginUser()
        val resp = ResponseEntity(CommonResponse(HttpStatus.OK.value(), "유저 정보 불러오기 완료", 0, UserInfoResponse(tokenDTO.getUserIdOrThrow(), getStringRepresentation(tokenDTO.role))), HttpStatus.OK)
        return resp
    }

    private fun getStringRepresentation(role : Role) : String {
        return when(role) {
            Role.ADMIN -> "ADMIN"
            Role.MEMBER -> "MEMBER"
            Role.GUEST -> "GUEST"
        }
    }
}