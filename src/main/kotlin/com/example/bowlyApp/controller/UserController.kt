package com.example.bowlyApp.controller

import com.example.bowlyApp.dto.UserDto
import com.example.bowlyApp.model.User
import com.example.bowlyApp.repository.UserRepository
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userRepository: UserRepository
) {

    @GetMapping
    fun getAllUsers(): ResponseEntity<List<UserDto>> {
        val users = userRepository.findAll().map {
            mapToDto(it)
        }
        return ResponseEntity.ok(users)
    }

    @DeleteMapping("/{id}")
    fun deleteUser(@PathVariable id: Long): ResponseEntity<String> {
        val user = userRepository.findById(id)
        if (user.isEmpty) {
            return ResponseEntity.notFound().build()
        }

        val userEntity = user.get()
        if (userEntity.role == "ADMIN") {
            val adminCount = userRepository.findAll().count { it.role == "ADMIN" }
            if (adminCount <= 1) {
                return ResponseEntity.badRequest().body("Nie można usunąć ostatniego administratora.")
            }
        }

        userRepository.deleteById(id)
        return ResponseEntity.ok("Użytkownik został usunięty.")
    }

    @GetMapping("/profile")
    fun getUserProfile(authentication: Authentication): ResponseEntity<UserDto> {
        val user = userRepository.findByUsername(authentication.name)
            ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(mapToDto(user))
    }

    @PutMapping("/profile")
    fun updateUserProfile(
        @RequestBody updatedProfile: UserDto,
        authentication: Authentication
    ): ResponseEntity<UserDto> {
        val user = userRepository.findByUsername(authentication.name)
            ?: return ResponseEntity.notFound().build()

        updatedProfile.gender?.let { user.gender = it }
        updatedProfile.age?.let { user.age = java.lang.Integer(it) }
        updatedProfile.heightCm?.let { user.heightCm = java.lang.Double(it) }
        updatedProfile.weightKg?.let { user.weightKg = java.lang.Double(it) }
        updatedProfile.targetWeightKg?.let { user.targetWeightKg = java.lang.Double(it) }
        updatedProfile.weeklyChangeRateKg?.let { user.weeklyChangeRateKg = java.lang.Double(it) }
        updatedProfile.activityLevel?.let { user.activityLevel = java.lang.Double(it) }
        updatedProfile.proteinRatio?.let { user.proteinRatio = java.lang.Double(it) }
        updatedProfile.fatRatio?.let { user.fatRatio = java.lang.Double(it) }
        updatedProfile.carbsRatio?.let { user.carbsRatio = java.lang.Double(it) }
        updatedProfile.isDarkTheme?.let { user.isDarkTheme = java.lang.Boolean(it) }
        updatedProfile.showBatchOnboarding?.let { user.showBatchOnboarding = java.lang.Boolean(it) }

        val saved = userRepository.save(user)
        return ResponseEntity.ok(mapToDto(saved))
    }

    private fun mapToDto(user: User): UserDto {
        return UserDto(
            id = user.id,
            username = user.username,
            role = user.role,
            gender = user.gender,
            age = user.age?.toInt(),
            heightCm = user.heightCm?.toDouble(),
            weightKg = user.weightKg?.toDouble(),
            targetWeightKg = user.targetWeightKg?.toDouble(),
            weeklyChangeRateKg = user.weeklyChangeRateKg?.toDouble(),
            activityLevel = user.activityLevel?.toDouble(),
            proteinRatio = user.proteinRatio?.toDouble(),
            fatRatio = user.fatRatio?.toDouble(),
            carbsRatio = user.carbsRatio?.toDouble(),
            isDarkTheme = user.isDarkTheme?.booleanValue(),
            showBatchOnboarding = user.showBatchOnboarding?.booleanValue()
        )
    }
}
