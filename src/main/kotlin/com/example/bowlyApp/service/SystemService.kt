package com.example.bowlyApp.service

import com.example.bowlyApp.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class SystemService(
    private val userRepository: UserRepository
) {

    val isSystemSetup: Boolean
        get() = userRepository.count() > 0
}
