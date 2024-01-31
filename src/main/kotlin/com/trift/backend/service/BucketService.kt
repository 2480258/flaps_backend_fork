package com.trift.backend.service

import com.trift.backend.domain.BucketHistory
import com.trift.backend.domain.BucketPlace
import com.trift.backend.domain.BucketPlaceStatus
import com.trift.backend.domain.PlaceBrief
import com.trift.backend.repository.BucketHistoryRepository
import com.trift.backend.repository.BucketPlaceRepository
import com.trift.backend.repository.PlaceRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

interface BucketService {
    fun addBucketItem(projectId: Long, placeId: Long): List<PlaceBrief>
    fun deleteBucketItem(projectId: Long, placeId: Long) : List<PlaceBrief>
    fun getBucketItem(projectId: Long): List<PlaceBrief>
}

@Component
class BucketServiceImpl : BucketService {

    @Autowired
    lateinit var projectService: ProjectService

    @Autowired
    lateinit var bucketPlaceRepository: BucketPlaceRepository

    @Autowired
    lateinit var bucketHistoryRepository: BucketHistoryRepository

    @Autowired
    lateinit var placeRepository: PlaceRepository

    val action_AddBucket = "버킷에 아이템 추가"
    val action_DeleteBucket = "버킷에 아이템 삭제"

    val bucket_CountLimit = 50

    @Transactional(isolation = Isolation.SERIALIZABLE)
    override fun addBucketItem(projectId: Long, placeId: Long) : List<PlaceBrief> {
        val currentProject = projectService.findReadWriteProject(projectId)
        val bucketCount = bucketPlaceRepository.countBucketPlaceByBucketId(projectId)
        val bucketPlace = bucketPlaceRepository.findBucketPlaceByBucketIdAndPlaceId(currentProject.projectId!!, placeId)

        val place = placeRepository.findById(placeId).orElseThrow {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "해당 Place ID가 없음")
        }

        if(bucketPlace != null) {
            throw ResponseStatusException(HttpStatus.NOT_MODIFIED, "해당 Place ID가 이미 Bucket에 등록되어 있음")
        }

        if(bucketCount >= bucket_CountLimit) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "버킷 한도 초과")
        }

        val newBucketPlace = BucketPlace().apply {
            this.project = currentProject
            this.place = place
            this.bucketPlaceStatus = BucketPlaceStatus.OK
        }

        val newBucketHistory = BucketHistory().apply {
            this.project = currentProject
            this.place = place
            this.action = action_AddBucket
        }

        bucketPlaceRepository.save(newBucketPlace)
        bucketHistoryRepository.save(newBucketHistory)

        return getBucketItem(projectId)
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    override fun deleteBucketItem(projectId: Long, placeId: Long) : List<PlaceBrief> {
        val currentProject = projectService.findReadWriteProject(projectId)

        val bucketPlace = bucketPlaceRepository.findBucketPlaceByBucketIdAndPlaceId(currentProject!!.projectId!!, placeId)

        val place = placeRepository.findById(placeId).orElseThrow {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "해당 Place ID가 없음")
        }

        if (bucketPlace == null) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "해당 Place가 Bucket에 들어가있지 않음")
        } else {
            bucketPlace.bucketPlaceStatus = BucketPlaceStatus.EXPIRED
        }

        val newBucketHistory = BucketHistory().apply {
            this.project = currentProject
            this.place = place
            this.action = action_DeleteBucket
        }

        bucketHistoryRepository.save(newBucketHistory)
        return getBucketItem(projectId)
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    override fun getBucketItem(projectId: Long): List<PlaceBrief> {
        val currentProject = projectService.findReadOnlyProject(projectId)
        val places = bucketPlaceRepository.findPlaceBriefBybucketId(currentProject.projectId!!)

        return places
    }
}
