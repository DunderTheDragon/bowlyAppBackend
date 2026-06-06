package com.example.bowlyApp.integration

import com.example.bowlyApp.dto.UserDto
import com.example.bowlyApp.support.IntegrationTestBase
import com.example.bowlyApp.support.RestTestHelper
import com.example.bowlyApp.support.RestTestHelper.bearer
import com.example.bowlyApp.support.TestFixtures
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.client.HttpClientErrorException

class AuthControllerIntegrationTest : IntegrationTestBase() {

    @Test
    fun `register i login zwracają token JWT`() {
        val token = RestTestHelper.registerAndGetToken(rest, username = "auth_user")

        val profile = rest.get()
            .uri("/api/users/profile")
            .bearer(token)
            .retrieve()
            .body(UserDto::class.java)

        assertEquals("auth_user", profile?.username)
    }

    @Test
    fun `register odrzuca błędny registration secret`() {
        val request = TestFixtures.registerRequest(username = "bad_secret_user")
            .copy(registrationSecret = "wrong")

        val ex = assertThrows(HttpClientErrorException::class.java) {
            rest.post()
                .uri("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(String::class.java)
        }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun `chroniony endpoint zwraca 401 bez tokena`() {
        val ex = assertThrows(HttpClientErrorException::class.java) {
            rest.get()
                .uri("/api/users/profile")
                .retrieve()
                .body(String::class.java)
        }
        assertTrue(
            ex.statusCode == HttpStatus.UNAUTHORIZED || ex.statusCode == HttpStatus.FORBIDDEN,
            "Oczekiwano 401 lub 403, otrzymano ${ex.statusCode}"
        )
    }
}
