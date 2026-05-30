package com.example.bowlyApp.controller

import com.example.bowlyApp.dto.ConsumeProductRequest
import com.example.bowlyApp.dto.DailySummaryDto
import com.example.bowlyApp.service.DiaryService
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/api/diary")
class DiaryController(
    private val diaryService: DiaryService
) {

    @GetMapping("/daily")
    fun getDailySummary(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate,
        authentication: Authentication
    ): ResponseEntity<DailySummaryDto> {
        val username = authentication.name
        val summary = diaryService.getDailySummary(username, date)
        return ResponseEntity.ok(summary)
    }

    @PostMapping("/consume")
    fun consumeProduct(
        @RequestBody request: ConsumeProductRequest,
        authentication: Authentication
    ): ResponseEntity<String> {
        diaryService.consumeProduct(authentication.name, request)
        return ResponseEntity.ok("Product consumed")
    }

    @DeleteMapping("/meals/{id}")
    fun deleteConsumedMeal(
        @PathVariable id: Long,
        authentication: Authentication
    ): ResponseEntity<String> {
        diaryService.deleteConsumedMeal(authentication.name, id)
        return ResponseEntity.ok("Meal deleted")
    }

    @PutMapping("/meals/{id}")
    fun updateConsumedMeal(
        @PathVariable id: Long,
        @RequestBody request: ConsumeProductRequest,
        authentication: Authentication
    ): ResponseEntity<String> {
        diaryService.updateConsumedMeal(authentication.name, id, request)
        return ResponseEntity.ok("Meal updated")
    }
}
