package com.trift.backend.e2e

import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.hamcrest.Matchers
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestMethodOrder
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource

@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestInstance(value = TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.MethodName::class)
class ProjectTransferTest {
    lateinit var adminAccessToken1: String
    lateinit var adminAccessToken2: String

    var country = 0
    var city = 0
    var project = 0

    @BeforeAll
    fun beforeAll() {
        adminAccessToken1 = E2ETestUtil.generateAdmin()
        adminAccessToken2 = E2ETestUtil.generateAdmin()
        country = E2ETestUtil.createCountry(adminAccessToken1)
        city = E2ETestUtil.createCity(adminAccessToken1, country)
        project = E2ETestUtil.createProject("12345", adminAccessToken1, country).projectId.toInt()
    }

    @Test
    fun `transfer_project_to_other`() {
        Given {
            header("Authorization", "Bearer $adminAccessToken2")
            header("Content-Type", "application/json")
            body("""{"user": "$adminAccessToken1"}""")
        } When {
            post("/api/v1/project/convey")
        } Then {
            statusCode(201)
            body("data.movedProject", Matchers.hasSize<Any>(1))
            body("data.movedProject", Matchers.contains(project))
        }

        Given {
            header("Authorization", "Bearer $adminAccessToken2")
            header("Content-Type", "application/json")
        } When {
            get("/api/v1/project?page=0&size=10")
        } Then {
            statusCode(200)
            body("data.content", Matchers.hasSize<Any>(1))
        }

        Given {
            header("Authorization", "Bearer $adminAccessToken1")
            header("Content-Type", "application/json")
        } When {
            get("/api/v1/project?page=0&size=10")
        } Then {
            statusCode(200)
            body("data.content", Matchers.hasSize<Any>(0))
        }
    }

    @Test
    fun `reject_transfer_if_source_and_dest_are_equal`() {
        Given {
            header("Authorization", "Bearer $adminAccessToken1")
            header("Content-Type", "application/json")
            body("""{"user": "$adminAccessToken1"}""")
        } When {
            post("/api/v1/project/convey")
        } Then {
            statusCode(400)
        }
    }
}