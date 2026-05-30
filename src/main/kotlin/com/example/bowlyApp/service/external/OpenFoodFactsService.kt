package com.example.bowlyApp.service.external

import com.example.bowlyApp.dto.OffProduct
import com.example.bowlyApp.dto.OffSearchResponse
import com.example.bowlyApp.dto.ProductSearchResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException

@Service
class OpenFoodFactsService(
    private val webClientBuilder: WebClient.Builder
) {
    private val logger = LoggerFactory.getLogger(OpenFoodFactsService::class.java)

    private val webClient = webClientBuilder
        .baseUrl("$BASE_URL/api/v2")
        .defaultHeader("User-Agent", USER_AGENT)
        .build()

    private val searchClient = webClientBuilder
        .baseUrl("$BASE_URL/cgi")
        .defaultHeader("User-Agent", USER_AGENT)
        .build()

    suspend fun searchProducts(query: String): List<ProductSearchResult> {
        repeat(MAX_RETRIES) { attempt ->
            try {
                logger.info("Wysyłanie zapytania do OpenFoodFacts API ( próba ${attempt + 1}/$MAX_RETRIES ): search_terms=$query")

                val response = searchClient.get()
                    .uri { builder ->
                        builder.path("/search.pl")
                            .queryParam("search_terms", query)
                            .queryParam("search_simple", "1")
                            .queryParam("action", "process")
                            .queryParam("json", "1")
                            .queryParam("sort_by", "unique_scans_n")
                            .queryParam("page_size", "20")
                            .build()
                    }
                    .retrieve()
                    .bodyToMono(OffSearchResponse::class.java)
                    .awaitSingleOrNull()

                return mapResponse(response)
            } catch (e: WebClientResponseException.ServiceUnavailable) {
                logger.warn("OpenFoodFacts zwrócił 503 (próba ${attempt + 1}/$MAX_RETRIES): ${e.message}")
                if (attempt < MAX_RETRIES - 1) {
                    delay(RETRY_DELAY_MS * (attempt + 1))
                }
            } catch (e: Exception) {
                logger.error("Błąd podczas połączenia z API OpenFoodFacts (search): ${e.message}", e)
                return emptyList()
            }
        }
        return emptyList()
    }

    private fun mapResponse(response: OffSearchResponse?): List<ProductSearchResult> {
        return response?.products?.filter {
            !it.product_name.isNullOrBlank() &&
                it.nutriments != null
        }?.map { product ->
            val nutriments = product.nutriments!!
            val externalId = product.code ?: product.id ?: ""
            ProductSearchResult(
                externalId = externalId,
                name = product.product_name ?: "Nieznany produkt",
                source = "OPEN_FOOD_FACTS",
                barcode = externalId,
                calories = nutriments.kcalPer100g(),
                protein = nutriments.proteins_100g ?: 0.0,
                fat = nutriments.fat_100g ?: 0.0,
                carbohydrates = nutriments.carbohydrates_100g ?: 0.0
            )
        } ?: emptyList()
    }

    suspend fun getProductByBarcode(barcode: String): ProductSearchResult? {
        data class OffBarcodeResponse(val status: Int?, val product: OffProduct?)

        return try {
            val response = webClient.get()
                .uri("/product/$barcode.json")
                .retrieve()
                .bodyToMono(OffBarcodeResponse::class.java)
                .awaitSingleOrNull()

            if (response?.status == 1 && response.product != null && !response.product.product_name.isNullOrBlank() && response.product.nutriments != null) {
                val nutriments = response.product.nutriments
                val externalId = response.product.code ?: response.product.id ?: barcode
                ProductSearchResult(
                    externalId = externalId,
                    name = response.product.product_name,
                    source = "OPEN_FOOD_FACTS",
                    barcode = barcode,
                    calories = nutriments.kcalPer100g(),
                    protein = nutriments.proteins_100g ?: 0.0,
                    fat = nutriments.fat_100g ?: 0.0,
                    carbohydrates = nutriments.carbohydrates_100g ?: 0.0
                )
            } else {
                null
            }
        } catch (e: Exception) {
            logger.error("Błąd podczas połączenia z API OpenFoodFacts (barcode): ${e.message}", e)
            null
        }
    }

    private companion object {
        const val BASE_URL = "https://world.openfoodfacts.org"
        const val USER_AGENT = "BowlyApp/1.0 (https://github.com/cantbebetter/bowly)"
        const val MAX_RETRIES = 3
        const val RETRY_DELAY_MS = 1000L
    }
}
