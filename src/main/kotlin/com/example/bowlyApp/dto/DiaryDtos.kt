package com.example.bowlyApp.dto

import java.time.LocalDate

data class ConsumeProductRequest(
    val product: ProductSearchResult, // Zmieniono z productId na cały obiekt produktu
    val weightG: Double,
    val mealDate: String,
    val mealType: String
)

data class DailySummaryDto(
    val date: LocalDate,
    val totalKcal: Double,
    val totalProtein: Double,
    val totalFat: Double,
    val totalCarbs: Double,
    val burnedKcal: Double = 0.0,
    val workouts: List<WorkoutActivityDto> = emptyList(),
    val meals: Map<String, MealSummaryDto>
)

data class MealSummaryDto(
    val mealType: String,
    val totalKcal: Double,
    val totalProtein: Double,
    val totalFat: Double,
    val totalCarbs: Double,
    val portions: List<ConsumedPortionDto>
)

data class ConsumedPortionDto(
    val id: Long,
    val segmentName: String?,
    val batchMealName: String?,
    val productName: String?, // Nowe pole dla zwyklego produktu
    val consumedWeightG: Double,
    val kcal: Double,
    val protein: Double,
    val fat: Double,
    val carbs: Double
)
