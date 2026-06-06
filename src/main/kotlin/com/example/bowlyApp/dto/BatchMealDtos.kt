package com.example.bowlyApp.dto

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonProperty

data class BatchMealSegmentDto(
    val id: Long,
    val name: String,
    val product: ProductSearchResult?,
    val initialWeightG: Double,
    val currentWeightG: Double,
    val rawWeightG: Double? = null,
    val totalKcal: Double,
    val totalProtein: Double,
    val totalFat: Double,
    val totalCarbs: Double
)

data class BatchMealDto(
    val id: Long,
    val name: String,
    val recipeId: Long?,
    @JsonProperty("isDepleted")
    val isDepleted: Boolean,
    val segments: List<BatchMealSegmentDto>
)

data class CreateBatchMealRequest(
    val name: String,
    val recipeId: Long? = null,
    val saveAsRecipe: Boolean = false,
    val recipeSections: List<CreateRecipeSectionRequest> = emptyList(),
    val segments: List<CreateBatchMealSegmentRequest>
)

data class CreateBatchMealSegmentRequest(
    val name: String,
    val productId: String? = null,
    val product: ProductSearchResult? = null,
    val products: List<ProductSearchResult>? = null,
    val initialWeightG: Double,
    val totalKcal: Double? = null,
    val totalProtein: Double? = null,
    val totalFat: Double? = null,
    val totalCarbs: Double? = null
)

data class ConsumePortionRequest(
    val segmentId: Long,
    @JsonAlias("consumedWeightG")
    val weightG: Double,
    val mealDate: String? = null,
    val mealType: String // e.g. "BREAKFAST", "LUNCH", "DINNER", "SNACK"
)

data class UpdateSegmentCookedWeightRequest(
    val cookedWeightG: Double
)