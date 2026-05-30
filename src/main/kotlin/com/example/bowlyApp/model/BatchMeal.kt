package com.example.bowlyApp.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "batch_meals")
data class BatchMeal(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val name: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id")
    val recipe: MealRecipe? = null,

    @Column(name = "is_depleted", nullable = false)
    var isDepleted: Boolean = false,

    @Column(name = "created_at", insertable = false, updatable = false)
    val createdAt: LocalDateTime? = null,

    @OneToMany(mappedBy = "batchMeal", cascade = [CascadeType.ALL], orphanRemoval = true)
    val segments: MutableList<BatchMealSegment> = mutableListOf()
)
