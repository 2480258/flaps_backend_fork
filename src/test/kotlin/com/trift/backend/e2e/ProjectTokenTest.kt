package com.trift.backend.e2e

import io.restassured.module.kotlin.extensions.Extract
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
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
class ProjectTokenTest {
    lateinit var adminAccessToken1: String
    lateinit var adminAccessToken2: String
    lateinit var projectAccessToken: String
    lateinit var writeProjectToken: String

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
    fun createProjectToken_Returns403() {
        Given {
            header("Authorization", "Bearer $adminAccessToken2")
            header("Content-Type", "application/json")
        } When {
            post("/api/v1/project/token/$project")
        } Then {
            statusCode(403)
        }
    }

    @Test
    fun createProjectToken() {
        val result = Given {
            header("Authorization", "Bearer $adminAccessToken1")
            header("Content-Type", "application/json")
        } When {
            post("/api/v1/project/token/$project")
        } Then {
            statusCode(201)
        } Extract {
            body().jsonPath().getObject("data.token", String::class.java)
        }

        projectAccessToken = result
    }

    @Test
    fun readProjectByToken() {
        Given {
            header("Authorization", "Bearer $projectAccessToken")
            header("Content-Type", "application/json")
        } When {
            get("/api/v1/project/$project")
        } Then {
            statusCode(200)
        }
    }

    @Test
    fun `reject_create_project_token_from_shared_token`() {
        val result = Given {
            header("Authorization", "Bearer $projectAccessToken")
            header("Content-Type", "application/json")
        } When {
            post("/api/v1/project/token/$project")
        } Then {
            statusCode(403)
        }
    }

    @Test
    fun createWriteProjectToken() {
        val result = Given {
            header("Authorization", "Bearer $adminAccessToken1")
            header("Content-Type", "application/json")
        } When {
            post("/api/v1/project/writetoken/$project")
        } Then {
            statusCode(201)
        } Extract {
            body().jsonPath().getObject("data.token", String::class.java)
        }

        writeProjectToken = result
    }

    @Test
    fun updateGraph_using_WriteProject_Token() {
        Given {
            header("Authorization", "Bearer $writeProjectToken")
            header("Content-Type", "application/json")
            body(dailyGraphPayload)
        } When {
            post("/api/v1/graph?projectId=${project}&nthDay=0")
        } Then {
            statusCode(204)
        }
    }

    val dailyGraphPayload = """
        {
        "nthday": 0,
        "graphPlaceNodes": [
            {
                "id": "START",
                "type": "startFinishNode",
                "position": {
                    "x": 0,
                    "y": 100
                },
                "data": "START",
                "width": 89,
                "height": 44
            },
            {
                "id": "FINISH",
                "type": "startFinishNode",
                "position": {
                    "x": 0,
                    "y": 600
                },
                "data": "FINISH",
                "width": 89,
                "height": 44
            },
            {
                "id": "31086981-b565-435d-b59b-91b1a4e6e348",
                "type": "graphNode",
                "position": {
                    "x": -90,
                    "y": 435
                },
                "data": {
                    "isChecked": false,
                    "memo": "",
                    "placeId": 1
                },
                "dragging": true,
                "selected": true,
                "width": 270,
                "height": 130,
                "positionAbsolute": {
                    "x": -90,
                    "y": 435
                }
            },
            {
                "id": "155e0e25-49b6-4806-a44c-ed9f85762fd1",
                "type": "graphNode",
                "position": {
                    "x": -135,
                    "y": 225
                },
                "data": {
                    "isChecked": false,
                    "memo": "",
                    "placeId": 1
                },
                "dragging": true,
                "selected": false,
                "width": 270,
                "height": 130,
                "positionAbsolute": {
                    "x": -135,
                    "y": 225
                }
            }
        ],
        "graphPlaceEdges": [
            {
                "id": "proximity-155e0e25-49b6-4806-a44c-ed9f85762fd1-START",
                "source": "START",
                "target": "155e0e25-49b6-4806-a44c-ed9f85762fd1",
                "style": {
                    "strokeDasharray": 5
                },
                "type": "straight"
            },
            {
                "id": "proximity-31086981-b565-435d-b59b-91b1a4e6e348-155e0e25-49b6-4806-a44c-ed9f85762fd1",
                "source": "155e0e25-49b6-4806-a44c-ed9f85762fd1",
                "target": "31086981-b565-435d-b59b-91b1a4e6e348",
                "style": {
                    "strokeDasharray": 5
                },
                "type": "straight"
            },
            {
                "id": "proximity-31086981-b565-435d-b59b-91b1a4e6e348-FINISH",
                "source": "31086981-b565-435d-b59b-91b1a4e6e348",
                "target": "FINISH",
                "style": {
                    "strokeDasharray": 5
                },
                "type": "straight"
            }
        ]
    }
    """.trimIndent()
}