package com.example.bowlyApp.model

import jakarta.persistence.*

@Entity
@Table(name = "recipe_ingredients")
data class RecipeIngredient(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id", nullable = false)
    val recipe: MealRecipe,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    val product: Product,

    @Column(name = "weight_g", nullable = false)
    val weightG: Double,

    @Column(name = "section_name", nullable = false)
    val sectionName: String = "Główna część"
)
