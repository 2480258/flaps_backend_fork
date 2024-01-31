package com.trift.backend.web.controller

import com.trift.backend.domain.CityInfo
import com.trift.backend.domain.CountryInfo
import com.trift.backend.service.*
import com.trift.backend.web.dto.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.security.SecurityScheme
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.olegcherednik.jackson.utils.JacksonUtils

@RestController
@RequestMapping("api/v1/admin")
@SecurityScheme(type = SecuritySchemeType.HTTP, scheme = "bearer", name = "jwtAuth")
class AdminController {
    @Autowired
    private lateinit var cityService: CityService

    @Autowired
    private lateinit var placeService: PlaceService

    @Autowired
    private lateinit var countryService: CountryService

    @Autowired
    private lateinit var sampleProjectService: SampleProjectService

    @Operation(security = [SecurityRequirement(name = "jwtAuth")])
    @PostMapping("/country")
    fun createCountry(@RequestBody countryCreateRequest: CountryCreateRequest): ResponseEntity<CommonResponse<CountryInfo>> {
        val result = countryService.createCountry(
            countryCreateRequest.name,
            countryCreateRequest.thumbnail,
            countryCreateRequest.isAvailable,
            countryCreateRequest.latitude,
            countryCreateRequest.longitude
        )

        val entity = ResponseEntity.status(HttpStatus.CREATED)
            .body(CommonResponse(HttpStatus.CREATED.value(), "국가 생성 완료", 0, result))

        return entity
    }

    @Operation(security = [SecurityRequirement(name = "jwtAuth")])
    @PostMapping("/city")
    fun createCity(@RequestBody cityCreateRequest: CityCreateRequest): ResponseEntity<CommonResponse<CityInfo>> {
        val result = cityService.createCity(
            cityCreateRequest.name,
            cityCreateRequest.thumbnail,
            cityCreateRequest.longitude,
            cityCreateRequest.latitude,
            cityCreateRequest.isAvailable,
            cityCreateRequest.country
        )

        val entity = ResponseEntity.status(HttpStatus.CREATED)
            .body(CommonResponse(HttpStatus.CREATED.value(), "도시 생성 완료", 0, result))

        return entity
    }

    @Operation(security = [SecurityRequirement(name = "jwtAuth")])
    @PostMapping("/samples")
    fun createSampleProject(@RequestBody sampleProjectCreation: SampleProjectCreationRequest): ResponseEntity<CommonResponse<SampleProjectResponse>> {
        val result = sampleProjectService.saveSampleProject(sampleProjectCreation.project.let {
            ProjectCreation(it.name, it.countryId, it.startTimeStamp, it.endTimeStamp)
        }, sampleProjectCreation.extraInfo)

        val entity = ResponseEntity.status(HttpStatus.CREATED)
            .body(CommonResponse(HttpStatus.CREATED.value(), "샘플 프로젝트 생성 완료", 0, result.let {
                SampleProjectResponse(it.info.projectId, it.token.accessToken, it.info.extraInfo, ProjectInfo.from(it.info.project))
            }))

        return entity
    }

    @Operation(security = [SecurityRequirement(name = "jwtAuth")])
    @PostMapping("/place")
    fun createPlace(@RequestBody body: String): ResponseEntity<CommonResponse<PlaceId>> {
        val placeCreateRequest = JacksonUtils.readValue(body, PlaceCreateRequest::class.java)

        val result = placeService.createPlace(
            PlaceRequestDTO(
                countryId = placeCreateRequest.countryId!!,
                cityId = placeCreateRequest.cityId!!,
                outscrapper_queried_category = placeCreateRequest.queried_category!!,
                outscrapper_query = placeCreateRequest.query!!,
                outscrapper_name = placeCreateRequest.name!!,
                outscrapper_site = placeCreateRequest.site,
                outscrapper_type = placeCreateRequest.type!!,
                outscrapper_json_subtypes = placeCreateRequest.subtypes!!, // JSON
                outscrapper_category = placeCreateRequest.category,
                outscrapper_phone = placeCreateRequest.phone,
                outscrapper_full_address = placeCreateRequest.full_address!!,
                outscrapper_borough = placeCreateRequest.borough!!,
                outscrapper_street = placeCreateRequest.street,
                outscrapper_city = placeCreateRequest.city,
                outscrapper_postal_code = placeCreateRequest.postal_code!!,
                outscrapper_state = placeCreateRequest.state,
                outscrapper_us_state = placeCreateRequest.us_state,
                outscrapper_country = placeCreateRequest.country,
                outscrapper_country_code = placeCreateRequest.country_code,
                outscrapper_latitude = placeCreateRequest.latitude!!,
                outscrapper_longitude = placeCreateRequest.longitude!!,
                outscrapper_time_zone = placeCreateRequest.time_zone,
                outscrapper_plus_code = placeCreateRequest.plus_code,
                outscrapper_area_service = placeCreateRequest.area_service,
                outscrapper_rating = placeCreateRequest.rating,
                outscrapper_reviews = placeCreateRequest.reviews,
                outscrapper_reviews_link = placeCreateRequest.reviews_link,
                outscrapper_reviews_tags = placeCreateRequest.reviews_tags,
                outscrapper_reviews_per_score = placeCreateRequest.reviews_per_score,
                outscrapper_photos_count = placeCreateRequest.photos_count,
                outscrapper_photo = placeCreateRequest.photo!!,
                outscrapper_street_view = placeCreateRequest.street_view,
                outscrapper_located_in = placeCreateRequest.located_in,
                outscrapper_json_working_hours = placeCreateRequest.working_hours, // JSON
                outscrapper_json_other_hours = placeCreateRequest.other_hours, // JSON
                outscrapper_json_popular_times = placeCreateRequest.popular_times, // JSON
                outscrapper_business_status = placeCreateRequest.business_status,
                outscrapper_json_about = placeCreateRequest.about, // JSON
                outscrapper_range = placeCreateRequest.range,
                outscrapper_posts = placeCreateRequest.posts,
                outscrapper_logo = placeCreateRequest.logo,
                outscrapper_description = placeCreateRequest.description,
                outscrapper_verified = placeCreateRequest.verified,
                outscrapper_owner_id = placeCreateRequest.owner_id,
                outscrapper_owner_title = placeCreateRequest.owner_title,
                outscrapper_owner_link = placeCreateRequest.owner_link,
                outscrapper_reservation_links = placeCreateRequest.reservation_links,
                outscrapper_booking_appointment_link = placeCreateRequest.booking_appointment_link,
                outscrapper_menu_link = placeCreateRequest.menu_link,
                outscrapper_order_links = placeCreateRequest.order_links,
                outscrapper_location_link = placeCreateRequest.location_link,
                outscrapper_place_id = placeCreateRequest.place_id!!,
                outscrapper_google_id = placeCreateRequest.google_id!!,
                outscrapper_cid = placeCreateRequest.cid,
                outscrapper_reviews_id = placeCreateRequest.reviews_id,
                outscrapper_located_google_id = placeCreateRequest.located_google_id
            )
        )

        val entity = ResponseEntity.status(HttpStatus.CREATED)
            .body(CommonResponse(HttpStatus.CREATED.value(), "Place 생성 완료", 0, result))

        return entity
    }
}