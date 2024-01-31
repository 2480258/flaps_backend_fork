package com.trift.backend.e2e

import com.trift.backend.domain.Project
import com.trift.backend.service.ProjectInfo
import io.restassured.module.kotlin.extensions.Extract
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.hamcrest.CoreMatchers
import org.hamcrest.Matchers
import org.junit.jupiter.api.*
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource

@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestInstance(value = TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.MethodName::class)
class ProjectCopyTest {
    lateinit var adminAccessToken1: String
    lateinit var adminAccessToken2: String
    lateinit var adminAccessToken3: String

    var country = 0
    var city = 0
    lateinit var project : ProjectInfo

    @BeforeAll
    fun beforeAll() {
        adminAccessToken1 = E2ETestUtil.generateAdmin()
        adminAccessToken2 = E2ETestUtil.generateAdmin()
        adminAccessToken3 = E2ETestUtil.generateAdmin()
        country = E2ETestUtil.createCountry(adminAccessToken1)
        city = E2ETestUtil.createCity(adminAccessToken1, country)
        project = E2ETestUtil.createProject("12345", adminAccessToken1, country)
        E2ETestUtil.createGraph(adminAccessToken1, project.projectId.toInt(), dailyGraphPayload1, 0)
        E2ETestUtil.createGraph(adminAccessToken1, project.projectId.toInt(), dailyGraphPayload2, 1)
        E2ETestUtil.createGraph(adminAccessToken1, project.projectId.toInt(), dailyGraphPayload3, 2)
    }

    @Test
    fun `copy_project_from_other`() {
        val my_project = Given {
            header("Authorization", "Bearer $adminAccessToken2")
            header("Content-Type", "application/json")
            body("""{"token": "$adminAccessToken1", "projectId": "${project.projectId}", "projectName": "${project.name}",  "date": {"3":"1", "2":"2"}, "startAt": 1688169600000, "endAt": 1688385600000}""")
        } When {
            post("/api/v1/project/clone")
        } Then {
            statusCode(201)
        } Extract {
            jsonPath().get<Int>("data.projectId")
        }

        Given {
            header("Authorization", "Bearer $adminAccessToken2")
            header("Content-Type", "application/json")
        } When {
            get("/api/v1/project/${project.projectId}")
        } Then {
            statusCode(403)
        }

        Given {
            header("Authorization", "Bearer $adminAccessToken2")
            header("Content-Type", "application/json")
        } When {
            get("/api/v1/project/$my_project")
        } Then {
            statusCode(200)
            body("data.name", CoreMatchers.equalTo(project.name))
            body("data.countryId", CoreMatchers.equalTo(project.countryId.toInt()))
            body("data.startTimeStamp", CoreMatchers.equalTo(project.startTimeStamp))
            body("data.endTimeStamp", CoreMatchers.equalTo(project.endTimeStamp))
            body("data.thumbnail", CoreMatchers.equalTo(project.thumbnail))
            body("data.creationTimeStamp", CoreMatchers.anything())
        }

        Given {
            header("Authorization", "Bearer $adminAccessToken2")
            header("Content-Type", "application/x-www-form-url-encoded")
        } When {
            get("/api/v1/graph?projectId=${my_project}&nthDay=0")
        } Then {
            statusCode(200)
            body("data.graphPlaceNodes[0].id", CoreMatchers.equalTo("3"))
        }

        Given {
            header("Authorization", "Bearer $adminAccessToken2")
            header("Content-Type", "application/x-www-form-url-encoded")
        } When {
            get("/api/v1/graph?projectId=${my_project}&nthDay=1")
        } Then {
            statusCode(200)
            body("data.graphPlaceNodes[0].id", CoreMatchers.equalTo("2"))
        }

        Given {
            header("Authorization", "Bearer $adminAccessToken2")
            header("Content-Type", "application/x-www-form-url-encoded")
        } When {
            get("/api/v1/graph?projectId=${my_project}&nthDay=2")
        } Then {
            statusCode(200)
            body("data", CoreMatchers.anything())
        }
    }

