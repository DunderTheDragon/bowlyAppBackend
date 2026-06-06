package com.example.bowlyApp.service

import com.example.bowlyApp.dto.LoginRequest
import com.example.bowlyApp.repository.UserRepository
import com.example.bowlyApp.security.JwtUtil
import com.example.bowlyApp.support.TestFixtures
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.security.crypto.password.PasswordEncoder

class AuthServiceTest {

    private val userRepository = mockk<UserRepository>()
    private val passwordEncoder = mockk<PasswordEncoder>()
    private val jwtUtil = mockk<JwtUtil>()
    private lateinit var authService: AuthService

    @BeforeEach
    fun setUp() {
        authService = AuthService(
            userRepository = userRepository,
            passwordEncoder = passwordEncoder,
            jwtUtil = jwtUtil,
            registrationSecret = TestFixtures.REGISTRATION_SECRET
        )
    }

    @Test
    fun `register tworzy użytkownika przy poprawnym secrecie`() {
        val request = TestFixtures.registerRequest()
        every { userRepository.existsByUsername("testuser") } returns false
        every { passwordEncoder.encode("secret12") } returns "hashed"
        every { userRepository.save(any()) } answers {
            firstArg<com.example.bowlyApp.model.User>().copy(id = 1L)
        }

        val user = authService.register(request)

        assertEquals("testuser", user.username)
        verify { userRepository.save(match { it.username == "testuser" && it.passwordHash == "hashed" }) }
    }

    @Test
    fun `register odrzuca niepoprawny registration secret`() {
        val request = TestFixtures.registerRequest().copy(registrationSecret = "wrong")

        val ex = assertThrows(IllegalArgumentException::class.java) {
            authService.register(request)
        }
        assertEquals("Invalid registration secret", ex.message)
    }

    @Test
    fun `register odrzuca zajętą nazwę użytkownika`() {
        every { userRepository.existsByUsername("testuser") } returns true

        val ex = assertThrows(IllegalArgumentException::class.java) {
            authService.register(TestFixtures.registerRequest())
        }
        assertEquals("Username is already taken", ex.message)
    }

    @Test
    fun `login zwraca token i rolę przy poprawnych danych`() {
        val user = TestFixtures.user(passwordHash = "hashed")
        every { userRepository.findByUsername("testuser") } returns user
        every { passwordEncoder.matches("secret12", "hashed") } returns true
        every { jwtUtil.generateToken("testuser") } returns "jwt-token"

        val (token, role) = authService.login(TestFixtures.loginRequest())

        assertEquals("jwt-token", token)
        assertEquals("USER", role)
    }

    @Test
    fun `login odrzuca nieistniejącego użytkownika`() {
        every { userRepository.findByUsername("testuser") } returns null

        assertThrows(IllegalArgumentException::class.java) {
            authService.login(TestFixtures.loginRequest())
        }
    }

    @Test
    fun `login odrzuca błędne hasło`() {
        val user = TestFixtures.user(passwordHash = "hashed")
        every { userRepository.findByUsername("testuser") } returns user
        every { passwordEncoder.matches("secret12", "hashed") } returns false

        assertThrows(IllegalArgumentException::class.java) {
            authService.login(LoginRequest("testuser", "secret12"))
        }
    }
}
