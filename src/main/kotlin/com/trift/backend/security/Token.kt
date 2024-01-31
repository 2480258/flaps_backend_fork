package com.trift.backend.security

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.trift.backend.domain.Role
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jws
import io.jsonwebtoken.Jwts
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.security.InvalidParameterException
import java.util.*

data class AccessToken(val accessToken: String)

data class RefreshToken(val refreshToken: String)


public data class TokenSubject @JsonCreator(mode = JsonCreator.Mode.PROPERTIES) public constructor(@JsonProperty("userId") val userId: Long?, @JsonProperty("projectId") val projectId: Long?, @JsonProperty("writable") val writable: Boolean?) {
    init {
        if(!((userId == null) xor (projectId == null))) {
            throw InvalidParameterException("Invalid JWT Token, both user id and project id are null or not null")
        }

        if((userId != null) and (writable != null)) {
            throw InvalidParameterException("Invalid JWT Token, user id and writable has value")
        }
    }
    companion object {
        fun from(json: String) : TokenSubject {
            val objectMapper = ObjectMapper()
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
            val map =  objectMapper.readValue(json, TokenSubject::class.java)

            return map
        }
    }

    fun toJSONString() : String {
        val objectMapper = ObjectMapper()
        return objectMapper.writeValueAsString(this)
    }
}

data class TokenDTO(private val subject: TokenSubject, val role: Role) {
    companion object {

        val ROLE_KEY = "role"

        fun verifyExpirationAndCreateDTOFrom(claims: Jws<Claims>) : TokenDTO {
            if(claims.body.expiration.after(Date())) {
                return TokenDTO(TokenSubject.from(claims.body.subject), Role.valueOf(claims.body[ROLE_KEY] as String))
            } else {
                throw InvalidParameterException("이미 지난 Token 유효기간입니다.")
            }
        }
    }

    fun createClaimFrom() : Claims {
        val claim = Jwts.claims()
        claim.subject = subject.toJSONString()
        claim[ROLE_KEY] = role.name

        return claim
    }

    val userId: Long? = subject.userId
    val projectId: Long? = subject.projectId

    fun getUserIdOrNull() : Long? {
        return subject.userId
    }

    fun getUserIdOrThrow() : Long {
        return subject.userId ?: throw ResponseStatusException(HttpStatus.FORBIDDEN, "잘못된 토큰 컨텍스트")
    }

    fun getProjectIdOrThrow(): Long {
        return subject.projectId ?: throw ResponseStatusException(HttpStatus.FORBIDDEN, "잘못된 토큰 컨텍스트")
    }

    fun getProjectIdIfWritableOrNull(): Long? {
        return if(subject.writable == true) getProjectIdOrThrow() else null
    }
}

