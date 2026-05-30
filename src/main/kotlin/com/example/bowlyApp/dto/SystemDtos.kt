package com.example.bowlyApp.dto

import jakarta.validation.constraints.NotBlank

data class SetupRequest(
    @field:NotBlank val adminUsername: String,
    @field:NotBlank val adminPassword: String
)
