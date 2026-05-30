package com.example.bowlyApp.repository

import com.example.bowlyApp.model.BatchMeal
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface BatchMealRepository : JpaRepository<BatchMeal, Long> {
    
    @Query("SELECT b FROM BatchMeal b LEFT JOIN FETCH b.segments s LEFT JOIN FETCH s.product WHERE b.isDepleted = false")
    fun findAllActiveWithSegments(): List<BatchMeal>
}