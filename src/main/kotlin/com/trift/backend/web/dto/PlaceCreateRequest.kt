package com.trift.backend.web.dto

class PlaceCreateRequest {
    var countryId: Long? = null
    var cityId: Long? = null
    var queried_category: String? = null
    var query: String? = null
    var name: String? = null
    var site: String? = null
    var type: String? = null
    var subtypes: List<Any>? = null // JSON
    var category: String? = null
    var phone: String? = null
    var full_address: String? = null
    var borough: String? = null
    var street: String? = null
    var city: String? = null
    var postal_code: String? = null
    var state: String? = null
    var us_state: String? = null
    var country: String? = null
    var country_code: String? = null
    var latitude: Double? = null
    var longitude: Double? = null
    var time_zone: String? = null
    var plus_code: String? = null
    var area_service: Boolean? = null
    var rating: Double? = null
    var reviews: Long? = null
    var reviews_link: String? = null
    var reviews_tags: String? = null
    var reviews_per_score: Map<String, Any>? = null
    var photos_count: Long? = null
    var photo: String? = null
    var street_view: String? = null
    var located_in: String? = null
    var working_hours: Map<String, Any>? = null // JSON
    var other_hours: List<Map<String, Any>>? = null // JSON
    var popular_times: List<Any>? = null // JSON
    var business_status: String? = null
    var about: Map<String, Any>? = null // JSON
    var range: String? = null
    var posts: String? = null
    var logo: String? = null
    var description: String? = null
    var verified: Boolean? = null
    var owner_id: String? = null
    var owner_title: String? = null
    var owner_link: String? = null
    var reservation_links: List<String>? = null
    var booking_appointment_link: String? = null
    var menu_link: String? = null
    var order_links: List<String>? = null
    var location_link: String? = null
    var place_id: String? = null
    var google_id: String? = null
    var cid: String? = null
    var reviews_id: String? = null
    var located_google_id: String? = null
}