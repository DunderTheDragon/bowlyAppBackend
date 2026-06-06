package com.example.bowlyApp.integration

import com.example.bowlyApp.dto.*
import com.example.bowlyApp.support.IntegrationTestBase
import com.example.bowlyApp.support.RestTestHelper
import com.example.bowlyApp.support.TestFixtures
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.core.ParameterizedTypeReference

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
        val response = RestTestHelper.post(
            rest,
            "/api/products/local",
            token,
            product,
            ProductSearchResult::class.java
        )
        return requireNotNull(response?.id)
    }

    @Test
    fun `tworzenie patelni konsumpcja i edycja segmentu`() {
        val batchMeal = RestTestHelper.post(
            rest,
            "/api/batch-meals",
            token,
            TestFixtures.createBatchMealRequest(productId = productId, weightG = 1000.0),
            BatchMealDto::class.java
        )

        val segmentId = requireNotNull(batchMeal?.segments?.first()?.id)

        val updated = RestTestHelper.put(
            rest,
            "/api/batch-meals/${batchMeal!!.id}/segments/$segmentId",
            token,
            UpdateSegmentCookedWeightRequest(cookedWeightG = 800.0),
            BatchMealDto::class.java
        )

        assertEquals(800.0, updated?.segments?.first()?.initialWeightG)

        RestTestHelper.postEmpty(
            rest,
            "/api/batch-meals/consume",
            token,
            TestFixtures.consumePortionRequest(segmentId = segmentId, weightG = 200.0)
        )

        val active = RestTestHelper.getList(
            rest,
            "/api/batch-meals/active",
            token,
            object : ParameterizedTypeReference<List<BatchMealDto>>() {}
        )

        val batchId = requireNotNull(batchMeal.id)
        val activeBatch = active?.first { it.id == batchId }
        assertEquals(600.0, activeBatch?.segments?.first()?.currentWeightG)
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
        RestTestHelper.postEmpty(
            rest,
            "/api/diary/consume",
            token,
            TestFixtures.consumeProductRequest()
        )

        val summary = RestTestHelper.get(
            rest,
            "/api/diary/daily?date=2026-06-06",
            token,
            DailySummaryDto::class.java
        )

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
        val response = RestTestHelper.post(
            rest,
            "/api/products/local",
            token,
            product,
            ProductSearchResult::class.java
        )
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
        val created = RestTestHelper.post(
            rest,
            "/api/recipes",
            token,
            request,
            MealRecipeDto::class.java
        )

        val recipeId = requireNotNull(created?.id)

        val list = RestTestHelper.getList(
            rest,
            "/api/recipes",
            token,
            object : ParameterizedTypeReference<List<MealRecipeDto>>() {}
        )

        assertTrue(list?.any { it.name == "Tofu stir fry" } == true)

        RestTestHelper.delete(rest, "/api/recipes/$recipeId", token)
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
        val created = RestTestHelper.post(
            rest,
            "/api/workouts",
            token,
            TestFixtures.createWorkoutRequest(),
            WorkoutActivityDto::class.java
        )

        val id = requireNotNull(created?.id)

        val list = RestTestHelper.getList(
            rest,
            "/api/workouts?date=2026-06-06",
            token,
            object : ParameterizedTypeReference<List<WorkoutActivityDto>>() {}
        )

        assertEquals("Bieganie", list?.first { it.id == id }?.name)

        RestTestHelper.delete(rest, "/api/workouts/$id", token)
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
        val created = RestTestHelper.post(
            rest,
            "/api/containers",
            token,
            TestFixtures.createContainerRequest(),
            WeighingContainerDto::class.java
        )

        val id = requireNotNull(created?.id)

        val list = RestTestHelper.getList(
            rest,
            "/api/containers",
            token,
            object : ParameterizedTypeReference<List<WeighingContainerDto>>() {}
        )

        assertEquals("Talerz", list?.first { it.id == id }?.name)

        RestTestHelper.delete(rest, "/api/containers/$id", token)
    }
}
