package com.example.bowlyApp.controller

import com.example.bowlyApp.dto.CreateWorkoutActivityRequest
import com.example.bowlyApp.dto.WorkoutActivityDto
import com.example.bowlyApp.service.WorkoutService
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/api/workouts")
class WorkoutController(
    private val workoutService: WorkoutService
) {

    @GetMapping
    fun getActivities(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate,
        authentication: Authentication
    ): ResponseEntity<List<WorkoutActivityDto>> {
        return ResponseEntity.ok(workoutService.getActivitiesForDate(authentication.name, date))
    }

    @PostMapping
    fun addActivity(
        @RequestBody request: CreateWorkoutActivityRequest,
        authentication: Authentication
    ): ResponseEntity<WorkoutActivityDto> {
        return ResponseEntity.ok(workoutService.addActivity(authentication.name, request))
    }

    @DeleteMapping("/{id}")
    fun deleteActivity(
        @PathVariable id: Long,
        authentication: Authentication
    ): ResponseEntity<Void> {
        workoutService.deleteActivity(authentication.name, id)
        return ResponseEntity.noContent().build()
    }
}
