package com.trift.backend.e2e

import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.hamcrest.CoreMatchers
import org.hamcrest.Matchers
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestMethodOrder
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.TestPropertySource

@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@TestInstance(value = TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.MethodName::class)
class ProjectTest {

    lateinit var adminAccessToken1: String
    lateinit var adminAccessToken2: String
    lateinit var adminAccessToken3: String
    lateinit var guestToken: String
    var countryId = 0
    @Test
    fun `01_없는_프로젝트_조회_시_404_반환`() {
        adminAccessToken1 = E2ETestUtil.generateAdmin()
        adminAccessToken2 = E2ETestUtil.generateAdmin()
        adminAccessToken3 = E2ETestUtil.generateAdmin()
        guestToken = E2ETestUtil.generateGuest()
        countryId = E2ETestUtil.createCountry(adminAccessToken1)
        Given {
            header("Authorization", "Bearer $adminAccessToken1")
            header("Content-Type", "application/json")
        } When {
            get("/api/v1/project/1")
        } Then {
            statusCode(404)
        }
    }

    @Test
    fun `02_16자_이상의_프로젝트_생성_시_400_반환`() {
        val token = E2ETestUtil.generateAdmin()

        E2ETestUtil.createCity(token, countryId)
        Given {
            header("Authorization", "Bearer $token")
            header("Content-Type", "application/json")
            body("""{
  "name": "1234567890123456",
  "countryId": $countryId,
  "startTimeStamp": 2,
  "endTimeStamp": 3
}""")
        } When {
            post("/api/v1/project")
        } Then {
            statusCode(400)
        }
    }

    @Test
    fun `02_30일_이상의_프로젝트_생성_시_400_반환`() {
        val token = E2ETestUtil.generateAdmin()
        Given {
            header("Authorization", "Bearer $token")
            header("Content-Type", "application/json")
            body("""{
  "name": "1234567",
  "countryId": $countryId,
  "startTimeStamp": 0,
  "endTimeStamp": 2592086401
}""")
        } When {
            post("/api/v1/project")
        } Then {
            statusCode(400)
        }
    }

    @Test
    fun `02_다른_유저의_프로젝트_조회_시_403_반환`() {
        E2ETestUtil.createCity(adminAccessToken1, countryId)
        E2ETestUtil.createProject("name", adminAccessToken1, countryId)

        Given {
            header("Authorization", "Bearer $adminAccessToken2")
            header("Content-Type", "application/json")
        } When {
            get("/api/v1/project/1")
        } Then {
            statusCode(403)
        }
    }

    @Test
    fun `03_프로젝트_목록_조회_시_다른_유저의_프로젝트_제외_후_200_반환`() {
        Given {
            header("Authorization", "Bearer $adminAccessToken2")
            header("Content-Type", "application/json")
        } When {
            get("/api/v1/project?page=0&size=10")
        } Then {
            statusCode(200)
            body("data.content", Matchers.hasSize<Any>(0))
        }
    }

    @Test
    fun `04_프로젝트_페이지네이션_200_반환`() {
        val names = "abcdefghijklmnopqrstuv"

        names.forEach {
            E2ETestUtil.createProject(it.toString(), adminAccessToken3, countryId)
        }


        Given {
            header("Authorization", "Bearer $adminAccessToken3")
            header("Content-Type", "application/json")
        } When {
            get("/api/v1/project?page=0&size=10")
        } Then {
            statusCode(200)
            body("data.content", Matchers.hasSize<Any>(10))
            body("data.content[0].name", CoreMatchers.equalTo("a"))
        }

        Given {
            header("Authorization", "Bearer $adminAccessToken3")
            header("Content-Type", "application/json")
        } When {
            get("/api/v1/project?page=1&size=10")
        } Then {
            statusCode(200)
            body("data.content", Matchers.hasSize<Any>(10))
            body("data.content[0].name", CoreMatchers.equalTo("k"))
        }

        Given {
            header("Authorization", "Bearer $adminAccessToken3")
            header("Content-Type", "application/json")
        } When {
            get("/api/v1/project?page=2&size=10")
        } Then {
            statusCode(200)
            body("data.content", Matchers.hasSize<Any>(2))
            body("data.content[1].name", CoreMatchers.equalTo("v"))
        }
    }

    @Test
    fun `05_로그아웃_시_프로젝트_전체_삭제`() {

        E2ETestUtil.createProject("a", guestToken, countryId)

        Given {
            header("Authorization", "Bearer $guestToken")
            header("Content-Type", "application/json")
        } When {
            post("/api/v1/logout")
        } Then {
            statusCode(201)
        }

        Given {
            header("Authorization", "Bearer $guestToken")
            header("Content-Type", "application/json")
        } When {
            get("/api/v1/project?page=0&size=10")
        } Then {
            statusCode(200)
            body("data.content", Matchers.hasSize<Any>(0))
        }

        Given {
            header("Authorization", "Bearer $adminAccessToken1")
            header("Content-Type", "application/json")
        } When {
            get("/api/v1/project?page=0&size=10")
        } Then {
            statusCode(200)
            body("data.content", Matchers.hasSize<Any>(1))
        }
    }
}