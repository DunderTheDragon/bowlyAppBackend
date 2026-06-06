package com.example.bowlyApp.model

import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "consumed_portions")
data class ConsumedPortion(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "segment_id")
    val segment: BatchMealSegment? = null,

    // Dodano możliwość zjedzenia "zwykłego" produktu bez patelni
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    var product: Product? = null,

    @Column(name = "consumed_weight_g", nullable = false)
    var consumedWeightG: Double,

    @Column(name = "meal_date", nullable = false)
    val mealDate: LocalDate,

    @Column(name = "meal_type", nullable = false)
    var mealType: String,

    @Column(name = "weight_basis_g")
    val weightBasisG: Double? = null,

    @Column(name = "created_at", insertable = false, updatable = false)
    val createdAt: LocalDateTime? = null
)
