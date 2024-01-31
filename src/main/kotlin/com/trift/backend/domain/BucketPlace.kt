package com.trift.backend.domain

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.util.*

enum class BucketPlaceStatus(val status: Long) {
    OK(0), EXPIRED(1)
} // backing field due to aggregation operations in DB

@Entity
@Table(indexes = [Index(name = "i_project_Id", columnList = "projectId"), Index(name = "i_place_Id",columnList = "placeId")])
class BucketPlace {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    var bucketPlaceId: Long? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "projectId", nullable = false)
    var project: Project? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "placeId", nullable = false)
    var place: Place? = null

    @Column(nullable = false)
    @Enumerated(EnumType.ORDINAL)
    var bucketPlaceStatus: BucketPlaceStatus? = null

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(updatable = false, nullable = false)
    val createDate: Date? = null

    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    val updateDate: Date? = null
}