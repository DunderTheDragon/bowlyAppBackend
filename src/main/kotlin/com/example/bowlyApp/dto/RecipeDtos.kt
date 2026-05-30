package com.example.bowlyApp.dto

data class RecipeIngredientDto(
    val productId: Long,
    val productName: String,
    val weightG: Double,
    val calories: Double = 0.0,
    val protein: Double = 0.0,
    val fat: Double = 0.0,
    val carbohydrates: Double = 0.0,
    val barcode: String? = null,
    val source: String? = null,
    val externalId: String? = null
)

data class RecipeSectionDto(
    val name: String,
    val ingredients: List<RecipeIngredientDto>
)

data class MealRecipeDto(
    val id: Long,
    val name: String,
    val description: String?,
    val tags: String?,
    val source: String,
    val userId: Long? = null,
    val username: String? = null,
    val isSingleMeal: Boolean = false,
    val sections: List<RecipeSectionDto>
)

data class CreateMealRecipeRequest(
    val name: String,
    val description: String? = null,
    val tags: String? = null,
    val isSingleMeal: Boolean = false,
    val sections: List<CreateRecipeSectionRequest>
)

data class CreateRecipeSectionRequest(
    val name: String,
    val ingredients: List<CreateRecipeIngredientRequest>
)

data class CreateRecipeIngredientRequest(
    val productId: Long? = null,
    val product: ProductSearchResult? = null,
    val weightG: Double
)
