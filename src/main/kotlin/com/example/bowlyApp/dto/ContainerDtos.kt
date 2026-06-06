package com.example.bowlyApp.dto

data class WeighingContainerDto(
    val id: Long,
    val name: String,
    val type: String,
    val weightG: Double,
    val imageBase64: String? = null
)

data class CreateWeighingContainerRequest(
    val name: String,
    val type: String,
    val weightG: Double,
    val imageBase64: String? = null
)

data class UpdateWeighingContainerRequest(
    val name: String,
    val type: String,
    val weightG: Double,
    val imageBase64: String? = null
)
