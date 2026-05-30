package com.example.bowlyApp.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "meal_recipes")
data class MealRecipe(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    var name: String,

    var description: String? = null,
    
    var tags: String? = null,

    @Column(nullable = false)
    val source: String,

    @Column(name = "external_id")
    val externalId: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    val user: User? = null,

    @Column(name = "is_single_meal", nullable = false)
    var isSingleMeal: Boolean = false,

    @Column(name = "created_at", insertable = false, updatable = false)
    val createdAt: LocalDateTime? = null,

    @OneToMany(mappedBy = "recipe", cascade = [CascadeType.ALL], orphanRemoval = true)
    val ingredients: MutableList<RecipeIngredient> = mutableListOf()
)
