package com.trift.backend.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.server.ResponseStatusException
import java.net.URI


data class NaverAuthTokenResponse constructor(
    val access_token: String?,
    val refresh_token: String?,
    val token_type: String?,
    val expires_in: Int?,
    val error: String?,
    val error_description: String?
)


interface NaverLoginVerifyService {
    fun getNidFromOAuthResult(code: String, state: String): String
}

@Component
class NaverLoginVerifyServiceImpl : NaverLoginVerifyService {

    @Value("\${registration.naver.client-id}")
    lateinit var clientId: String

    @Value("\${registration.naver.client-secret}")
    lateinit var clientSecret: String

    private fun getAuthFromStateAndCode(code: String, state: String): String {
        val response = try {
            val restTemplate = RestTemplate()
            val resp = restTemplate.getForEntity(
                "https://nid.naver.com/oauth2.0/token?grant_type=authorization_code&client_id=$clientId&client_secret=$clientSecret&code=$code&state=$state",
                NaverAuthTokenResponse::class.java
            )

            resp
        } catch(e: Exception) {
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "네이버 접속 오류 (code, state)", e)
        }

        if(response.body == null || response.body!!.access_token == null) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "네이버 서버가 인증을 거절하였습니다. (info: ${response.body?.error}, reason: ${response.body?.error_description}")
        }

        return response.body!!.access_token!!
    }

    private fun getNidFromAuth(auth: String) : String {
        try {
            val restTemplate = RestTemplate()

            val headers = HttpHeaders()
            headers.add("Authorization", "Bearer $auth")
            headers.contentType = MediaType.APPLICATION_JSON

            val requestEntity = RequestEntity<Any?>(headers, HttpMethod.GET, URI("https://openapi.naver.com/v1/nid/me"))


            val response = restTemplate.exchange(requestEntity, String::class.java)

            val mapper = ObjectMapper()
            val mapped = mapper.readValue(response.body!!, Map::class.java)

            return (mapped["response"] as Map<*, *>)["id"] as String
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "네이버 접속 오류 (auth)", e)
        }
    }

    override fun getNidFromOAuthResult(code: String, state: String): String {
        val auth = getAuthFromStateAndCode(code, state)
        return getNidFromAuth(auth)
    }
}