package com.trift.backend.domain

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.util.*

@Entity
@Table
class Country {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    var countryId: Long? = null

    @Column(nullable = false)
    var name: String? = null

    @Column(nullable = false)
    var thumbnail: String? = null

    @Column(nullable = false)
    var isAvailable: Boolean? = null

    @Column(nullable = false)
    var longitude: Double? = null

    @Column(nullable = false)
    var latitude: Double? = null

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(updatable = false, nullable = false)
    val createDate: Date? = null

    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    val updateDate: Date? = null
}