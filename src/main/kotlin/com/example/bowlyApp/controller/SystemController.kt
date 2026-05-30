package com.example.bowlyApp.controller

import com.example.bowlyApp.service.SystemService
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
}
