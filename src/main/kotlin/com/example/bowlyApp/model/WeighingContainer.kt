package com.example.bowlyApp.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "weighing_containers")
data class WeighingContainer(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val name: String,

    @Column(nullable = false)
    val type: String,

    @Column(name = "weight_g", nullable = false)
    val weightG: Double,

    @Column(name = "image_base64", columnDefinition = "TEXT")
    val imageBase64: String? = null,

    @Column(name = "created_at", insertable = false, updatable = false)
    val createdAt: LocalDateTime? = null
)
