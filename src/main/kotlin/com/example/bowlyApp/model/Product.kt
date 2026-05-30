package com.example.bowlyApp.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "products")
data class Product(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val name: String,

    @Column(nullable = false)
    val source: String,

    @Column(name = "external_id")
    val externalId: String? = null,
    
    @Column(name = "barcode", unique = true)
    val barcode: String? = null,

    @Column(name = "kcal_per_100g", nullable = false)
    val kcalPer100g: Double,

    @Column(name = "protein_per_100g", nullable = false)
    val proteinPer100g: Double,

    @Column(name = "fat_per_100g", nullable = false)
    val fatPer100g: Double,

    @Column(name = "carbs_per_100g", nullable = false)
    val carbsPer100g: Double,

    @Column(name = "unit_name")
    val unitName: String? = null,

    @Column(name = "unit_weight_g")
    val unitWeightG: Double? = null,

    @Column(name = "created_at", insertable = false, updatable = false)
    val createdAt: LocalDateTime? = null
)