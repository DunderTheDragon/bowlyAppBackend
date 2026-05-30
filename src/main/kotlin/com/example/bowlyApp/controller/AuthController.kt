package com.example.bowlyApp.controller

import com.example.bowlyApp.dto.AuthResponse
import com.example.bowlyApp.dto.LoginRequest
import com.example.bowlyApp.dto.RegisterRequest
import com.example.bowlyApp.service.AuthService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService
) {

    @PostMapping("/register")
    fun register(@Valid @RequestBody request: RegisterRequest): ResponseEntity<AuthResponse> {
        return try {
            val user = authService.register(request)
            val (token, role) = authService.login(LoginRequest(request.username, request.password))
            ResponseEntity.ok(AuthResponse(token, user.username, role, "Registration successful"))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(AuthResponse("", "", "", e.message))
        }
    }

    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<AuthResponse> {
        return try {
            val (token, role) = authService.login(request)
            ResponseEntity.ok(AuthResponse(token, request.username, role, "Login successful"))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(AuthResponse("", "", "", e.message))
        }
    }
}
