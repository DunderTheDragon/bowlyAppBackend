package com.example.bowlyApp.dto

import java.time.LocalDate

data class WorkoutActivityDto(
    val id: Long,
    val name: String,
    val caloriesBurned: Double,
    val activityDate: LocalDate
)

data class CreateWorkoutActivityRequest(
    val name: String,
    val caloriesBurned: Double,
    val activityDate: String
)
