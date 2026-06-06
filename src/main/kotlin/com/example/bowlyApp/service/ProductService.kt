package com.example.bowlyApp.service

import com.example.bowlyApp.dto.ProductSearchResult
import com.example.bowlyApp.model.Product
import com.example.bowlyApp.repository.ProductRepository
import com.example.bowlyApp.service.external.OpenFoodFactsService
import kotlinx.coroutines.coroutineScope
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ProductService(
    private val productRepository: ProductRepository,
    private val openFoodFactsService: OpenFoodFactsService
) {
    private val logger = LoggerFactory.getLogger(ProductService::class.java)

    /**
     * Natychmiastowe wyszukiwanie w lokalnej bazie — bez limitu długości zapytania.
     */
    fun searchLocalProducts(query: String): List<ProductSearchResult> {
        logger.info("Wyszukiwanie lokalne dla zapytania: '$query'")
        val results = if (query.isBlank()) {
            productRepository.findAll()
        } else {
            productRepository.findByNameContainingIgnoreCase(query)
        }.map { it.toSearchResult() }
        logger.info("Znaleziono ${results.size} wyników w lokalnej bazie.")
        return results
    }

    /**
     * Wyszukiwanie w Open Food Facts — wymaga min. 3 znaków (limit rate OFF).
     */
    suspend fun searchExternalProducts(query: String): List<ProductSearchResult> {
        if (query.length < MIN_EXTERNAL_QUERY_LENGTH) {
            logger.info("Pominięto wyszukiwanie zewnętrzne — zapytanie za krótkie: '$query'")
            return emptyList()
        }

        logger.info("Wyszukiwanie w Open Food Facts dla zapytania: '$query'")

        return try {
            val results = openFoodFactsService.searchProducts(query)
            logger.info("Znaleziono ${results.size} wyników w Open Food Facts.")
            deduplicateProducts(results)
        } catch (e: Exception) {
            logger.error("Błąd podczas wyszukiwania w Open Food Facts: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Pełne wyszukiwanie (kompatybilność wsteczna) — lokalne + zewnętrzne.
     */
    suspend fun searchProducts(query: String): List<ProductSearchResult> = coroutineScope {
        val localProducts = searchLocalProducts(query)
        val externalProducts = searchExternalProducts(query)

        val localKeys = localProducts.map { productDedupKey(it) }.toSet()
        val externalFiltered = externalProducts.filter { productDedupKey(it) !in localKeys }

        localProducts + externalFiltered
    }

    suspend fun getProductByBarcode(barcode: String): ProductSearchResult? {
        logger.info("Wyszukiwanie produktu dla kodu kreskowego: $barcode")
        val localProduct = productRepository.findByBarcode(barcode)
        if (localProduct != null) {
            logger.info("Znaleziono produkt w lokalnej bazie dla kodu: $barcode")
            return localProduct.toSearchResult()
        }

        logger.info("Brak produktu w lokalnej bazie dla kodu: $barcode. Odpytywanie Open Food Facts...")

        return try {
            openFoodFactsService.getProductByBarcode(barcode)
        } catch (e: Exception) {
            logger.error("Błąd podczas pobierania produktu po kodzie kreskowym z API: ${e.message}", e)
            null
        }
    }

    fun getLocalProducts(): List<ProductSearchResult> {
        logger.info("Pobieranie wszystkich lokalnych produktów z bazy danych.")
        return productRepository.findAll().map { it.toSearchResult() }
    }

    fun saveLocalProduct(productDto: ProductSearchResult): Product {
        if (productDto.id != null) {
            logger.info("Aktualizowanie istniejącego produktu: ${productDto.name}")
            val existing = productRepository.findById(productDto.id.toLong()).orElseThrow {
                IllegalArgumentException("Nie znaleziono produktu o ID: ${productDto.id}")
            }
            return productRepository.save(
                existing.copy(
                    name = productDto.name,
                    barcode = productDto.barcode,
                    externalId = productDto.externalId ?: existing.externalId,
                    kcalPer100g = productDto.calories,
                    proteinPer100g = productDto.protein,
                    fatPer100g = productDto.fat,
                    carbsPer100g = productDto.carbohydrates,
                    unitName = productDto.unitName,
                    unitWeightG = productDto.unitWeightG
                )
            )
        }
        return findOrCreateProduct(productDto)
    }

    fun findOrCreateProduct(productDto: ProductSearchResult): Product {
        if (productDto.id != null) {
            productRepository.findById(productDto.id.toLong()).orElse(null)?.let { return it }
            logger.warn("Produkt o ID ${productDto.id} nie istnieje — wyszukiwanie po barcode/externalId lub tworzenie nowego")
        }

        if (productDto.source != null && productDto.source !in listOf("LOCAL", "USER") && !productDto.externalId.isNullOrBlank()) {
            productRepository.findBySourceAndExternalId(productDto.source, productDto.externalId)?.let { return it }
        }

        if (!productDto.barcode.isNullOrBlank()) {
            productRepository.findByBarcode(productDto.barcode!!)?.let { return it }
        }

        logger.info("Tworzenie produktu w cache: ${productDto.name}")
        return productRepository.save(
            Product(
                name = productDto.name,
                source = productDto.source ?: "USER",
                externalId = productDto.externalId,
                barcode = productDto.barcode,
                kcalPer100g = productDto.calories,
                proteinPer100g = productDto.protein,
                fatPer100g = productDto.fat,
                carbsPer100g = productDto.carbohydrates,
                unitName = productDto.unitName,
                unitWeightG = productDto.unitWeightG
            )
        )
    }

    fun Product.toSearchResult() = ProductSearchResult(
        id = id.toString(),
        externalId = externalId,
        name = name,
        source = source,
        barcode = barcode,
        calories = kcalPer100g,
        protein = proteinPer100g,
        fat = fatPer100g,
        carbohydrates = carbsPer100g,
        unitName = unitName,
        unitWeightG = unitWeightG
    )

    private fun deduplicateProducts(products: List<ProductSearchResult>): List<ProductSearchResult> {
        val seen = mutableSetOf<String>()
        return products.filter { product ->
            seen.add(productDedupKey(product))
        }
    }

    companion object {
        const val MIN_EXTERNAL_QUERY_LENGTH = 3

        fun productDedupKey(product: ProductSearchResult): String {
            if (!product.externalId.isNullOrBlank() && !product.source.isNullOrBlank()) {
                return "${product.source}:${product.externalId}"
            }
            if (!product.externalId.isNullOrBlank()) return "ext:${product.externalId}"
            if (!product.barcode.isNullOrBlank()) return "barcode:${product.barcode}"
            if (product.id != null) return "id:${product.id}"
            return "name:${product.name.lowercase()}"
        }
    }
}
