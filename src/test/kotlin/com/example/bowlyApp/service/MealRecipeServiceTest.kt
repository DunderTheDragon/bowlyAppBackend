package com.example.bowlyApp.service

import com.example.bowlyApp.dto.CreateMealRecipeRequest
import com.example.bowlyApp.dto.CreateRecipeIngredientRequest
import com.example.bowlyApp.dto.CreateRecipeSectionRequest
import com.example.bowlyApp.model.MealRecipe
import com.example.bowlyApp.repository.MealRecipeRepository
import com.example.bowlyApp.repository.ProductRepository
import com.example.bowlyApp.repository.UserRepository
import com.example.bowlyApp.support.TestFixtures
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Optional

class MealRecipeServiceTest {

    private val mealRecipeRepository = mockk<MealRecipeRepository>()
    private val productRepository = mockk<ProductRepository>()
    private val productService = mockk<ProductService>()
    private val userRepository = mockk<UserRepository>()
    private lateinit var mealRecipeService: MealRecipeService

    @BeforeEach
    fun setUp() {
        mealRecipeService = MealRecipeService(
            mealRecipeRepository,
            productRepository,
            productService,
            userRepository
        )
    }

    @Test
    fun `createLocalRecipe zapisuje przepis z sekcjami`() {
        val user = TestFixtures.user()
        val product = TestFixtures.product()
        every { userRepository.findByUsername("testuser") } returns user
        every { productRepository.findById(1L) } returns Optional.of(product)
        every { mealRecipeRepository.save(any()) } answers {
            val recipe = firstArg<MealRecipe>()
            recipe.copy(id = 10L)
        }

        val request = CreateMealRecipeRequest(
            name = "Obiad",
            sections = listOf(
                CreateRecipeSectionRequest(
                    name = "Główna",
                    ingredients = listOf(
                        CreateRecipeIngredientRequest(productId = 1L, weightG = 200.0)
                    )
                )
            )
        )

        val dto = mealRecipeService.createLocalRecipe("testuser", request)

        assertEquals("Obiad", dto.name)
        assertEquals(10L, dto.id)
        verify { mealRecipeRepository.save(any()) }
    }

    @Test
    fun `deleteRecipe odrzuca modyfikację cudzego przepisu`() {
        val owner = TestFixtures.user(id = 1L, username = "owner")
        val recipe = MealRecipe(id = 5L, name = "Cudzy", source = "LOCAL", user = owner)
        every { mealRecipeRepository.findById(5L) } returns Optional.of(recipe)

        val ex = assertThrows(IllegalArgumentException::class.java) {
            mealRecipeService.deleteRecipe("intruder", 5L)
        }
        assertEquals("Brak uprawnień do modyfikacji tego przepisu", ex.message)
    }

    @Test
    fun `getAllRecipes filtruje po scope MINE`() {
        val user = TestFixtures.user()
        val ownRecipe = MealRecipe(id = 1L, name = "Mój", source = "LOCAL", user = user)
        val otherRecipe = MealRecipe(id = 2L, name = "Cudzy", source = "LOCAL", user = TestFixtures.user(id = 2L, username = "other"))
        every { mealRecipeRepository.findAllWithIngredients() } returns listOf(ownRecipe, otherRecipe)

        val results = mealRecipeService.getAllRecipes("testuser", scope = "MINE", singleMealOnly = null)

        assertEquals(1, results.size)
        assertEquals("Mój", results.first().name)
    }
}
