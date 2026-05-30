package com.example.bowlyApp.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

// Open Food Facts DTOs
@JsonIgnoreProperties(ignoreUnknown = true)
data class OffSearchResponse(
    val count: Int? = null,
    val products: List<OffProduct>? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class OffProduct(
    @JsonProperty("id")
    val id: String? = null,

    @JsonProperty("code")
    val code: String? = null,

    @JsonProperty("product_name")
    val product_name: String? = null,

    @JsonProperty("nutriments")
    val nutriments: OffNutriments? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class OffNutriments(
    @JsonProperty("energy-kcal_100g")
    val energy_kcal_100g: Double? = null,

    @JsonProperty("energy_100g")
    val energy_100g: Double? = null,

    @JsonProperty("proteins_100g")
    val proteins_100g: Double? = null,

    @JsonProperty("fat_100g")
    val fat_100g: Double? = null,

    @JsonProperty("carbohydrates_100g")
    val carbohydrates_100g: Double? = null
) {
    fun kcalPer100g(): Double =
        energy_kcal_100g ?: energy_100g?.let { it / 4.184 } ?: 0.0
}

data class ProductSearchResult(
    val id: String? = null,
    val externalId: String? = null,
    val name: String,
    val source: String? = null,
    val barcode: String? = null,
    val calories: Double,
    val protein: Double,
    val fat: Double,
    val carbohydrates: Double,
    val unitName: String? = null,
    val unitWeightG: Double? = null
)
