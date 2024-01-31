package com.trift.backend.domain

import io.hypersistence.utils.hibernate.type.json.JsonType
import jakarta.persistence.*
import org.hibernate.annotations.Type

@Entity
@Table(
    name = "TriftPlace",
    indexes = [
        Index(name = "i_reviews", columnList = "outscrapper_reviews"),
        Index(name = "i_name", columnList = "search_without_space"),
        Index(name = "i_chosung", columnList = "search_chosung_without_space"),
        Index(name = "i_reviews_with_name", columnList = "outscrapper_reviews, outscrapper_name"),
        Index(name = "i_lat_long", columnList = "outscrapper_latitude, outscrapper_longitude")],
    uniqueConstraints = [
        UniqueConstraint(columnNames = arrayOf("outscrapper_place_id")),
        UniqueConstraint(columnNames = arrayOf("outscrapper_google_id"))
    ]
)
class Place {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    var placeId: Long? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cityId", nullable = false)
    var city: City? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "countryId", nullable = false)
    var country: Country? = null

    @Column(nullable = false)
    var search_without_space: String? = null

    @Column(nullable = true)
    var search_chosung_without_space: String? = null

    @Column(nullable = false)
    var outscrapper_queried_category: String? = null;

    @Column(nullable = false)
    var outscrapper_query: String? = null;

    @Column(nullable = false)
    var outscrapper_name: String? = null;

    @Column(nullable = true, length = 2048)
    var outscrapper_site: String? = null;

    @Column(nullable = false)
    var outscrapper_type: String? = null;

    @Column(nullable = false, columnDefinition = "json")
    @Type(value = JsonType::class)
    var outscrapper_json_subtypes: List<Any>? = null; // JSON

    @Column(nullable = true)
    var outscrapper_category: String? = null;

    @Column(nullable = true)
    var outscrapper_phone: String? = null;

    @Column(nullable = false)
    var outscrapper_full_address: String? = null;

    @Column(nullable = false)
    var outscrapper_borough: String? = null;

    @Column(nullable = true)
    var outscrapper_street: String? = null;

    @Column(nullable = true)
    var outscrapper_city: String? = null;

    @Column(nullable = false)
    var outscrapper_postal_code: String? = null;

    @Column(nullable = true)
    var outscrapper_state: String? = null;

    @Column(nullable = true)
    var outscrapper_us_state: String? = null;

    @Column(nullable = true)
    var outscrapper_country: String? = null;

    @Column(nullable = true)
    var outscrapper_country_code: String? = null;

    @Column(nullable = false)
    var outscrapper_latitude: Double? = null;

    @Column(nullable = false)
    var outscrapper_longitude: Double? = null;

    @Column(nullable = true)
    var outscrapper_time_zone: String? = null;

    @Column(nullable = true)
    var outscrapper_plus_code: String? = null;

    @Column(nullable = true)
    var outscrapper_area_service: Boolean? = null;

    @Column(nullable = true)
    var outscrapper_rating: Double? = null;

    @Column(nullable = true)
    var outscrapper_reviews: Long? = null;

    @Column(nullable = true, length = 2048)
    var outscrapper_reviews_link: String? = null;

    @Column(nullable = true)
    var outscrapper_reviews_tags: String? = null;

    @Column(nullable = true, columnDefinition = "json")
    @Type(value = JsonType::class)
    var outscrapper_reviews_per_score: Map<String, Any>? = null;

    @Column(nullable = true)
    var outscrapper_photos_count: Long? = null;

    @Column(nullable = false, length = 2048)
    var outscrapper_photo: String? = null;

    @Column(nullable = true, length = 2048)
    var outscrapper_street_view: String? = null;

    @Column(nullable = true)
    var outscrapper_located_in: String? = null;

    @Column(nullable = true, columnDefinition = "json")
    @Type(value = JsonType::class)
    var outscrapper_json_working_hours: Map<String, Any>? = null; // JSON

    @Column(nullable = true, columnDefinition = "json")
    @Type(value = JsonType::class)
    var outscrapper_json_other_hours: List<Map<String, Any>>? = null; // JSON

    @Column(nullable = true, columnDefinition = "json")
    @Type(value = JsonType::class)
    var outscrapper_json_popular_times: List<Any>? = null; // JSON

    @Column(nullable = true)
    var outscrapper_business_status: String? = null;

    @Column(nullable = true, columnDefinition = "json")
    @Type(value = JsonType::class)
    var outscrapper_json_about: Map<String, Any>? = null; // JSON

    @Column(nullable = true)
    var outscrapper_range: String? = null;

    @Column(nullable = true)
    var outscrapper_posts: String? = null;

    @Column(nullable = true)
    var outscrapper_logo: String? = null;

    @Column(nullable = true)
    var outscrapper_description: String? = null;

    @Column(nullable = true)
    var outscrapper_verified: Boolean? = null;

    @Column(nullable = true)
    var outscrapper_owner_id: String? = null;

    @Column(nullable = true)
    var outscrapper_owner_title: String? = null;

    @Column(nullable = true, length = 2048)
    var outscrapper_owner_link: String? = null;

    @Column(nullable = true, columnDefinition = "json")
    @Type(value = JsonType::class)
    var outscrapper_reservation_links: List<String>? = null;

    @Column(nullable = true, length = 2048)
    var outscrapper_booking_appointment_link: String? = null;

    @Column(nullable = true, length = 2048)
    var outscrapper_menu_link: String? = null;

    @Column(nullable = true, columnDefinition = "json")
    @Type(value = JsonType::class)
    var outscrapper_order_links: List<String>? = null;

    @Column(nullable = true, length = 2048)
    var outscrapper_location_link: String? = null;

    @Column(nullable = false)
    var outscrapper_place_id: String? = null;

    @Column(nullable = false)
    var outscrapper_google_id: String? = null;

    @Column(nullable = true)
    var outscrapper_cid: String? = null;

    @Column(nullable = true)
    var outscrapper_reviews_id: String? = null;

    @Column(nullable = true)
    var outscrapper_located_google_id: String? = null;
}