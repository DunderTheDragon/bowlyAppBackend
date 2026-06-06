package com.example.bowlyApp.support

import com.example.bowlyApp.dto.*
import com.example.bowlyApp.model.*

object TestFixtures {

    const val REGISTRATION_SECRET = "test-registration-secret-min-32-chars"
    const val DEFAULT_PASSWORD = "secret12"

    fun registerRequest(
        username: String = "testuser",
        password: String = DEFAULT_PASSWORD
    ) = RegisterRequest(
        username = username,
        password = password,
        registrationSecret = REGISTRATION_SECRET
    )

    fun loginRequest(
        username: String = "testuser",
        password: String = DEFAULT_PASSWORD
    ) = LoginRequest(username = username, password = password)

    fun user(
        id: Long = 1L,
        username: String = "testuser",
        passwordHash: String = "hashed",
        role: String = "USER"
    ) = User(id = id, username = username, passwordHash = passwordHash, role = role)

    fun product(
        id: Long = 1L,
        name: String = "Kurczak",
        kcal: Double = 165.0,
        protein: Double = 31.0,
        fat: Double = 3.6,
        carbs: Double = 0.0
    ) = Product(
        id = id,
        name = name,
        source = "LOCAL",
        kcalPer100g = kcal,
        proteinPer100g = protein,
        fatPer100g = fat,
        carbsPer100g = carbs
    )

    fun productSearchResult(
        name: String = "Kurczak",
        calories: Double = 165.0,
        protein: Double = 31.0,
        fat: Double = 3.6,
        carbohydrates: Double = 0.0,
        id: String? = null,
        barcode: String? = null
    ) = ProductSearchResult(
        id = id,
        name = name,
        source = "LOCAL",
        barcode = barcode,
        calories = calories,
        protein = protein,
        fat = fat,
        carbohydrates = carbohydrates
    )

    fun batchMealSegment(
        batchMeal: BatchMeal,
        id: Long = 1L,
        name: String = "Sekcja 1",
        initialWeightG: Double = 1000.0,
        currentWeightG: Double = 1000.0,
        product: Product? = null
    ) = BatchMealSegment(
        id = id,
        batchMeal = batchMeal,
        name = name,
        product = product,
        initialWeightG = initialWeightG,
        currentWeightG = currentWeightG,
        rawWeightG = initialWeightG,
        totalKcal = 500.0,
        totalProtein = 50.0,
        totalFat = 20.0,
        totalCarbs = 30.0
    )

    fun batchMeal(
        id: Long = 1L,
        name: String = "Patelnia testowa",
        segments: MutableList<BatchMealSegment> = mutableListOf()
    ) = BatchMeal(id = id, name = name, segments = segments)

    fun createBatchMealRequest(
        name: String = "Patelnia testowa",
        segmentName: String = "Kurczak",
        weightG: Double = 500.0,
        productId: String? = "1",
        saveAsRecipe: Boolean? = false
    ) = CreateBatchMealRequest(
        name = name,
        saveAsRecipe = saveAsRecipe,
        segments = listOf(
            CreateBatchMealSegmentRequest(
                name = segmentName,
                productId = productId,
                initialWeightG = weightG,
                totalKcal = 500.0,
                totalProtein = 50.0,
                totalFat = 20.0,
                totalCarbs = 30.0
            )
        )
    )

    fun consumePortionRequest(
        segmentId: Long = 1L,
        weightG: Double = 100.0,
        mealType: String = "LUNCH"
    ) = ConsumePortionRequest(
        segmentId = segmentId,
        weightG = weightG,
        mealDate = "2026-06-06",
        mealType = mealType
    )

    fun consumeProductRequest(
        mealDate: String = "2026-06-06",
        weightG: Double = 150.0,
        mealType: String = "BREAKFAST"
    ) = ConsumeProductRequest(
        product = productSearchResult(name = "Jabłko", calories = 52.0, protein = 0.3, fat = 0.2, carbohydrates = 14.0),
        weightG = weightG,
        mealDate = mealDate,
        mealType = mealType
    )

    fun createWorkoutRequest(
        name: String = "Bieganie",
        calories: Double = 300.0,
        date: String = "2026-06-06"
    ) = CreateWorkoutActivityRequest(
        name = name,
        caloriesBurned = calories,
        activityDate = date
    )

    fun createContainerRequest(
        name: String = "Talerz",
        type: String = "PLATE",
        weightG: Double = 250.0
    ) = CreateWeighingContainerRequest(
        name = name,
        type = type,
        weightG = weightG
    )
}
