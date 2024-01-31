package com.trift.backend.e2e

import io.restassured.RestAssured
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.hamcrest.CoreMatchers
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.TestPropertySource

@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class UserTest {
    @Test
    fun loginAndGetUserInfo() {
        val response = RestAssured.post("/api/v1/login/guest")
        val accessToken = response.cookies["accessToken"]
        val refreshToken = response.cookies["refreshToken"]

        Given {
            header("authorization", "Bearer $accessToken")
        } When {
            get("/api/v1/user/info")
        } Then {
            statusCode(200)
            body("data.userId", CoreMatchers.equalTo(1))
            body("data.role", CoreMatchers.equalTo("GUEST"))
        }
    }
}