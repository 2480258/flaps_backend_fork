package com.trift.backend.repository

import com.querydsl.core.BooleanBuilder
import com.querydsl.core.QueryResults
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.jpa.JPAExpressions
import com.trift.backend.domain.*
import com.trift.backend.service.HangulString
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
import org.springframework.http.HttpStatus
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

interface PlaceRepository : PlaceRepositoryCustom, JpaRepository<Place, Long> {
}

interface PlaceRepositoryCustom {
    @Transactional(propagation = Propagation.MANDATORY)
    fun getPlaceDetailAndIsLiked(placeId: Long, projectId: Long): PlaceWithIsLiked

    @Transactional(propagation = Propagation.MANDATORY)
    fun getPlaceIdFromFullSearch(
        pageOffset: Long,
        pageSize: Int,
        search: PlaceSearch,
        countryId: Long
    ): PageableData<Long>

    @Transactional(propagation = Propagation.MANDATORY)
    fun getPlacelistAndIsLikedBySearchTerm(
        page: Pageable,
        project: Project,
        placeList: List<Long>
    ): List<PlaceBrief>

    @Transactional(propagation = Propagation.MANDATORY)
    fun getPlacelist(
        placeList: Set<Long>
    ): List<PlaceLocation>

    @Transactional(propagation = Propagation.MANDATORY)
    fun getNearbyPlaceByPlaceId(
        placeId: Long,
        project: Project,
        targetLatitude: Double,
        targetLongitude: Double,
        latitudeTolerance: Double,
        longitudeTolerance: Double,
        limit: Long
    ): List<PlaceBrief>

    @Transactional(propagation = Propagation.MANDATORY)
    fun getGooglePlaceIdByPlaceId(placeList: List<Long>): List<GooglePlaceId>
}

data class PlaceSearch constructor(
    val searchTerm: HangulString?,
    val category: String?,
    val city: City?
)

class PlaceRepositoryCustomImpl : PlaceRepositoryCustom, QuerydslRepositorySupport(Place::class.java) {


    @Transactional(propagation = Propagation.MANDATORY)
    override fun getPlaceDetailAndIsLiked(placeId: Long, projectId: Long): PlaceWithIsLiked {
        val qPlace = QPlace.place
        val qBucketPlace = QBucketPlace.bucketPlace
        val place = findPlaceById(qPlace, placeId) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "장소를 찾을 수 없음")

        val isLiked: Long? = from(qBucketPlace) // can be null if not exist in bucket
            .select(qBucketPlace.bucketPlaceStatus.castToNum(Long::class.java).min())
            .where(qBucketPlace.place.placeId.eq(placeId).and(qBucketPlace.project.projectId.eq(projectId)))
            .groupBy(qBucketPlace.place.placeId)
            .fetchOne()

