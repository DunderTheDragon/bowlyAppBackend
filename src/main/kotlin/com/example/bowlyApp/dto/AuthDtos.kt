package com.example.bowlyApp.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class RegisterRequest(
    @field:NotBlank(message = "Username is required")
    @field:Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    val username: String,

    @field:NotBlank(message = "Password is required")
    @field:Size(min = 6, message = "Password must be at least 6 characters")
    val password: String,

    @field:NotBlank(message = "Registration secret is required")
    val registrationSecret: String
)

data class LoginRequest(
    @field:NotBlank(message = "Username is required")
    val username: String,

    @field:NotBlank(message = "Password is required")
    val password: String
)

data class AuthResponse(
    val token: String,
    val username: String,
    val role: String,
    val message: String? = null
)

data class UserDto(
    val id: Long,
    val username: String,
    val role: String? = null,
    val gender: String? = null,
    val age: Int? = null,
    val heightCm: Double? = null,
    val weightKg: Double? = null,
    val targetWeightKg: Double? = null,
    val weeklyChangeRateKg: Double? = null,
    val activityLevel: Double? = null,
    val proteinRatio: Double? = null,
    val fatRatio: Double? = null,
    val carbsRatio: Double? = null,
    val isDarkTheme: Boolean? = null,
    val showBatchOnboarding: Boolean? = null
)