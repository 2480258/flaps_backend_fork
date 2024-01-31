package com.trift.backend.security.jwt

import com.trift.backend.security.*
import com.trift.backend.service.LoginDBWriteService
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.lang.Exception
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.*


interface TokenService {
    fun createAccessToken(access: TokenDTO): AccessToken
    fun createRefreshToken(refresh: TokenDTO): RefreshToken
    fun verifyAccessToken(accessToken: AccessToken) : TokenDTO
    fun createAccessToken(refreshToken: RefreshToken): AccessToken
    fun removeRefreshToken(userId: Long)
}

@Service
class TokenServiceImpl : TokenService {
    @Value("\${jwt.access_secret}")
    lateinit var ACCESS_SECRET_KEY: String

    @Value("\${jwt.refresh_secret}")
    lateinit var REFRESH_SECRET_KEY: String

    @Value("\${jwt.access_expire}")
    var ACCESS_EXPIRE_TIME: Long = 1000 * 60 * 60

    @Value("\${jwt.refresh_expire}")
    var REFRESH_EXPIRE_TIME: Long = 1000 * 60 * 60 * 24 * 7

    @Autowired
    lateinit var loginDbWriteService: LoginDBWriteService

    private fun calculateSHA256From(key: String) : ByteArray {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(key.toByteArray(StandardCharsets.UTF_8))
        return hashBytes
    }

    override fun createAccessToken(access: TokenDTO): AccessToken {
        val claim = access.createClaimFrom()

        val currentTime = Date()
        val accessToken = Jwts.builder()
            .setClaims(claim)
            .setIssuedAt(currentTime)
            .setExpiration(Date(currentTime.time + ACCESS_EXPIRE_TIME))
            .signWith(SignatureAlgorithm.HS256, calculateSHA256From(ACCESS_SECRET_KEY))
            .compact()

        return AccessToken(accessToken)
    }

    override fun createRefreshToken(refresh: TokenDTO): RefreshToken {
        val claim = refresh.createClaimFrom()
        val currentTime = Date()

        val refreshToken = Jwts.builder()
            .setClaims(claim)
            .setIssuedAt(currentTime)
            .setExpiration(Date(currentTime.time + REFRESH_EXPIRE_TIME))
            .signWith(SignatureAlgorithm.HS256, calculateSHA256From(REFRESH_SECRET_KEY))
            .compact()

        val result = RefreshToken(refreshToken)
        loginDbWriteService.registerRefreshTokenAndWriteLogIn(refresh.getUserIdOrThrow(), result)

        return result
    }

    override fun verifyAccessToken(accessToken: AccessToken) : TokenDTO {
        return parseJWT(accessToken.accessToken, ACCESS_SECRET_KEY)
    }

    override fun createAccessToken(refreshToken: RefreshToken): AccessToken {
        val dto = parseJWT(refreshToken.refreshToken, REFRESH_SECRET_KEY)

        if(!loginDbWriteService.isVaildRefreshTokenAndWriteSignIn(dto.getUserIdOrThrow(), refreshToken)) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "수동으로 만료처리된 Refresh Token")
        }

        return createAccessToken(dto)
    }

    private fun parseJWT(tokenString: String, secretKey: String): TokenDTO {
        try {
            val claim = Jwts.parser()
                .setSigningKey(calculateSHA256From(secretKey))
                .parseClaimsJws(tokenString)

            val dto = TokenDTO.verifyExpirationAndCreateDTOFrom(claim)
            return dto
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "유효하지 않은 JWT Token 입니다.")
        }
    }

    override fun removeRefreshToken(userId: Long) {
        loginDbWriteService.revokeRefreshToken(userId)
    }
}