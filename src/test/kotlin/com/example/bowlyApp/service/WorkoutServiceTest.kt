package com.example.bowlyApp.service

import com.example.bowlyApp.model.WorkoutActivity
import com.example.bowlyApp.repository.UserRepository
import com.example.bowlyApp.repository.WorkoutActivityRepository
import com.example.bowlyApp.support.TestFixtures
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.Optional

class WorkoutServiceTest {

    private val workoutActivityRepository = mockk<WorkoutActivityRepository>()
    private val userRepository = mockk<UserRepository>()
    private lateinit var workoutService: WorkoutService

    @BeforeEach
    fun setUp() {
        workoutService = WorkoutService(workoutActivityRepository, userRepository)
    }

    @Test
    fun `addActivity zapisuje trening użytkownika`() {
        val user = TestFixtures.user()
        every { userRepository.findByUsername("testuser") } returns user
        every { workoutActivityRepository.save(any()) } answers {
            firstArg<WorkoutActivity>().copy(id = 1L)
        }

        val dto = workoutService.addActivity("testuser", TestFixtures.createWorkoutRequest())

        assertEquals("Bieganie", dto.name)
        assertEquals(300.0, dto.caloriesBurned)
    }

    @Test
    fun `addActivity odrzuca pustą nazwę`() {
        assertThrows(IllegalArgumentException::class.java) {
            workoutService.addActivity("testuser", TestFixtures.createWorkoutRequest(name = "  "))
        }
    }

    @Test
    fun `addActivity odrzuca zerowe kalorie`() {
        assertThrows(IllegalArgumentException::class.java) {
            workoutService.addActivity("testuser", TestFixtures.createWorkoutRequest(calories = 0.0))
        }
    }

    @Test
    fun `deleteActivity odrzuca usunięcie cudzej aktywności`() {
        val user = TestFixtures.user()
        val other = TestFixtures.user(id = 2L, username = "other")
        val activity = WorkoutActivity(
            id = 1L,
            user = other,
            activityDate = LocalDate.parse("2026-06-06"),
            name = "Rowery",
            caloriesBurned = 200.0
        )
        every { userRepository.findByUsername("testuser") } returns user
        every { workoutActivityRepository.findById(1L) } returns Optional.of(activity)

        assertThrows(IllegalArgumentException::class.java) {
            workoutService.deleteActivity("testuser", 1L)
        }
    }

    @Test
    fun `getActivitiesForDate zwraca listę aktywności`() {
        val user = TestFixtures.user()
        val activity = WorkoutActivity(
            id = 1L,
            user = user,
            activityDate = LocalDate.parse("2026-06-06"),
            name = "Spacer",
            caloriesBurned = 150.0
        )
        every { userRepository.findByUsername("testuser") } returns user
        every { workoutActivityRepository.findByUserIdAndActivityDate(1L, LocalDate.parse("2026-06-06")) } returns listOf(activity)

        val results = workoutService.getActivitiesForDate("testuser", LocalDate.parse("2026-06-06"))

        assertEquals(1, results.size)
        assertEquals("Spacer", results.first().name)
    }
}
