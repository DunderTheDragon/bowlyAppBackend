package com.example.bowlyApp.integration

import com.example.bowlyApp.dto.ProductSearchResult
import com.example.bowlyApp.support.IntegrationTestBase
import com.example.bowlyApp.support.RestTestHelper
import com.example.bowlyApp.support.RestTestHelper.bearer
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.MediaType

class SystemControllerIntegrationTest : IntegrationTestBase() {

    @Test
    fun `status jest publiczny bez autoryzacji`() {
        val response = rest.get()
            .uri("/api/system/status")
            .retrieve()
            .body(object : ParameterizedTypeReference<Map<String, Any>>() {})

        assertNotNull(response?.get("status"))
    }
}

class ProductControllerIntegrationTest : IntegrationTestBase() {

    private lateinit var token: String

    @BeforeEach
    fun auth() {
        token = RestTestHelper.registerAndGetToken(rest, username = "product_user")
    }

    @Test
    fun `POST local i GET search zwracają produkt`() {
        val product = ProductSearchResult(
            name = "Ryż",
            source = "LOCAL",
            calories = 130.0,
            protein = 2.7,
            fat = 0.3,
            carbohydrates = 28.0
        )
        val created = rest.post()
            .uri("/api/products/local")
            .bearer(token)
            .contentType(MediaType.APPLICATION_JSON)
            .body(product)
            .retrieve()
            .body(ProductSearchResult::class.java)

        assertEquals("Ryż", created?.name)

        val search = rest.get()
            .uri("/api/products/search/local?query=Ryż")
            .bearer(token)
            .retrieve()
            .body(object : ParameterizedTypeReference<List<ProductSearchResult>>() {})

        assertTrue(search?.any { it.name == "Ryż" } == true)
    }
}
