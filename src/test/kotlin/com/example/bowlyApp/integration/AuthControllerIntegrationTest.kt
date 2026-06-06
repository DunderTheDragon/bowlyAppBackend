package com.example.bowlyApp.integration

import com.example.bowlyApp.dto.UserDto
import com.example.bowlyApp.support.IntegrationTestBase
import com.example.bowlyApp.support.RestTestHelper
import com.example.bowlyApp.support.TestFixtures
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class AuthControllerIntegrationTest : IntegrationTestBase() {

    @Test
    fun `register i login zwracają token JWT`() {
        val token = RestTestHelper.registerAndGetToken(rest, username = "auth_user")

        val profile = RestTestHelper.get(rest, "/api/users/profile", token, UserDto::class.java)

        assertEquals("auth_user", profile?.username)
    }

    @Test
    fun `register odrzuca błędny registration secret`() {
        val request = TestFixtures.registerRequest(username = "bad_secret_user")
            .copy(registrationSecret = "wrong")

        val status = RestTestHelper.postExpectError(rest, "/api/auth/register", request)
        assertEquals(HttpStatus.BAD_REQUEST, status)
    }

    @Test
    fun `chroniony endpoint zwraca 401 bez tokena`() {
        val status = RestTestHelper.getExpectError(rest, "/api/users/profile")
        assertTrue(
            status == HttpStatus.UNAUTHORIZED || status == HttpStatus.FORBIDDEN,
            "Oczekiwano 401 lub 403, otrzymano $status"
        )
    }
}
