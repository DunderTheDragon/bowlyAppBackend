package com.example.bowlyApp.integration

import com.example.bowlyApp.dto.*
import com.example.bowlyApp.support.IntegrationTestBase
import com.example.bowlyApp.support.RestTestHelper
import com.example.bowlyApp.support.RestTestHelper.bearer
import com.example.bowlyApp.support.TestFixtures
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.MediaType

class BatchMealControllerIntegrationTest : IntegrationTestBase() {

    private lateinit var token: String
    private var productId: String = ""

    @BeforeEach
    fun setUp() {
        token = RestTestHelper.registerAndGetToken(rest, username = "batch_user")
        productId = createProduct()
    }

    private fun createProduct(): String {
        val product = ProductSearchResult(
            name = "Kurczak",
            source = "LOCAL",
            calories = 165.0,
            protein = 31.0,
            fat = 3.6,
            carbohydrates = 0.0
        )
        val response = rest.post()
            .uri("/api/products/local")
            .bearer(token)
            .contentType(MediaType.APPLICATION_JSON)
            .body(product)
            .retrieve()
            .body(ProductSearchResult::class.java)
        return requireNotNull(response?.id)
    }

    @Test
    fun `tworzenie patelni konsumpcja i edycja segmentu`() {
        val batchMeal = rest.post()
            .uri("/api/batch-meals")
            .bearer(token)
            .contentType(MediaType.APPLICATION_JSON)
            .body(TestFixtures.createBatchMealRequest(productId = productId, weightG = 1000.0))
            .retrieve()
            .body(BatchMealDto::class.java)

        val segmentId = requireNotNull(batchMeal?.segments?.first()?.id)

        val updated = rest.put()
            .uri("/api/batch-meals/${batchMeal!!.id}/segments/$segmentId")
            .bearer(token)
            .contentType(MediaType.APPLICATION_JSON)
            .body(UpdateSegmentCookedWeightRequest(cookedWeightG = 800.0))
            .retrieve()
            .body(BatchMealDto::class.java)

        assertEquals(800.0, updated?.segments?.first()?.initialWeightG)

        rest.post()
            .uri("/api/batch-meals/consume")
            .bearer(token)
            .contentType(MediaType.APPLICATION_JSON)
            .body(TestFixtures.consumePortionRequest(segmentId = segmentId, weightG = 200.0))
            .retrieve()
            .toBodilessEntity()

        val active = rest.get()
            .uri("/api/batch-meals/active")
            .bearer(token)
            .retrieve()
            .body(object : ParameterizedTypeReference<List<BatchMealDto>>() {})

        assertEquals(600.0, active?.first()?.segments?.first()?.currentWeightG)
    }
}

class DiaryControllerIntegrationTest : IntegrationTestBase() {

    private lateinit var token: String

    @BeforeEach
    fun auth() {
        token = RestTestHelper.registerAndGetToken(rest, username = "diary_user")
    }

    @Test
    fun `consume produktu i daily summary`() {
        rest.post()
            .uri("/api/diary/consume")
            .bearer(token)
            .contentType(MediaType.APPLICATION_JSON)
            .body(TestFixtures.consumeProductRequest())
            .retrieve()
            .toBodilessEntity()

        val summary = rest.get()
            .uri("/api/diary/daily?date=2026-06-06")
            .bearer(token)
            .retrieve()
            .body(DailySummaryDto::class.java)

        assertEquals(78.0, summary?.totalKcal)
    }
}

class MealRecipeControllerIntegrationTest : IntegrationTestBase() {

    private lateinit var token: String
    private var productId: Long = 0L

    @BeforeEach
    fun setUp() {
        token = RestTestHelper.registerAndGetToken(rest, username = "recipe_user")
        val product = ProductSearchResult(
            name = "Tofu",
            source = "LOCAL",
            calories = 144.0,
            protein = 15.0,
            fat = 8.0,
            carbohydrates = 3.0
        )
        val response = rest.post()
            .uri("/api/products/local")
            .bearer(token)
            .contentType(MediaType.APPLICATION_JSON)
            .body(product)
            .retrieve()
            .body(ProductSearchResult::class.java)
        productId = response?.id?.toLong() ?: error("Brak productId")
    }

    @Test
    fun `CRUD przepisu lokalnego`() {
        val request = CreateMealRecipeRequest(
            name = "Tofu stir fry",
            sections = listOf(
                CreateRecipeSectionRequest(
                    name = "Główna",
                    ingredients = listOf(CreateRecipeIngredientRequest(productId = productId, weightG = 200.0))
                )
            )
        )
        val created = rest.post()
            .uri("/api/recipes")
            .bearer(token)
            .contentType(MediaType.APPLICATION_JSON)
            .body(request)
            .retrieve()
            .body(MealRecipeDto::class.java)

        val recipeId = requireNotNull(created?.id)

        val list = rest.get()
            .uri("/api/recipes")
            .bearer(token)
            .retrieve()
            .body(object : ParameterizedTypeReference<List<MealRecipeDto>>() {})

        assertTrue(list?.any { it.name == "Tofu stir fry" } == true)

        rest.delete()
            .uri("/api/recipes/$recipeId")
            .bearer(token)
            .retrieve()
            .toBodilessEntity()
    }
}

class WorkoutControllerIntegrationTest : IntegrationTestBase() {

    private lateinit var token: String

    @BeforeEach
    fun auth() {
        token = RestTestHelper.registerAndGetToken(rest, username = "workout_user")
    }

    @Test
    fun `dodanie i usunięcie treningu`() {
        val created = rest.post()
            .uri("/api/workouts")
            .bearer(token)
            .contentType(MediaType.APPLICATION_JSON)
            .body(TestFixtures.createWorkoutRequest())
            .retrieve()
            .body(WorkoutActivityDto::class.java)

        val id = requireNotNull(created?.id)

        val list = rest.get()
            .uri("/api/workouts?date=2026-06-06")
            .bearer(token)
            .retrieve()
            .body(object : ParameterizedTypeReference<List<WorkoutActivityDto>>() {})

        assertEquals("Bieganie", list?.first()?.name)

        rest.delete()
            .uri("/api/workouts/$id")
            .bearer(token)
            .retrieve()
            .toBodilessEntity()
    }
}

class WeighingContainerControllerIntegrationTest : IntegrationTestBase() {

    private lateinit var token: String

    @BeforeEach
    fun auth() {
        token = RestTestHelper.registerAndGetToken(rest, username = "container_user")
    }

    @Test
    fun `CRUD naczynia`() {
        val created = rest.post()
            .uri("/api/containers")
            .bearer(token)
            .contentType(MediaType.APPLICATION_JSON)
            .body(TestFixtures.createContainerRequest())
            .retrieve()
            .body(WeighingContainerDto::class.java)

        val id = requireNotNull(created?.id)

        val list = rest.get()
            .uri("/api/containers")
            .bearer(token)
            .retrieve()
            .body(object : ParameterizedTypeReference<List<WeighingContainerDto>>() {})

        assertEquals("Talerz", list?.first()?.name)

        rest.delete()
            .uri("/api/containers/$id")
            .bearer(token)
            .retrieve()
            .toBodilessEntity()
    }
}
