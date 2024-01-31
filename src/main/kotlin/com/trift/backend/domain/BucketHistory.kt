package com.trift.backend.domain

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.util.*

@Entity
@Table
class BucketHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    var bucketHistoryId: Long? = null


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="projectId", nullable = false)
    var project: Project? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="placeId", nullable = false)
    var place: Place? = null

    @Column(nullable = false)
    var action: String? = null

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(updatable = false, nullable = false)
    private val createDate: Date? = null


}