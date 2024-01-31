package com.trift.backend.repository

import com.querydsl.core.types.dsl.Expressions
import com.trift.backend.domain.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

interface BucketPlaceRepository : BucketPlaceRepositoryCustom, JpaRepository<BucketPlace, Long> {

}

interface BucketPlaceRepositoryCustom {
    @Transactional(propagation = Propagation.MANDATORY)
    fun findPlaceBriefBybucketId(projectId: Long): List<PlaceBrief>

    @Transactional(propagation = Propagation.MANDATORY)
    fun findBucketPlaceByBucketIdAndPlaceId(projectId: Long, placeId: Long): BucketPlace?

    @Transactional(propagation = Propagation.MANDATORY)
    fun countBucketPlaceByBucketId(projectId: Long): Long
}

class BucketPlaceRepositoryCustomImpl : BucketPlaceRepositoryCustom,
    QuerydslRepositorySupport(BucketPlace::class.java) {
    @Transactional(propagation = Propagation.MANDATORY)
    override fun findPlaceBriefBybucketId(projectId: Long): List<PlaceBrief> {
        val qBucketPlace = QBucketPlace.bucketPlace
        val qPlace = QPlace.place
        val qProject = QProject.project

        val result = from(qPlace)
            .leftJoin(qPlace).on(qPlace.placeId.eq(qBucketPlace.place.placeId))
            .leftJoin(qBucketPlace).on(qBucketPlace.project.projectId.eq(qProject.projectId))
            .select(
                QPlaceBrief(
                    qPlace.placeId,
                    Expressions.TRUE,
                    qPlace.outscrapper_name,
                    qPlace.outscrapper_photo,
                    qPlace.outscrapper_description,
                    qPlace.outscrapper_type,
                    qPlace.outscrapper_latitude,
                    qPlace.outscrapper_longitude,
                    qPlace.outscrapper_rating,
                    qPlace.outscrapper_reviews_per_score,
                    qBucketPlace.createDate
                )
            )
            .where(
                qBucketPlace.project.projectId.eq(projectId)
                    .and(qBucketPlace.bucketPlaceStatus.eq(BucketPlaceStatus.OK))
                    .and(qBucketPlace.place.eq(qPlace))
            )
            .orderBy(qPlace.outscrapper_name.asc().nullsLast())
            .fetch()

        return result
    }

    @Transactional(propagation = Propagation.MANDATORY)
    override fun findBucketPlaceByBucketIdAndPlaceId(projectId: Long, placeId: Long): BucketPlace? {
        val qBucketPlace = QBucketPlace.bucketPlace

        val result = from(qBucketPlace)
            .where(
                qBucketPlace.project.projectId.eq(projectId)
                    .and(qBucketPlace.bucketPlaceStatus.eq(BucketPlaceStatus.OK))
                    .and(qBucketPlace.place.placeId.eq(placeId))
            )
            .fetchOne()

        return result
    }

    override fun countBucketPlaceByBucketId(projectId: Long): Long {
        val qBucketPlace = QBucketPlace.bucketPlace

        val result = from(qBucketPlace)
            .where(
                qBucketPlace.project.projectId.eq(projectId)
                    .and(qBucketPlace.bucketPlaceStatus.eq(BucketPlaceStatus.OK))
            )
            .fetchCount()

        return result
    }
}