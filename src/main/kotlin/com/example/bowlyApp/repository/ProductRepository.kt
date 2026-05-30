package com.example.bowlyApp.repository

import com.example.bowlyApp.model.Product
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ProductRepository : JpaRepository<Product, Long> {
    fun findByNameContainingIgnoreCase(name: String): List<Product>
    fun findBySourceAndExternalId(source: String, externalId: String): Product?
    fun existsBySourceAndExternalId(source: String, externalId: String): Boolean
    fun findByBarcode(barcode: String): Product?
}