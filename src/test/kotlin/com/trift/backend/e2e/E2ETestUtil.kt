package com.trift.backend.e2e

import com.trift.backend.service.ProjectInfo
import io.restassured.module.kotlin.extensions.Extract
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.hamcrest.CoreMatchers
import java.util.*

class E2ETestUtil {
    companion object {
        fun generateAdmin(): String {
            return When {
                post("/api/v1/login/admin")
            } Then {
                statusCode(201)
                header(
                    "Set-Cookie",
                    CoreMatchers.anyOf(
                        CoreMatchers.containsString("accessToken="),
                        CoreMatchers.containsString("refreshToken=")
                    )
                )
            } Extract {
                cookie("accessToken")
            }
        }

        fun generateGuest(): String {
            return When {
                post("/api/v1/login/guest")
            } Then {
                statusCode(201)
                header(
                    "Set-Cookie",
                    CoreMatchers.anyOf(
                        CoreMatchers.containsString("accessToken="),
                        CoreMatchers.containsString("refreshToken=")
                    )
                )
            } Extract {
                cookie("accessToken")
            }
        }

        fun createProject(
            name: String,
            token: String,
            countryId: Int,
            startTimeStamp: Long = 1688169600000,
            endTimeStamp: Long = 1688385600000
        ): ProjectInfo {
            val res = Given {
                header("Authorization", "Bearer $token")
                header("Content-Type", "application/json")
                // 2023-07-01 12:00AM(GMT) ~ 2023-07-03 12:00AM(GMT)
                body(
                    """{
                      "name": "$name",
                      "countryId": $countryId,
                      "startTimeStamp": $startTimeStamp,
                      "endTimeStamp": $endTimeStamp
                    }"""
                )
            } When {
                post("/api/v1/project")
            } Then {
                statusCode(201)
            } Extract {
                response()
            }

            res.prettyPrint()

            return res.body.jsonPath().getObject("data", ProjectInfo::class.java)
        }


        fun createCity(token: String, countryId: Int): Int {
            return Given {
                header("Authorization", "Bearer $token")
                header("Content-Type", "application/json")
                body("{\"name\" : \"a\", \"thumbnail\" : \"b\", \"longitude\":1.2, \"latitude\":2.3, \"isAvailable\": true, \"country\":$countryId }")
            } When {
                post("/api/v1/admin/city")
            } Then {
                statusCode(201)
            } Extract {
                body().jsonPath().getObject("data.cityId", Int::class.java)
            }
        }

        fun createCountry(token: String): Int {
            return Given {
                header("Authorization", "Bearer $token")
                header("Content-Type", "application/json")
                body("{\"name\" : \"a\", \"thumbnail\" : \"b\", \"longitude\":1.2, \"latitude\":2.3, \"isAvailable\": true }")
            } When {
                post("/api/v1/admin/country")
            } Then {
                statusCode(201)
            } Extract {
                body().jsonPath().getObject("data.countryId", Int::class.java)
            }
        }

        fun createGraph(token: String, project: Int, payload: String, nthDay: Int) {
            Given {
                header("Authorization", "Bearer $token")
                header("Content-Type", "application/json")
                body(payload)
            } When {
                val pp = post("/api/v1/graph?projectId=${project}&nthDay=$nthDay")

                pp.prettyPrint()
                pp
            } Then {
                statusCode(204)
            }
        }

        fun createPlaceRequest(
            name: String,
            token: String,
            countryId: Int,
            cityId: Int,
            latitude: Double = 1.2,
            longitude: Double = 2.3,
            place_id: String? = null,
            google_id: String? = null
        ): Long {
            val notNullPlaceId = place_id ?: UUID.randomUUID().toString()
            val notNullGoogleId = google_id ?: UUID.randomUUID().toString()
            val json = """{
  "countryId": $countryId,
  "cityId": $cityId,
  "queried_category": "queried_category",
  "query": "query",
  "name": "$name",
  "site": "site",
  "type": "type",
  "subtypes": [
    "subtypes"
  ],
  "category": "category",
  "phone": "phone",
  "full_address": "full_address",
  "borough": "borough",
  "street": "street",
  "city": "city",
  "postal_code": "postal_code",
  "state": "state",
  "us_state": "us_state",
  "country": "country",
  "country_code": "country_code",
  "latitude": $latitude,
  "longitude": $longitude,
  "time_zone": "time_zone",
  "plus_code": "plus_code",
  "area_service": true,
  "rating": 3.4,
  "reviews": 5,
  "reviews_link": "reviews_link",
  "reviews_tags": "reviews_tags",
  "reviews_per_score": {
    "1": 1
  },
  "photos_count": 6,
  "photo": "photo",
  "street_view": "street_view",
  "located_in": "located_in",
  "working_hours": {
    "2": 2
  },
  "other_hours": [{
    "3": 3
  }],
  "popular_times": [{
    "4": 4
  }],
  "business_status": "business_status",
  "about": {
    "5": 5
  },
  "range": "range",
  "posts": "posts",
  "logo": "logo",
  "description": "description",
  "verified": true,
  "owner_id": "7",
  "owner_title": "owner_title",
  "owner_link": "owner_link",
  "reservation_links": ["reservation_links"],
  "booking_appointment_link": "booking_appointment_link",
  "menu_link": "menu_link",
  "order_links": ["order_links"],
  "location_link": "location_link",
  "place_id": "$notNullPlaceId",
  "google_id": "$notNullGoogleId",
  "cid": "cid",
  "reviews_id": "reviews_id",
  "located_google_id": "located_google_id"
}"""

            val result = Given {
                header("Authorization", "Bearer $token")
                header("Content-Type", "application/json")
                body(json)
            } When {
                post("/api/v1/admin/place")
            } Then {
                statusCode(201)
            } Extract {
                jsonPath().getObject("data.id", Long::class.java)
            }

            return result
        }
    }
}