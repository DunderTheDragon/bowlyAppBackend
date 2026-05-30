package com.example.bowlyApp.service.external

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class OpenFoodFactsServiceTest {

    @Autowired
    lateinit var openFoodFactsService: OpenFoodFactsService

    @Test
    fun `test wyszukiwania jogurtu w OpenFoodFacts api`() {
        runBlocking {
            val results = openFoodFactsService.searchProducts("jogurt")
            
            // Oczekujemy, że wyszukiwanie tak pospolitego produktu zwróci przynajmniej 1 wynik
            assertTrue(results.isNotEmpty(), "Wyszukiwanie 'jogurt' powinno zwrócić przynajmniej 1 wynik.")
            
            val firstResult = results.first()
            assertNotNull(firstResult.name, "Produkt z OpenFoodFacts musi mieć nazwę.")
            assertNotNull(firstResult.externalId, "Produkt z OpenFoodFacts musi mieć przypisane ID (barcode).")
            assertEquals("OPEN_FOOD_FACTS", firstResult.source)
            
            println("Znaleziono ${results.size} produktów. Pierwszy z nich to: ${firstResult.name} (kcal: ${firstResult.calories})")
        }
    }
}
