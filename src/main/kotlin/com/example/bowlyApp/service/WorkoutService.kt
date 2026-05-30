package com.example.bowlyApp.service

import com.example.bowlyApp.dto.CreateWorkoutActivityRequest
import com.example.bowlyApp.dto.WorkoutActivityDto
import com.example.bowlyApp.model.WorkoutActivity
import com.example.bowlyApp.repository.UserRepository
import com.example.bowlyApp.repository.WorkoutActivityRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import kotlin.math.round

@Service
class WorkoutService(
    private val workoutActivityRepository: WorkoutActivityRepository,
    private val userRepository: UserRepository
) {
    private val logger = LoggerFactory.getLogger(WorkoutService::class.java)

    @Transactional(readOnly = true)
    fun getActivitiesForDate(username: String, date: LocalDate): List<WorkoutActivityDto> {
        val user = userRepository.findByUsername(username)
            ?: throw IllegalArgumentException("User not found")
        return workoutActivityRepository.findByUserIdAndActivityDate(user.id, date)
            .map { mapToDto(it) }
    }

    @Transactional
    fun addActivity(username: String, request: CreateWorkoutActivityRequest): WorkoutActivityDto {
        if (request.name.isBlank()) {
            throw IllegalArgumentException("Nazwa aktywności nie może być pusta")
        }
        if (request.caloriesBurned <= 0) {
            throw IllegalArgumentException("Spalone kalorie muszą być większe od zera")
        }

        val user = userRepository.findByUsername(username)
            ?: throw IllegalArgumentException("User not found")

        val activity = WorkoutActivity(
            user = user,
            activityDate = LocalDate.parse(request.activityDate),
            name = request.name.trim(),
            caloriesBurned = request.caloriesBurned
        )

        val saved = workoutActivityRepository.save(activity)
        logger.info("User '${user.username}' logged workout '${saved.name}' (${saved.caloriesBurned} kcal)")
        return mapToDto(saved)
    }

    @Transactional
    fun deleteActivity(username: String, id: Long) {
        val user = userRepository.findByUsername(username)
            ?: throw IllegalArgumentException("User not found")

        val activity = workoutActivityRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Workout activity not found") }

        if (activity.user.id != user.id) {
            throw IllegalArgumentException("Brak uprawnień do usunięcia tej aktywności")
        }

        workoutActivityRepository.delete(activity)
    }

    private fun mapToDto(activity: WorkoutActivity): WorkoutActivityDto = WorkoutActivityDto(
        id = activity.id,
        name = activity.name,
        caloriesBurned = round(activity.caloriesBurned * 100) / 100.0,
        activityDate = activity.activityDate
    )
}
