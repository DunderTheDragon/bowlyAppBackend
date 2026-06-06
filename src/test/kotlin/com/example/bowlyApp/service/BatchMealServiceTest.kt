package com.example.bowlyApp.service

import com.example.bowlyApp.model.BatchMeal
import com.example.bowlyApp.model.BatchMealSegment
import com.example.bowlyApp.model.ConsumedPortion
import com.example.bowlyApp.repository.*
import com.example.bowlyApp.support.TestFixtures
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Optional

class BatchMealServiceTest {

    private val batchMealRepository = mockk<BatchMealRepository>()
    private val batchMealSegmentRepository = mockk<BatchMealSegmentRepository>()
    private val mealRecipeRepository = mockk<MealRecipeRepository>()
    private val mealRecipeService = mockk<MealRecipeService>()
    private val productRepository = mockk<ProductRepository>()
    private val productService = mockk<ProductService>(relaxed = true)
    private val userRepository = mockk<UserRepository>()
    private val consumedPortionRepository = mockk<ConsumedPortionRepository>()
    private lateinit var batchMealService: BatchMealService

    @BeforeEach
    fun setUp() {
        batchMealService = BatchMealService(
            batchMealRepository,
            batchMealSegmentRepository,
            mealRecipeRepository,
            mealRecipeService,
            productRepository,
            productService,
            userRepository,
            consumedPortionRepository
        )
    }

    @Test
    fun `createBatchMeal wymaga co najmniej jednej sekcji`() {
        val request = TestFixtures.createBatchMealRequest().copy(segments = emptyList())

        assertThrows(IllegalArgumentException::class.java) {
            batchMealService.createBatchMeal("testuser", request)
        }
    }

    @Test
    fun `createBatchMeal tworzy patelnię z segmentem`() {
        val product = TestFixtures.product()
        every { productRepository.findById(1L) } returns Optional.of(product)
        every { batchMealRepository.save(any()) } answers {
            val meal = firstArg<BatchMeal>()
            meal.copy(id = 1L).also { saved ->
                saved.segments.forEachIndexed { index, segment ->
                    saved.segments[index] = segment.copy(id = (index + 1).toLong())
                }
            }
        }
        every { productService.findOrCreateProduct(any()) } returns product

        val dto = batchMealService.createBatchMeal("testuser", TestFixtures.createBatchMealRequest())

        assertEquals("Patelnia testowa", dto.name)
        assertEquals(1, dto.segments.size)
        assertEquals(500.0, dto.segments.first().initialWeightG)
    }

    @Test
    fun `consumePortion zmniejsza wagę segmentu i zapisuje wpis dziennika`() {
        val user = TestFixtures.user()
        val batchMeal = TestFixtures.batchMeal()
        val segment = TestFixtures.batchMealSegment(batchMeal, currentWeightG = 500.0)
        batchMeal.segments.add(segment)

        every { userRepository.findByUsername("testuser") } returns user
        every { batchMealSegmentRepository.findByIdWithPessimisticLock(1L) } returns segment
        every { consumedPortionRepository.save(any()) } answers { firstArg() }

        batchMealService.consumePortion("testuser", TestFixtures.consumePortionRequest(weightG = 100.0))

        assertEquals(400.0, segment.currentWeightG)
        verify {
            consumedPortionRepository.save(match<ConsumedPortion> {
                it.consumedWeightG == 100.0 && it.weightBasisG == 1000.0
            })
        }
    }

    @Test
    fun `consumePortion odrzuca zbyt dużą porcję`() {
        val user = TestFixtures.user()
        val batchMeal = TestFixtures.batchMeal()
        val segment = TestFixtures.batchMealSegment(batchMeal, currentWeightG = 50.0)
        every { userRepository.findByUsername("testuser") } returns user
        every { batchMealSegmentRepository.findByIdWithPessimisticLock(1L) } returns segment

        assertThrows(IllegalArgumentException::class.java) {
            batchMealService.consumePortion("testuser", TestFixtures.consumePortionRequest(weightG = 100.0))
        }
    }

    @Test
    fun `updateSegmentCookedWeight skaluje bieżącą wagę`() {
        val batchMeal = TestFixtures.batchMeal(id = 1L)
        val segment = TestFixtures.batchMealSegment(
            batchMeal,
            id = 2L,
            initialWeightG = 1000.0,
            currentWeightG = 800.0
        )
        every { batchMealSegmentRepository.findById(2L) } returns Optional.of(segment)
        every { batchMealSegmentRepository.save(any()) } answers { firstArg() }

        val dto = batchMealService.updateSegmentCookedWeight(
            batchMealId = 1L,
            segmentId = 2L,
            request = com.example.bowlyApp.dto.UpdateSegmentCookedWeightRequest(cookedWeightG = 500.0)
        )

        assertEquals(500.0, segment.initialWeightG)
        assertEquals(400.0, segment.currentWeightG, 0.01)
        assertNotNull(dto)
    }
}
