package com.example.bowlyApp.service

import com.example.bowlyApp.dto.SetupRequest
import com.example.bowlyApp.model.User
import com.example.bowlyApp.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SystemService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {

    val isSystemSetup: Boolean
        get() = userRepository.count() > 0

    @Transactional
    fun setupSystem(request: SetupRequest) {
        if (isSystemSetup) {
            throw IllegalStateException("System is already configured. Cannot run setup again.")
        }

        val encodedPassword = requireNotNull(passwordEncoder.encode(request.adminPassword))

        val adminUser = User(
            username = request.adminUsername,
            passwordHash = encodedPassword,
            role = "ADMIN"
        )
        userRepository.save(adminUser)
    }
}