    @Test
    fun `no_copy_when_no_permission`() {
        Given {
            header("Authorization", "Bearer $adminAccessToken2")
            header("Content-Type", "application/json")
            body("""{"token": "$adminAccessToken3", "projectId": "${project.projectId}", "projectName": "${project.name}", "date": {"3":"1", "2":"2"}, "startAt": 1688169600000, "endAt": 1688385600000}""")
        } When {
            post("/api/v1/project/clone")
        } Then {
            statusCode(403)
        }
    }

    @Test
    fun `large_exceed_date_when_date`() {
        Given {
            header("Authorization", "Bearer $adminAccessToken2")
            header("Content-Type", "application/json")
            body("""{"token": "$adminAccessToken1", "projectId": "${project.projectId}", "projectName": "${project.name}", "date": {"1234567890":"1234567890", "2":"2"}, "startAt": 1688169600000, "endAt": 1688385600000}""")
        } When {
            post("/api/v1/project/clone")
        } Then {
            statusCode(400)
        }
    }

    @Test
    fun `small_src_exceed_date_when_date`() {
        Given {
            header("Authorization", "Bearer $adminAccessToken2")
            header("Content-Type", "application/json")
            body("""{"token": "$adminAccessToken1", "projectId": "${project.projectId}", "projectName": "${project.name}", "date": {"4":"1", "2":"2"}, "startAt": 1688169600000, "endAt": 1688385600000}""")
        } When {
            post("/api/v1/project/clone")
        } Then {
            statusCode(400)
        }
    }

    @Test
    fun `small_dst_exceed_date_when_date`() {
        Given {
            header("Authorization", "Bearer $adminAccessToken2")
            header("Content-Type", "application/json")
            body("""{"token": "$adminAccessToken1", "projectId": "${project.projectId}", "projectName": "${project.name}", "date": {"1":"4", "2":"2"}, "startAt": 1688169600000, "endAt": 1688385600000}""")
        } When {
            post("/api/v1/project/clone")
        } Then {
            statusCode(400)
        }
    }

    val dailyGraphPayload1 = """
        {
        "nthday": 0,
        "graphPlaceNodes": [
            {
                "id": "1",
                "type": "startFinishNode",
                "position": {
                    "x": 0,
                    "y": 100
                },
                "data": "START",
                "width": 89,
                "height": 44
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
            }
        ]
    }
    """.trimIndent()

    val dailyGraphPayload2 = """
        {
        "nthday": 0,
        "graphPlaceNodes": [
            {
                "id": "2",
                "type": "startFinishNode",
                "position": {
                    "x": 0,
                    "y": 100
                },
                "data": "START",
                "width": 89,
                "height": 44
            }
        ],
        "graphPlaceEdges": [
            {
                "id": "proximity-155e0e25-49b6-4806-a44c-ed9f85762fd-START",
                "source": "START",
                "target": "155e0e25-49b6-4806-a44c-ed9f85762fd1",
                "style": {
                    "strokeDasharray": 5
                },
                "type": "straight"
            }
        ]
    }
    """.trimIndent()

    val dailyGraphPayload3 = """
        {
        "nthday": 0,
        "graphPlaceNodes": [
            {
                "id": "3",
                "type": "startFinishNode",
                "position": {
                    "x": 0,
                    "y": 100
                },
                "data": "START",
                "width": 89,
                "height": 44
            }
        ],
        "graphPlaceEdges": [
            {
                "id": "proximity-155e0e25-49b6-4806-a44c-ed9f85762f-START",
                "source": "START",
                "target": "155e0e25-49b6-4806-a44c-ed9f85762fd1",
                "style": {
                    "strokeDasharray": 5
                },
                "type": "straight"
            }
        ]
    }
    """.trimIndent()
}