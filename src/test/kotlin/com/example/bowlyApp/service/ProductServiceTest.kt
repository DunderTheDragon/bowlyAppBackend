package com.example.bowlyApp.service

import com.example.bowlyApp.dto.OffNutriments
import com.example.bowlyApp.dto.ProductSearchResult
import com.example.bowlyApp.repository.ProductRepository
import com.example.bowlyApp.service.external.OpenFoodFactsService
import com.example.bowlyApp.support.TestFixtures
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ProductServiceTest {

    private val productRepository = mockk<ProductRepository>()
    private val openFoodFactsService = mockk<OpenFoodFactsService>()
    private lateinit var productService: ProductService

    @BeforeEach
    fun setUp() {
        productService = ProductService(productRepository, openFoodFactsService)
    }

    @Test
    fun `searchLocalProducts zwraca wszystkie produkty dla pustego zapytania`() {
        val product = TestFixtures.product()
        every { productRepository.findAll() } returns listOf(product)

        val results = productService.searchLocalProducts("")

        assertEquals(1, results.size)
        assertEquals("Kurczak", results.first().name)
    }

    @Test
    fun `searchExternalProducts zwraca pustą listę dla krótkiego zapytania`() = runBlocking {
        val results = productService.searchExternalProducts("ab")

        assertTrue(results.isEmpty())
    }

    @Test
    fun `searchExternalProducts deleguje do Open Food Facts`() = runBlocking {
        val offResult = ProductSearchResult(
            externalId = "123",
            name = "Jogurt",
            source = "OPEN_FOOD_FACTS",
            calories = 60.0,
            protein = 3.0,
            fat = 1.5,
            carbohydrates = 8.0
        )
        coEvery { openFoodFactsService.searchProducts("jogurt") } returns listOf(offResult)

        val results = productService.searchExternalProducts("jogurt")

        assertEquals(1, results.size)
        assertEquals("Jogurt", results.first().name)
    }

    @Test
    fun `findOrCreateProduct zwraca istniejący produkt po barcode`() {
        val existing = TestFixtures.product().copy(barcode = "590123")
        every { productRepository.findByBarcode("590123") } returns existing

        val result = productService.findOrCreateProduct(
            TestFixtures.productSearchResult(barcode = "590123")
        )

        assertEquals(existing.id, result.id)
    }

    @Test
    fun `findOrCreateProduct tworzy nowy produkt gdy brak w bazie`() {
        every { productRepository.findByBarcode(any()) } returns null
        every { productRepository.findBySourceAndExternalId(any(), any()) } returns null
        every { productRepository.save(any()) } answers { firstArg() }

        val dto = TestFixtures.productSearchResult(name = "Nowy produkt")
        val saved = productService.findOrCreateProduct(dto)

        assertEquals("Nowy produkt", saved.name)
    }

    @Test
    fun `productDedupKey używa source i externalId`() {
        val key = ProductService.productDedupKey(
            ProductSearchResult(
                externalId = "abc",
                name = "X",
                source = "OPEN_FOOD_FACTS",
                calories = 0.0,
                protein = 0.0,
                fat = 0.0,
                carbohydrates = 0.0
            )
        )

        assertEquals("OPEN_FOOD_FACTS:abc", key)
    }
}

class OffNutrimentsTest {

    @Test
    fun `kcalPer100g preferuje energy-kcal_100g`() {
        val nutriments = OffNutriments(energy_kcal_100g = 250.0, energy_100g = 1000.0)
        assertEquals(250.0, nutriments.kcalPer100g())
    }

    @Test
    fun `kcalPer100g konwertuje energy_100g gdy brak kcal`() {
        val nutriments = OffNutriments(energy_kcal_100g = null, energy_100g = 418.4)
        assertEquals(100.0, nutriments.kcalPer100g(), 0.01)
    }
}
