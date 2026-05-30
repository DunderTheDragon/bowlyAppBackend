package com.example.bowlyApp.service

import com.example.bowlyApp.dto.LoginRequest
import com.example.bowlyApp.dto.RegisterRequest
import com.example.bowlyApp.model.User
import com.example.bowlyApp.repository.UserRepository
import com.example.bowlyApp.security.JwtUtil
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtUtil: JwtUtil,
    @Value("\${app.registration.secret}") private val registrationSecret: String
) {

    fun register(request: RegisterRequest): User {
        if (request.registrationSecret != registrationSecret) {
            throw IllegalArgumentException("Invalid registration secret")
        }

        if (userRepository.existsByUsername(request.username)) {
            throw IllegalArgumentException("Username is already taken")
        }

        val encodedPassword = requireNotNull(passwordEncoder.encode(request.password)) {
            "Password encoder returned null"
        }

        val user = User(
            username = request.username,
            passwordHash = encodedPassword,
            role = "USER"
        )

        return userRepository.save(user)
    }

    fun login(request: LoginRequest): Pair<String, String> {
        val user = userRepository.findByUsername(request.username)
            ?: throw IllegalArgumentException("Invalid username or password")

        if (!passwordEncoder.matches(request.password, user.passwordHash)) {
            throw IllegalArgumentException("Invalid username or password")
        }

        return Pair(jwtUtil.generateToken(user.username), user.role)
    }
}