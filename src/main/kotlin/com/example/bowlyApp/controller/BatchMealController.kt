package com.example.bowlyApp.controller

import com.example.bowlyApp.dto.BatchMealDto
import com.example.bowlyApp.dto.ConsumePortionRequest
import com.example.bowlyApp.dto.CreateBatchMealRequest
import com.example.bowlyApp.service.BatchMealService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/batch-meals")
class BatchMealController(
    private val batchMealService: BatchMealService
) {
    @PostMapping
    fun createBatchMeal(
        @RequestBody request: CreateBatchMealRequest,
        authentication: Authentication
    ): ResponseEntity<BatchMealDto> {
        val created = batchMealService.createBatchMeal(authentication.name, request)
        return ResponseEntity.ok(created)
    }

    @GetMapping("/active")
    fun getActiveBatchMeals(): ResponseEntity<List<BatchMealDto>> {
        return ResponseEntity.ok(batchMealService.getActiveBatchMeals())
    }

    @PostMapping("/consume")
    fun consumePortion(
        @RequestBody request: ConsumePortionRequest,
        authentication: Authentication
    ): ResponseEntity<String> {
        batchMealService.consumePortion(authentication.name, request)
        return ResponseEntity.ok("Portion consumed successfully")
    }

    @DeleteMapping("/{id}")
    fun deleteBatchMeal(@PathVariable id: Long): ResponseEntity<Void> {
        batchMealService.deleteBatchMeal(id)
        return ResponseEntity.noContent().build()
    }
}