        return PlaceWithIsLiked(place, isLiked == BucketPlaceStatus.OK.status)
    }

    override fun getPlaceIdFromFullSearch(
        pageOffset: Long,
        pageSize: Int,
        search: PlaceSearch,
        countryId: Long
    ): PageableData<Long> {
        val qPlace = QPlace.place
        val searchTerm =
            getWhereConditionsFromCountyAndCityAndCategory(qPlace, countryId, search.city, search.category)

        val result = if (search.searchTerm == null) {
            getPlaceIdsBySearchTerm(qPlace, pageOffset, pageSize, searchTerm)
        } else if ((search.searchTerm.flatAsInitialWithoutSpace() != null) and (search.searchTerm.flatAsInitialWithoutSpace() == search.searchTerm.explodeWithoutSpace())) { // 초성 검색의 경우
            getPlaceIdsBySearchTerm(
                qPlace,
                pageOffset,
                pageSize,
                searchTerm.and(getWhereConditionFromChosungText(qPlace, search.searchTerm))
            )
        } else {
            getPlaceIdsBySearchTerm(
                qPlace,
                pageOffset,
                pageSize,
                searchTerm.and(getWhereConditionFromFullText(qPlace, search.searchTerm))
            )
        }

        return PageableData(result.results, result.total)
    }

    private fun PlaceRepositoryCustomImpl.findPlaceById(
        qPlace: QPlace,
        placeId: Long
    ): Place? = from(qPlace)
        .select(qPlace)
        .where(qPlace.placeId.eq(placeId))
        .fetchOne()

    private fun getWhereConditionFromFullText(qPlace: QPlace, search: HangulString?): BooleanBuilder? {
        val rootBooleanCondition = BooleanBuilder()

        if (search == null) {
            return null
        }

        return rootBooleanCondition.and(
            qPlace.search_without_space.lower().contains(search.explodeWithoutSpace().lowercase())
        )
    }

    private fun getWhereConditionFromChosungText(qPlace: QPlace, search: HangulString?): BooleanBuilder? {
        val rootBooleanCondition = BooleanBuilder()

        if (search == null) {
            return null
        }

        return rootBooleanCondition.and(qPlace.search_chosung_without_space.lower().contains(search.flatAsInitialWithoutSpace()?.lowercase()))
    }

    private fun getWhereConditionsFromCountyAndCityAndCategory(
        qPlace: QPlace,
        countryId: Long,
        city: City?,
        category: String?
    ): BooleanBuilder {
        val rootBooleanCondition = BooleanBuilder()

        rootBooleanCondition.and(qPlace.country.countryId.eq(countryId))

        if (category != null) {
            rootBooleanCondition.and(qPlace.outscrapper_queried_category.eq(category))
        }

        if (city != null) {
            rootBooleanCondition.and(qPlace.city.eq(city))
        }

        return rootBooleanCondition
    }

    private fun orderByReviewCount(qPlace: QPlace): OrderSpecifier<Long> {
        return qPlace.outscrapper_reviews.desc().nullsLast()
    }

    private fun orderByPlaceName(qPlace: QPlace): OrderSpecifier<String> {
        return qPlace.outscrapper_name.asc()
    }


    @Transactional(propagation = Propagation.MANDATORY)
    override fun getPlacelistAndIsLikedBySearchTerm(
        page: Pageable,
        project: Project,
        placeList: List<Long>
    ): List<PlaceBrief> {
        val qPlace = QPlace.place
        val result = getPlacelistAndIsLikedBySearchTerm(qPlace, placeList, project)

        return result.results
    }

    override fun getPlacelist(placeList: Set<Long>): List<PlaceLocation> {
        val qPlace = QPlace.place
        val result = from(qPlace)
            .select(QPlaceLocation(qPlace.placeId, qPlace.outscrapper_latitude, qPlace.outscrapper_longitude))
            .where(qPlace.placeId.`in`(placeList))
            .fetchResults()

        return result.results
    }

    private fun getPlaceIdsBySearchTerm(
        qPlace: QPlace,
        pageOffset: Long,
        pageSize: Int,
        filter: BooleanBuilder
    ): QueryResults<Long> {
        val result = from(qPlace)
            .select(
                qPlace.placeId
            )
            .where(filter.value)
            .orderBy(orderByReviewCount(qPlace), orderByPlaceName(qPlace))
            .offset(pageOffset)
            .limit(pageSize.toLong())
            .fetchResults()

        return result
    }

    private fun getPlacelistAndIsLikedBySearchTerm(
        qPlace: QPlace,
        placeList: List<Long>,
        project: Project
    ): QueryResults<PlaceBrief> {

        val qBucketPlace = QBucketPlace.bucketPlace

        val result = from(qPlace)
            .select(
                buildPlaceBrief(qPlace, qBucketPlace, project)
            )
            .where(qPlace.placeId.`in`(placeList))
            .orderBy(orderByReviewCount(qPlace), orderByPlaceName(qPlace))
            .fetchResults()

        return result
    }

    override fun getNearbyPlaceByPlaceId(
        placeId: Long,
        project: Project,
        targetLatitude: Double,
        targetLongitude: Double,
        latitudeTolerance: Double,
        longitudeTolerance: Double,
        limit: Long
    ): List<PlaceBrief> {
        val qPlace = QPlace.place
        val qBucketPlace = QBucketPlace.bucketPlace

        val result = from(qPlace)
            .select(
                buildPlaceBrief(qPlace, qBucketPlace, project)
            )
            .where( // get result from N km * N km square, where the centre is target place
                getWhereConditionsFromCountyAndCityAndCategory(qPlace, project.country!!.countryId!!, null, null).and(
                    qPlace.outscrapper_latitude.between(
                        targetLatitude - latitudeTolerance,
                        targetLatitude + latitudeTolerance
                    )
                ).and(
                    qPlace.outscrapper_longitude.between(
                        targetLongitude - longitudeTolerance,
                        targetLongitude + longitudeTolerance
                    )
                ).and(
                    qPlace.placeId.ne(placeId)
                )
            )
            .orderBy(orderByReviewCount(qPlace))
            .limit(limit)
            .fetchResults()

        return result.results
    }

    override fun getGooglePlaceIdByPlaceId(placeList: List<Long>): List<GooglePlaceId> {
        val qPlace = QPlace.place

        val result = from(qPlace).select(QGooglePlaceId(qPlace.placeId, qPlace.outscrapper_place_id))
            .where(qPlace.placeId.`in`(placeList))
            .fetchResults()

        return result.results
    }

    private fun buildPlaceBrief(
        qPlace: QPlace,
        qBucketPlace: QBucketPlace,
        project: Project
    ) = QPlaceBrief(
        qPlace.placeId,
        JPAExpressions.select(qBucketPlace.isNotNull).from(qBucketPlace).where(
            qBucketPlace.place.eq(qPlace)
                .and(qBucketPlace.project.eq(project))
                .and(qBucketPlace.bucketPlaceStatus.eq(BucketPlaceStatus.OK))
        ),
        qPlace.outscrapper_name,
        qPlace.outscrapper_photo,
        qPlace.outscrapper_description,
        qPlace.outscrapper_type,
        qPlace.outscrapper_latitude,
        qPlace.outscrapper_longitude,
        qPlace.outscrapper_rating,
        qPlace.outscrapper_reviews_per_score,
        JPAExpressions.select(qBucketPlace.createDate).from(qBucketPlace).where(
            qBucketPlace.place.eq(qPlace)
                .and(qBucketPlace.project.eq(project))
                .and(qBucketPlace.bucketPlaceStatus.eq(BucketPlaceStatus.OK))
        )
    )
}