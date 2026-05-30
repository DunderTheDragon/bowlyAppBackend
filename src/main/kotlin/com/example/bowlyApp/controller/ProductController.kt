package com.example.bowlyApp.controller

import com.example.bowlyApp.dto.ProductSearchResult
import com.example.bowlyApp.service.ProductService
import kotlinx.coroutines.runBlocking
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.concurrent.Executors

@RestController
@RequestMapping("/api/products")
class ProductController(
    private val productService: ProductService
) {
    private val sseExecutor = Executors.newCachedThreadPool()

    /**
     * Strumień wyników wyszukiwania (SSE):
     * 1. natychmiast wyniki z lokalnej bazy,
     * 2. po zakończeniu odpytywania API — wyniki zewnętrzne (min. 3 znaki).
     */
    @GetMapping("/search/stream")
    fun searchProductsStream(@RequestParam query: String): SseEmitter {
        val emitter = SseEmitter(60_000L)

        sseExecutor.execute {
            runBlocking {
                try {
                    val localResults = productService.searchLocalProducts(query)
                    localResults.forEach { product ->
                        emitter.send(SseEmitter.event().name("product").data(product))
                    }

                    if (query.length >= ProductService.MIN_EXTERNAL_QUERY_LENGTH) {
                        val externalResults = productService.searchExternalProducts(query)
                        val localKeys = localResults.map { ProductService.productDedupKey(it) }.toSet()
                        externalResults
                            .filter { ProductService.productDedupKey(it) !in localKeys }
                            .forEach { product ->
                                emitter.send(SseEmitter.event().name("product").data(product))
                            }
                    }

                    emitter.send(SseEmitter.event().name("done").data(""))
                    emitter.complete()
                } catch (e: Exception) {
                    emitter.completeWithError(e)
                }
            }
        }

        return emitter
    }

    @GetMapping("/search/local")
    fun searchLocalProducts(@RequestParam query: String): ResponseEntity<List<ProductSearchResult>> {
        return ResponseEntity.ok(productService.searchLocalProducts(query))
    }

    @GetMapping("/search/external")
    fun searchExternalProducts(@RequestParam query: String): ResponseEntity<List<ProductSearchResult>> {
        if (query.length < ProductService.MIN_EXTERNAL_QUERY_LENGTH) {
            return ResponseEntity.ok(emptyList())
        }
        val results = runBlocking {
            productService.searchExternalProducts(query)
        }
        return ResponseEntity.ok(results)
    }

    @GetMapping("/search")
    fun searchProducts(@RequestParam query: String): ResponseEntity<List<ProductSearchResult>> {
        val results = runBlocking {
            productService.searchProducts(query)
        }
        return ResponseEntity.ok(results)
    }

    @GetMapping("/barcode/{barcode}")
    fun getProductByBarcode(@PathVariable barcode: String): ResponseEntity<ProductSearchResult> {
        val result = runBlocking {
            productService.getProductByBarcode(barcode)
        }
        return if (result != null) {
            ResponseEntity.ok(result)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping("/local")
    fun getLocalProducts(): ResponseEntity<List<ProductSearchResult>> {
        val results = productService.getLocalProducts()
        return ResponseEntity.ok(results)
    }

    @PostMapping("/local")
    fun saveLocalProduct(@RequestBody product: ProductSearchResult): ResponseEntity<ProductSearchResult> {
        val savedProduct = productService.saveLocalProduct(product)
        return ResponseEntity.ok(productService.run { savedProduct.toSearchResult() })
    }
}
