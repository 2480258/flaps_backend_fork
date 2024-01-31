package com.trift.backend.e2e

import io.restassured.module.kotlin.extensions.Extract
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.junit.jupiter.api.*
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.web.server.ResponseStatusException
import java.util.*


@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@TestInstance(value = TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.MethodName::class)
class PlaceDuplicationTest {

    lateinit var adminAccessToken1: String
    var country = 0
    var city = 0

    @Test
    fun `throws_exception_when_duplicated_place_id`() {
        adminAccessToken1 = E2ETestUtil.generateAdmin()
        country = E2ETestUtil.createCountry(adminAccessToken1)
        city = E2ETestUtil.createCity(adminAccessToken1, country)
        E2ETestUtil.createPlaceRequest("a", adminAccessToken1, country, city, place_id = "123", google_id = "abc")
        createPlaceRequestAndAssert409("a", adminAccessToken1, country, city, place_id = "123")
        createPlaceRequestAndAssert409("a", adminAccessToken1, country, city, google_id = "abc")
    }


    fun createPlaceRequestAndAssert409(
        name: String,
        token: String,
        countryId: Int,
        cityId: Int,
        latitude: Double = 1.2,
        longitude: Double = 2.3,
        place_id: String? = null,
        google_id: String? = null
    ) {
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
            statusCode(409)
        }
    }
}