package com.trift.backend.e2e

import io.restassured.RestAssured.post
import io.restassured.module.kotlin.extensions.Extract
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import io.restassured.specification.RequestSpecification
import org.hamcrest.CoreMatchers.*
import org.junit.jupiter.api.Test

import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource

@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class LoginTest {

    @Test
    fun rejectLogoutWhenNotLogin() {
        When {
            post("/api/v1/logout")
        } Then {
            statusCode(403)
        }
    }

    @Test
    fun loginAsGuest() {
        When {
            post("/api/v1/login/guest")
        } Then {
            statusCode(201)
            header("Set-Cookie", anyOf(containsString("accessToken="), containsString("refreshToken=")))
        }
    }

    @Test
    fun `403_when_invalid_json_token`() {
        Given {
            header("Authorization", "Bearer 1234567")
        } When {
            get("/api/v1/project")
        } Then {
            statusCode(403)
        }
    }

    @Test
    fun `403_when_not_bearer`() {
        Given {
            header("Authorization", "1234567")
        } When {
            get("/api/v1/project")
        } Then {
            statusCode(403)
        }
    }

    @Test
    fun `403_when_invalid_refresh_token`() {
        Given {
            cookie("refreshToken", "12345")
        } When {
            post("/api/v1/refresh")
        } Then {
            statusCode(403)
        }
    }

    @Test
    fun loginAsGuestWithNonCapitalHeader() {
        val response = post("/api/v1/login/guest")
        val accessToken = response.cookies["accessToken"]
        val refreshToken = response.cookies["refreshToken"]

        Given {
            header("Authorization", "Bearer $accessToken")
        } When {
            post("/api/v1/logout")
        } Then {
            statusCode(201)
        }

        Given {
            cookie("refreshToken", refreshToken)
        } When {
            post("/api/v1/refresh")
        } Then {
            statusCode(403)
        }
    }

    @Test
    fun loginAsGuestAndLogout() {
        val response = post("/api/v1/login/guest")
        val accessToken = response.cookies["accessToken"]
        val refreshToken = response.cookies["refreshToken"]

        Given {
            header("Authorization", "Bearer $accessToken")
        } When {
            post("/api/v1/logout")
        } Then {
            statusCode(201)
        }

        Given {
            cookie("refreshToken", refreshToken)
        } When {
            post("/api/v1/refresh")
        } Then {
            statusCode(403)
        }
    }


    @Test
    fun loginAsGuestAndRefresh() {
        val response = post("/api/v1/login/guest")
        val accessToken = response.cookies["accessToken"]
        val refreshToken = response.cookies["refreshToken"]

        Given {
            cookie("refreshToken", refreshToken)
        } When {
            post("/api/v1/refresh")

        } Then {
            statusCode(201)
        }

        Given {
            header("Authorization", "Bearer $accessToken")
        } When {
            post("/api/v1/logout")
        } Then {
            statusCode(201)
        }
    }
}