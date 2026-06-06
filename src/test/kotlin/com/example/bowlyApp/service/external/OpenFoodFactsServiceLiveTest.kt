package com.example.bowlyApp.service.external

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import com.example.bowlyApp.support.IntegrationTestBase

/**
 * Test na żywo wymaga sieci — uruchamiany tylko z tagiem external:
 * ./gradlew test -DincludeTags=external
 */
@Tag("external")
@SpringBootTest
@ActiveProfiles("test")
class OpenFoodFactsServiceLiveTest : IntegrationTestBase() {

    @Autowired
    lateinit var openFoodFactsService: OpenFoodFactsService

    @Test
    fun `live search zwraca wyniki dla jogurtu`() = runBlocking {
        val results = openFoodFactsService.searchProducts("jogurt")

        assertTrue(results.isNotEmpty(), "Wyszukiwanie 'jogurt' powinno zwrócić przynajmniej 1 wynik.")
        val firstResult = results.first()
        assertNotNull(firstResult.name)
        assertNotNull(firstResult.externalId)
        assertEquals("OPEN_FOOD_FACTS", firstResult.source)
    }
}
