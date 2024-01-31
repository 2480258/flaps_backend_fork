package com.trift.backend.domain

import io.hypersistence.utils.hibernate.type.json.JsonType
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.Type
import org.hibernate.annotations.UpdateTimestamp
import java.util.*

@Entity
@Table(indexes = [Index(name = "i_sample_project_id", columnList = "projectId")])
class SampleProject {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    var sampleProjectId: Long? = null

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "projectId", nullable = false)
    var project: Project? = null

    @Column(nullable = true, columnDefinition = "json") @Type(value = JsonType::class)
    var extraInfo: Map<String, Any?>? = null

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(updatable = false, nullable = false)
    val createDate: Date? = null

    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    val updateDate: Date? = null
}