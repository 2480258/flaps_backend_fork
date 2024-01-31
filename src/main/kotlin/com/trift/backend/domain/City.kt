package com.trift.backend.domain

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.util.*

@Entity
@Table
class City {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    var cityId: Long? = null

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

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="countryId", nullable = false)
    var country: Country? = null

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(updatable = false, nullable = false)
    val createDate: Date? = null

    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    val updateDate: Date? = null
}