package com.example.bowlyApp.service

import com.example.bowlyApp.model.BatchMeal
import com.example.bowlyApp.model.ConsumedPortion
import com.example.bowlyApp.repository.ConsumedPortionRepository
import com.example.bowlyApp.repository.UserRepository
import com.example.bowlyApp.support.TestFixtures
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.Optional

class DiaryServiceTest {

    private val consumedPortionRepository = mockk<ConsumedPortionRepository>()
    private val userRepository = mockk<UserRepository>()
    private val productService = mockk<ProductService>()
    private val workoutService = mockk<WorkoutService>()
    private lateinit var diaryService: DiaryService

    @BeforeEach
    fun setUp() {
        diaryService = DiaryService(
            consumedPortionRepository,
            userRepository,
            productService,
            workoutService
        )
    }

    @Test
    fun `consumeProduct zapisuje wpis z produktem`() {
        val user = TestFixtures.user()
        val product = TestFixtures.product(name = "Jabłko")
        every { userRepository.findByUsername("testuser") } returns user
        every { productService.findOrCreateProduct(any()) } returns product
        every { consumedPortionRepository.save(any()) } answers { firstArg() }

        diaryService.consumeProduct("testuser", TestFixtures.consumeProductRequest())

        verify {
            consumedPortionRepository.save(match<ConsumedPortion> {
                it.consumedWeightG == 150.0 && it.product?.name == "Jabłko"
            })
        }
    }

    @Test
    fun `deleteConsumedMeal przywraca wagę na patelnię`() {
        val user = TestFixtures.user()
        val batchMeal = TestFixtures.batchMeal()
        val segment = TestFixtures.batchMealSegment(batchMeal, currentWeightG = 200.0)
        batchMeal.isDepleted = true
        val portion = ConsumedPortion(
            id = 1L,
            user = user,
            segment = segment,
            consumedWeightG = 100.0,
            mealDate = LocalDate.parse("2026-06-06"),
            mealType = "LUNCH"
        )
        every { userRepository.findByUsername("testuser") } returns user
        every { consumedPortionRepository.findById(1L) } returns Optional.of(portion)
        every { consumedPortionRepository.delete(portion) } returns Unit

        diaryService.deleteConsumedMeal("testuser", 1L)

        assertEquals(300.0, segment.currentWeightG)
        assertFalse(batchMeal.isDepleted)
    }

    @Test
    fun `getDailySummary sumuje makro z produktu`() {
        val user = TestFixtures.user()
        val product = TestFixtures.product(
            name = "Jabłko",
            kcal = 100.0,
            protein = 1.0,
            fat = 0.5,
            carbs = 20.0
        )
        val portion = ConsumedPortion(
            id = 1L,
            user = user,
            product = product,
            consumedWeightG = 200.0,
            mealDate = LocalDate.parse("2026-06-06"),
            mealType = "BREAKFAST"
        )
        every { userRepository.findByUsername("testuser") } returns user
        every { consumedPortionRepository.findByUserIdAndMealDate(1L, LocalDate.parse("2026-06-06")) } returns listOf(portion)
        every { workoutService.getActivitiesForDate("testuser", LocalDate.parse("2026-06-06")) } returns emptyList()

        val summary = diaryService.getDailySummary("testuser", LocalDate.parse("2026-06-06"))

        assertEquals(200.0, summary.totalKcal)
        assertEquals(2.0, summary.totalProtein)
        assertEquals(1.0, summary.totalFat)
        assertEquals(40.0, summary.totalCarbs)
    }

    @Test
    fun `getDailySummary przelicza makro z patelni`() {
        val user = TestFixtures.user()
        val batchMeal = BatchMeal(name = "Patelnia")
        val segment = TestFixtures.batchMealSegment(batchMeal, initialWeightG = 1000.0)
        val portion = ConsumedPortion(
            id = 2L,
            user = user,
            segment = segment,
            consumedWeightG = 100.0,
            mealDate = LocalDate.parse("2026-06-06"),
            mealType = "LUNCH",
            weightBasisG = 1000.0
        )
        every { userRepository.findByUsername("testuser") } returns user
        every { consumedPortionRepository.findByUserIdAndMealDate(1L, LocalDate.parse("2026-06-06")) } returns listOf(portion)
        every { workoutService.getActivitiesForDate("testuser", LocalDate.parse("2026-06-06")) } returns emptyList()

        val summary = diaryService.getDailySummary("testuser", LocalDate.parse("2026-06-06"))

        assertEquals(50.0, summary.totalKcal)
        assertEquals(5.0, summary.totalProtein)
    }
}
