package com.trift.backend.e2e

import com.trift.backend.service.ProjectInfo
import io.restassured.module.kotlin.extensions.Extract
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.hamcrest.CoreMatchers
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
class SampleProjectTest {
    lateinit var adminAccessToken1: String
    lateinit var adminAccessToken2: String
    lateinit var adminAccessToken3: String

    var country = 0
    var city = 0
    var projectId : Int = 0
    var token: String? = null
    @BeforeAll
    fun beforeAll() {
        adminAccessToken1 = E2ETestUtil.generateAdmin()
        adminAccessToken2 = E2ETestUtil.generateAdmin()
        adminAccessToken3 = E2ETestUtil.generateAdmin()
        country = E2ETestUtil.createCountry(adminAccessToken1)
        city = E2ETestUtil.createCity(adminAccessToken1, country)
    }

    @Test
    fun `01_create_sample_project`() {
        projectId = Given {
            header("Authorization", "Bearer $adminAccessToken1")
            header("Content-Type", "application/json")
            body("""{"project": {
                      "name": "a",
                      "countryId": $country,
                      "startTimeStamp": 124,
                      "endTimeStamp": 567
                    }, "extraInfo": {"abc":"123"}}""")
        } When {
            post("/api/v1/admin/samples")
        } Then {
            statusCode(201)
        } Extract {
            jsonPath().get<Int>("data.projectId")
        }
    }

    @Test
    fun `02_list_sample_projects`() {
        token = Given {
            header("Authorization", "Bearer $adminAccessToken3")
            header("Content-Type", "application/json")
        } When {
            get("/api/v1/samples/$country")
        } Then {
            statusCode(200)
            body("data[0].projectId", CoreMatchers.equalTo(projectId))
            body("data[0].project.projectId", CoreMatchers.equalTo(projectId))
            body("data[0].projectExtraInfo.abc", CoreMatchers.equalTo("123"))
        } Extract {
            jsonPath().get<String>("data[0].token")
        }
    }

    @Test
    fun `03_access_project_via_token`() {
        Given {
            header("Authorization", "Bearer $token")
            header("Content-Type", "application/json")
        } When {
            get("/api/v1/project/$projectId")
        } Then {
            statusCode(200)
            body("data.projectId", CoreMatchers.equalTo(projectId))
        }
    }
}