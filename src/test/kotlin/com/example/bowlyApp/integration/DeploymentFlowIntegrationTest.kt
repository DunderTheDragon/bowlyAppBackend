package com.example.bowlyApp.integration

import com.example.bowlyApp.dto.BatchMealDto
import com.example.bowlyApp.dto.DailySummaryDto
import com.example.bowlyApp.dto.ProductSearchResult
import com.example.bowlyApp.support.IntegrationTestBase
import com.example.bowlyApp.support.RestTestHelper
import com.example.bowlyApp.support.RestTestHelper.bearer
import com.example.bowlyApp.support.TestFixtures
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.MediaType

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
            rest.post()
                .uri("/api/products/local")
                .bearer(token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(product)
                .retrieve()
                .body(ProductSearchResult::class.java)?.id
        )

        val batch = rest.post()
            .uri("/api/batch-meals")
            .bearer(token)
            .contentType(MediaType.APPLICATION_JSON)
            .body(
                TestFixtures.createBatchMealRequest(
                    name = "Meal prep",
                    productId = productId,
                    weightG = 1200.0
                )
            )
            .retrieve()
            .body(BatchMealDto::class.java)

        val segmentId = requireNotNull(batch?.segments?.first()?.id)

        rest.post()
            .uri("/api/batch-meals/consume")
            .bearer(token)
            .contentType(MediaType.APPLICATION_JSON)
            .body(
                TestFixtures.consumePortionRequest(segmentId = segmentId, weightG = 300.0, mealType = "DINNER")
            )
            .retrieve()
            .toBodilessEntity()

        val diary = rest.get()
            .uri("/api/diary/daily?date=2026-06-06")
            .bearer(token)
            .retrieve()
            .body(DailySummaryDto::class.java)

        assertNotNull(diary?.meals?.get("DINNER"))

        val active = rest.get()
            .uri("/api/batch-meals/active")
            .bearer(token)
            .retrieve()
            .body(object : ParameterizedTypeReference<List<BatchMealDto>>() {})

        assertEquals(900.0, active?.first()?.segments?.first()?.currentWeightG)

        rest.post()
            .uri("/api/workouts")
            .bearer(token)
            .contentType(MediaType.APPLICATION_JSON)
            .body(TestFixtures.createWorkoutRequest())
            .retrieve()
            .toBodilessEntity()

        val diaryWithWorkout = rest.get()
            .uri("/api/diary/daily?date=2026-06-06")
            .bearer(token)
            .retrieve()
            .body(DailySummaryDto::class.java)

        assertEquals(300.0, diaryWithWorkout?.burnedKcal)
    }
}
