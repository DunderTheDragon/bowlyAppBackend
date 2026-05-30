package com.example.bowlyApp.model

import jakarta.persistence.*

@Entity
@Table(name = "batch_meal_segments")
data class BatchMealSegment(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_meal_id", nullable = false)
    val batchMeal: BatchMeal,

    @Column(nullable = false)
    val name: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    val product: Product? = null,

    @Column(name = "initial_weight_g", nullable = false)
    val initialWeightG: Double,

    @Column(name = "current_weight_g", nullable = false)
    var currentWeightG: Double,

    @Column(name = "total_kcal", nullable = false)
    val totalKcal: Double,

    @Column(name = "total_protein", nullable = false)
    val totalProtein: Double,

    @Column(name = "total_fat", nullable = false)
    val totalFat: Double,

    @Column(name = "total_carbs", nullable = false)
    val totalCarbs: Double
)
