package com.example.bowlyApp.integration

import com.example.bowlyApp.dto.*
import com.example.bowlyApp.support.IntegrationTestBase
import com.example.bowlyApp.support.RestTestHelper
import com.example.bowlyApp.support.TestFixtures
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.core.ParameterizedTypeReference
import org.springframework.web.util.UriComponentsBuilder

class SystemControllerIntegrationTest : IntegrationTestBase() {

    @Test
    fun `status jest publiczny bez autoryzacji`() {
        val response = RestTestHelper.getPublicMap(rest, "/api/system/status")
        assertNotNull(response?.get("isSetup"))
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
        val created = RestTestHelper.post(
            rest,
            "/api/products/local",
            token,
            product,
            ProductSearchResult::class.java
        )

        assertEquals("Ryż", created?.name)

        val createdId = requireNotNull(created?.id)
        val searchPath = UriComponentsBuilder.fromPath("/api/products/search/local")
            .queryParam("query", "Ryż")
            .build()
            .toUriString()

        val search = RestTestHelper.getList(
            rest,
            searchPath,
            token,
            object : ParameterizedTypeReference<List<ProductSearchResult>>() {}
        )

        assertTrue(search?.any { it.id == createdId } == true)
    }
}
