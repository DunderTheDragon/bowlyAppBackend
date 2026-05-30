package com.example.bowlyApp.controller

import com.example.bowlyApp.dto.SetupRequest
import com.example.bowlyApp.service.SystemService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/system")
class SystemController(
    private val systemService: SystemService
) {

    @GetMapping("/status")
    fun getStatus(): ResponseEntity<Map<String, Boolean>> {
        return ResponseEntity.ok(mapOf("isSetup" to systemService.isSystemSetup))
    }

    @PostMapping("/setup")
    fun setupSystem(@Valid @RequestBody request: SetupRequest): ResponseEntity<String> {
        return try {
            systemService.setupSystem(request)
            ResponseEntity.ok("System configured successfully. You can now login as admin.")
        } catch (e: IllegalStateException) {
            ResponseEntity.badRequest().body(e.message)
        }
    }
}
