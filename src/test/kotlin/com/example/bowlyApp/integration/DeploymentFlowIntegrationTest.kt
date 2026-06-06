package com.example.bowlyApp.integration

import com.example.bowlyApp.dto.BatchMealDto
import com.example.bowlyApp.dto.DailySummaryDto
import com.example.bowlyApp.dto.ProductSearchResult
import com.example.bowlyApp.support.IntegrationTestBase
import com.example.bowlyApp.support.RestTestHelper
import com.example.bowlyApp.support.TestFixtures
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.core.ParameterizedTypeReference

/**
 * Test wdrożeniowy — pełny scenariusz użytkownika od rejestracji do dziennika.
 */
class DeploymentFlowIntegrationTest : IntegrationTestBase() {

    @Test
    fun `happy path rejestracja produkt patelnia porcja dziennik`() {
        val token = RestTestHelper.registerAndGetToken(rest, username = "deploy_user")

        val product = ProductSearchResult(
            name = "Indyk",
            source = "LOCAL",
            calories = 135.0,
            protein = 30.0,
            fat = 1.0,
            carbohydrates = 0.0
        )
        val productId = requireNotNull(
            RestTestHelper.post(
                rest,
                "/api/products/local",
                token,
                product,
                ProductSearchResult::class.java
            )?.id
        )

        val batch = RestTestHelper.post(
            rest,
            "/api/batch-meals",
            token,
            TestFixtures.createBatchMealRequest(
                name = "Meal prep",
                productId = productId,
                weightG = 1200.0
            ),
            BatchMealDto::class.java
        )

        val segmentId = requireNotNull(batch?.segments?.first()?.id)

        RestTestHelper.postEmpty(
            rest,
            "/api/batch-meals/consume",
            token,
            TestFixtures.consumePortionRequest(segmentId = segmentId, weightG = 300.0, mealType = "DINNER")
        )

        val diary = RestTestHelper.get(
            rest,
            "/api/diary/daily?date=2026-06-06",
            token,
            DailySummaryDto::class.java
        )

        assertNotNull(diary?.meals?.get("DINNER"))

        val active = RestTestHelper.getList(
            rest,
            "/api/batch-meals/active",
            token,
            object : ParameterizedTypeReference<List<BatchMealDto>>() {}
        )

        val batchId = requireNotNull(batch?.id)
        val activeBatch = active?.first { it.id == batchId }
        assertEquals(900.0, activeBatch?.segments?.first()?.currentWeightG)

        RestTestHelper.postEmpty(
            rest,
            "/api/workouts",
            token,
            TestFixtures.createWorkoutRequest()
        )

        val diaryWithWorkout = RestTestHelper.get(
            rest,
            "/api/diary/daily?date=2026-06-06",
            token,
            DailySummaryDto::class.java
        )

        assertEquals(300.0, diaryWithWorkout?.burnedKcal)
    }
}
