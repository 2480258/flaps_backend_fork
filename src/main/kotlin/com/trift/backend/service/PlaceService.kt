package com.trift.backend.service

import com.trift.backend.domain.PageableData
import com.trift.backend.domain.Place
import com.trift.backend.domain.PlaceBrief
import com.trift.backend.repository.CityRepository
import com.trift.backend.repository.CountryRepository
import com.trift.backend.repository.PlaceRepository
import org.hibernate.exception.ConstraintViolationException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import kotlin.math.PI
import kotlin.math.cos


interface PlaceService {
    @Transactional(isolation = Isolation.SERIALIZABLE)
    fun createPlace(placeRequestDTO: PlaceRequestDTO): PlaceId

    @Transactional(isolation = Isolation.SERIALIZABLE)
    fun getPlacelist(
        searchTerm: String?,
        category: String?,
        cityId: Long?,
        projectId: Long,
        page: Pageable
    ): PageableData<PlaceBrief>

    @Transactional(isolation = Isolation.SERIALIZABLE)
    fun getPlaceDetail(placeId: Long, projectId: Long): PlaceDetail

    @Transactional(isolation = Isolation.SERIALIZABLE)
    fun getNearbyPlace(placeId: Long, projectId: Long): List<PlaceBrief>

    fun getPlaceIdFromSearch(
        searchTerm: String?,
        category: String?,
        cityId: Long?,
        countryId: Long,
        pageOffset: Long,
        pageSize: Int
    ): PageableData<Long>
}

data class PlaceId constructor(
    val id: Long
)

data class PlaceDetailInfo constructor(
    val name: String?,
    val site: String?,
    val type: String?,
    val subtypes: List<Any>?,
    val phone: String?,
    val full_address: String?,
    val latitude: Double?,
    val longitude: Double?,
    val rating: Double?,
    val photo: String?,
    val range: String?,
    val description: String?,
    val about: Map<String, Any>?,
    val location_link: String?,
    val working_hours: Map<String, Any>?
)

data class PlaceDetail constructor(val info: PlaceDetailInfo, val placeId: Long?, val isLiked: Boolean)

data class PlaceRequestDTO constructor(
    val countryId: Long,
    val cityId: Long,
    val outscrapper_queried_category: String,
    val outscrapper_query: String,
    val outscrapper_name: String,
    val outscrapper_site: String?,
    val outscrapper_type: String,
    val outscrapper_json_subtypes: List<Any>, // JSON
    val outscrapper_category: String?,
    val outscrapper_phone: String?,
    val outscrapper_full_address: String,
    val outscrapper_borough: String,
    val outscrapper_street: String?,
    val outscrapper_city: String?,
    val outscrapper_postal_code: String,
    val outscrapper_state: String?,
    val outscrapper_us_state: String?,
    val outscrapper_country: String?,
    val outscrapper_country_code: String?,
    val outscrapper_latitude: Double,
    val outscrapper_longitude: Double,
    val outscrapper_time_zone: String?,
    val outscrapper_plus_code: String?,
    val outscrapper_area_service: Boolean?,
    val outscrapper_rating: Double?,
    val outscrapper_reviews: Long?,
    val outscrapper_reviews_link: String?,
    val outscrapper_reviews_tags: String?,
    val outscrapper_reviews_per_score: Map<String, Any>?,
    val outscrapper_photos_count: Long?,
    val outscrapper_photo: String,
    val outscrapper_street_view: String?,
    val outscrapper_located_in: String?,
    val outscrapper_json_working_hours: Map<String, Any>?, // JSON
    val outscrapper_json_other_hours: List<Map<String, Any>>?, // JSON
    val outscrapper_json_popular_times: List<Any>?, // JSON
    val outscrapper_business_status: String?,
    val outscrapper_json_about: Map<String, Any>?, // JSON
    val outscrapper_range: String?,
    val outscrapper_posts: String?,
    val outscrapper_logo: String?,
    val outscrapper_description: String?,
    val outscrapper_verified: Boolean?,
    val outscrapper_owner_id: String?,
    val outscrapper_owner_title: String?,
    val outscrapper_owner_link: String?,
    val outscrapper_reservation_links: List<String>?,
    val outscrapper_booking_appointment_link: String?,
    val outscrapper_menu_link: String?,
    val outscrapper_order_links: List<String>?,
    val outscrapper_location_link: String?,
    val outscrapper_place_id: String,
    val outscrapper_google_id: String,
    val outscrapper_cid: String?,
    val outscrapper_reviews_id: String?,
    val outscrapper_located_google_id: String?
)

