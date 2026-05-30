package com.example.bowlyApp.controller

import com.example.bowlyApp.dto.CreateMealRecipeRequest
import com.example.bowlyApp.dto.MealRecipeDto
import com.example.bowlyApp.service.MealRecipeService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/recipes")
class MealRecipeController(
    private val mealRecipeService: MealRecipeService
) {

    @PostMapping
    fun createRecipe(
        @RequestBody request: CreateMealRecipeRequest,
        authentication: Authentication
    ): ResponseEntity<MealRecipeDto> {
        val created = mealRecipeService.createLocalRecipe(authentication.name, request)
        return ResponseEntity.ok(created)
    }

    @PutMapping("/{id}")
    fun updateRecipe(
        @PathVariable id: Long,
        @RequestBody request: CreateMealRecipeRequest,
        authentication: Authentication
    ): ResponseEntity<MealRecipeDto> {
        val updated = mealRecipeService.updateLocalRecipe(authentication.name, id, request)
        return ResponseEntity.ok(updated)
    }

    @GetMapping
    fun getRecipes(
        @RequestParam(required = false) query: String?,
        @RequestParam(defaultValue = "MINE") scope: String,
        @RequestParam(required = false) singleMeal: Boolean?,
        authentication: Authentication
    ): ResponseEntity<List<MealRecipeDto>> {
        val results = if (query.isNullOrBlank()) {
            mealRecipeService.getAllRecipes(authentication.name, scope, singleMeal)
        } else {
            mealRecipeService.searchRecipes(authentication.name, query, scope, singleMeal)
        }
        return ResponseEntity.ok(results)
    }

    @GetMapping("/search")
    fun searchRecipes(
        @RequestParam query: String,
        @RequestParam(defaultValue = "MINE") scope: String,
        @RequestParam(required = false) singleMeal: Boolean?,
        authentication: Authentication
    ): ResponseEntity<List<MealRecipeDto>> {
        return ResponseEntity.ok(
            mealRecipeService.searchRecipes(authentication.name, query, scope, singleMeal)
        )
    }

    @DeleteMapping("/{id}")
    fun deleteRecipe(
        @PathVariable id: Long,
        authentication: Authentication
    ): ResponseEntity<Void> {
        mealRecipeService.deleteRecipe(authentication.name, id)
        return ResponseEntity.noContent().build()
    }
}
