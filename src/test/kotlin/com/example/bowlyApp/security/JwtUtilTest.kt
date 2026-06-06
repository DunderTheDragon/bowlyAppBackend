package com.example.bowlyApp.security

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class JwtUtilTest {

    private lateinit var jwtUtil: JwtUtil

    @BeforeEach
    fun setUp() {
        jwtUtil = JwtUtil(
            secret = "test-jwt-secret-key-minimum-32-characters-long",
            expirationMs = 3600_000
        )
    }

    @Test
    fun `generateToken i getUsernameFromToken zwracają spójnego użytkownika`() {
        val token = jwtUtil.generateToken("alice")

        assertTrue(jwtUtil.validateToken(token))
        assertEquals("alice", jwtUtil.getUsernameFromToken(token))
    }

    @Test
    fun `validateToken zwraca false dla niepoprawnego tokena`() {
        assertFalse(jwtUtil.validateToken("invalid.token.value"))
    }

    @Test
    fun `validateToken zwraca false dla pustego stringa`() {
        assertFalse(jwtUtil.validateToken(""))
    }
}
