package com.example.bowlyApp.repository

import com.example.bowlyApp.model.MealRecipe
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface MealRecipeRepository : JpaRepository<MealRecipe, Long> {
    
    // Pobiera przepis wraz ze składnikami (zapobiega N+1 queries)
    @Query("SELECT r FROM MealRecipe r LEFT JOIN FETCH r.ingredients i LEFT JOIN FETCH i.product WHERE r.id = :id")
    fun findByIdWithIngredients(id: Long): MealRecipe?
    
    @Query("SELECT r FROM MealRecipe r LEFT JOIN FETCH r.ingredients i LEFT JOIN FETCH i.product")
    fun findAllWithIngredients(): List<MealRecipe>
}