@Component
class PlaceServiceImpl : PlaceService {

    @Autowired
    private lateinit var placeRepository: PlaceRepository

    @Autowired
    private lateinit var cityRepository: CityRepository

    @Autowired
    private lateinit var countryRepository: CountryRepository

    @Autowired
    private lateinit var projectService: ProjectService

    @Autowired
    private lateinit var preprocessService: SearchPreprocessService

    @Autowired
    private lateinit var placeCacheService: PlaceCacheService


    @Transactional(isolation = Isolation.SERIALIZABLE)
    override fun createPlace(placeRequestDTO: PlaceRequestDTO): PlaceId {

        val city = cityRepository.findById(placeRequestDTO.cityId).orElseThrow {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "City ID를 찾을 수 없음")
        }

        val country = countryRepository.findById(placeRequestDTO.countryId).orElseThrow {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Country ID를 찾을 수 없음")
        }

        val hangulString = preprocessService.getHString(placeRequestDTO.outscrapper_name)

        val place = Place().apply {
            this.country = country
            this.city = city

            this.search_without_space = hangulString.explodeWithoutSpace()
            this.search_chosung_without_space = hangulString.flatAsInitialWithoutSpace()

            this.outscrapper_queried_category = placeRequestDTO.outscrapper_queried_category
            this.outscrapper_query = placeRequestDTO.outscrapper_query
            this.outscrapper_name = placeRequestDTO.outscrapper_name
            this.outscrapper_site = placeRequestDTO.outscrapper_site
            this.outscrapper_type = placeRequestDTO.outscrapper_type
            this.outscrapper_json_subtypes = placeRequestDTO.outscrapper_json_subtypes// JSON
            this.outscrapper_category = placeRequestDTO.outscrapper_category
            this.outscrapper_phone = placeRequestDTO.outscrapper_phone
            this.outscrapper_full_address = placeRequestDTO.outscrapper_full_address
            this.outscrapper_borough = placeRequestDTO.outscrapper_borough
            this.outscrapper_street = placeRequestDTO.outscrapper_street
            this.outscrapper_city = placeRequestDTO.outscrapper_city
            this.outscrapper_postal_code = placeRequestDTO.outscrapper_postal_code
            this.outscrapper_state = placeRequestDTO.outscrapper_state
            this.outscrapper_us_state = placeRequestDTO.outscrapper_us_state
            this.outscrapper_country = placeRequestDTO.outscrapper_country
            this.outscrapper_country_code = placeRequestDTO.outscrapper_country_code
            this.outscrapper_latitude = placeRequestDTO.outscrapper_latitude
            this.outscrapper_longitude = placeRequestDTO.outscrapper_longitude
            this.outscrapper_time_zone = placeRequestDTO.outscrapper_time_zone
            this.outscrapper_plus_code = placeRequestDTO.outscrapper_plus_code
            this.outscrapper_area_service = placeRequestDTO.outscrapper_area_service
            this.outscrapper_rating = placeRequestDTO.outscrapper_rating
            this.outscrapper_reviews = placeRequestDTO.outscrapper_reviews
            this.outscrapper_reviews_link = placeRequestDTO.outscrapper_reviews_link
            this.outscrapper_reviews_tags = placeRequestDTO.outscrapper_reviews_tags
            this.outscrapper_reviews_per_score = placeRequestDTO.outscrapper_reviews_per_score
            this.outscrapper_photos_count = placeRequestDTO.outscrapper_photos_count
            this.outscrapper_photo = placeRequestDTO.outscrapper_photo
            this.outscrapper_street_view = placeRequestDTO.outscrapper_street_view
            this.outscrapper_located_in = placeRequestDTO.outscrapper_located_in
            this.outscrapper_json_working_hours = placeRequestDTO.outscrapper_json_working_hours// JSON
            this.outscrapper_json_other_hours = placeRequestDTO.outscrapper_json_other_hours// JSON
            this.outscrapper_json_popular_times = placeRequestDTO.outscrapper_json_popular_times // JSON
            this.outscrapper_business_status = placeRequestDTO.outscrapper_business_status
            this.outscrapper_json_about = placeRequestDTO.outscrapper_json_about// JSON
            this.outscrapper_range = placeRequestDTO.outscrapper_range
            this.outscrapper_posts = placeRequestDTO.outscrapper_posts
            this.outscrapper_logo = placeRequestDTO.outscrapper_logo
            this.outscrapper_description = placeRequestDTO.outscrapper_description
            this.outscrapper_verified = placeRequestDTO.outscrapper_verified
            this.outscrapper_owner_id = placeRequestDTO.outscrapper_owner_id
            this.outscrapper_owner_title = placeRequestDTO.outscrapper_owner_title
            this.outscrapper_owner_link = placeRequestDTO.outscrapper_owner_link
            this.outscrapper_reservation_links = placeRequestDTO.outscrapper_reservation_links
            this.outscrapper_booking_appointment_link = placeRequestDTO.outscrapper_booking_appointment_link
            this.outscrapper_menu_link = placeRequestDTO.outscrapper_menu_link
            this.outscrapper_order_links = placeRequestDTO.outscrapper_order_links
            this.outscrapper_location_link = placeRequestDTO.outscrapper_location_link
            this.outscrapper_place_id = placeRequestDTO.outscrapper_place_id
            this.outscrapper_google_id = placeRequestDTO.outscrapper_google_id
            this.outscrapper_cid = placeRequestDTO.outscrapper_cid
            this.outscrapper_reviews_id = placeRequestDTO.outscrapper_reviews_id
            this.outscrapper_located_google_id = placeRequestDTO.outscrapper_located_google_id
        }
        try {
            val newPlace = placeRepository.saveAndFlush(place) // getting id by flushing
            return PlaceId(newPlace.placeId!!)
        } catch (e: DataIntegrityViolationException) {
            var t: Throwable? = e.cause
            while (t != null) {
                if (ConstraintViolationException::class.java == t.javaClass) {
                    throw ResponseStatusException(HttpStatus.CONFLICT, "이미 중복된 장소가 등록되어 있습니다.")
                }

                t = t.cause
            }
            throw e
        }
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    override fun getPlacelist(
        searchTerm: String?,
        category: String?,
        cityId: Long?,
        projectId: Long,
        page: Pageable
    ): PageableData<PlaceBrief> {
        val project = projectService.findReadOnlyProject(projectId)

        val placeIds = getPlaceIdFromSearch(
            searchTerm,
            category,
            cityId,
            project.country!!.countryId!!,
            page.offset,
            page.pageSize
        )

        val result = placeRepository.getPlacelistAndIsLikedBySearchTerm(page, project, placeIds.data)

        return PageableData(result, placeIds.total)
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    override fun getPlaceDetail(placeId: Long, projectId: Long): PlaceDetail {
        val result = placeRepository.getPlaceDetailAndIsLiked(placeId, projectId)
        val p = result.place
        val detail = PlaceDetail(
            PlaceDetailInfo(
                p.outscrapper_name,
                p.outscrapper_site,
                p.outscrapper_type,
                p.outscrapper_json_subtypes,
                p.outscrapper_phone,
                p.outscrapper_full_address,
                p.outscrapper_latitude,
                p.outscrapper_longitude,
                p.outscrapper_rating,
                p.outscrapper_photo,
                p.outscrapper_range,
                p.outscrapper_description,
                p.outscrapper_json_about,
                p.outscrapper_location_link,
                p.outscrapper_json_working_hours
            ), p.placeId, result.isLiked
        )

        return detail
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    override fun getNearbyPlace(placeId: Long, projectId: Long): List<PlaceBrief> {
        val EARTH_RADIUS_KM = 6371

        val project = projectService.findReadOnlyProject(projectId)
        val place = placeRepository.getPlaceDetailAndIsLiked(placeId, projectId) // TODO: Fix this

        val latitudeTolerance = 180 / (EARTH_RADIUS_KM * PI * 2) // 500M, 0.0045
        val longitudeTolerance =
            180 / (EARTH_RADIUS_KM * PI * cos(place.place.outscrapper_latitude!! * PI / 180) * 2) // 500M, 0.0059

        return placeRepository.getNearbyPlaceByPlaceId(
            placeId,
            project,
            place.place.outscrapper_latitude!!,
            place.place.outscrapper_longitude!!,
            latitudeTolerance,
            longitudeTolerance,
            limit = 10
        )
    }

    override fun getPlaceIdFromSearch(
        searchTerm: String?,
        category: String?,
        cityId: Long?,
        countryId: Long,
        pageOffset: Long,
        pageSize: Int
    ): PageableData<Long> {
        return placeCacheService.getPlaceIdFromSearch(searchTerm, category, cityId, countryId, pageOffset, pageSize)
    }
}