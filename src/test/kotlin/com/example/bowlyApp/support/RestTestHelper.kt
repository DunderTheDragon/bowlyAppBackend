package com.example.bowlyApp.support

import com.example.bowlyApp.dto.AuthResponse
import com.example.bowlyApp.dto.LoginRequest
import com.example.bowlyApp.dto.RegisterRequest
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpStatusCode
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.client.RestTestClient

object RestTestHelper {

    fun registerAndGetToken(
        rest: RestTestClient,
        username: String = "testuser",
        password: String = TestFixtures.DEFAULT_PASSWORD
    ): String {
        val register = RegisterRequest(
            username = username,
            password = password,
            registrationSecret = TestFixtures.REGISTRATION_SECRET
        )
        val registerResult = rest.post()
            .uri("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .body(register)
            .exchange()
            .returnResult(AuthResponse::class.java)

        registerResult.responseBody?.token?.takeIf { it.isNotBlank() }?.let { return it }

        val login = LoginRequest(username = username, password = password)
        val loginResult = rest.post()
            .uri("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .body(login)
            .exchange()
            .expectStatus().is2xxSuccessful()
            .returnResult(AuthResponse::class.java)

        return requireNotNull(loginResult.responseBody?.token) { "Brak tokena po logowaniu" }
    }

    fun <T : Any> get(
        rest: RestTestClient,
        path: String,
        token: String,
        responseType: Class<T>
    ): T? = rest.get()
        .uri(path)
        .headers { it.setBearerAuth(token) }
        .exchange()
        .expectStatus().is2xxSuccessful()
        .returnResult(responseType)
        .responseBody

    fun <T : Any> getList(
        rest: RestTestClient,
        path: String,
        token: String,
        responseType: ParameterizedTypeReference<List<T>>
    ): List<T>? = rest.get()
        .uri(path)
        .headers { it.setBearerAuth(token) }
        .exchange()
        .expectStatus().is2xxSuccessful()
        .returnResult(responseType)
        .responseBody

    @Suppress("UNCHECKED_CAST")
    fun getMap(
        rest: RestTestClient,
        path: String,
        token: String? = null
    ): Map<String, Any>? {
        val request = rest.get().uri(path)
        if (token != null) {
            request.headers { it.setBearerAuth(token) }
        }
        return request.exchange()
            .expectStatus().is2xxSuccessful()
            .returnResult(Map::class.java)
            .responseBody as Map<String, Any>?
    }

    fun getPublicMap(rest: RestTestClient, path: String): Map<String, Any>? =
        getMap(rest, path)

    fun <T : Any> post(
        rest: RestTestClient,
        path: String,
        token: String,
        body: Any,
        responseType: Class<T>
    ): T? = rest.post()
        .uri(path)
        .headers { it.setBearerAuth(token) }
        .contentType(MediaType.APPLICATION_JSON)
        .body(body)
        .exchange()
        .expectStatus().is2xxSuccessful()
        .returnResult(responseType)
        .responseBody

    fun postEmpty(
        rest: RestTestClient,
        path: String,
        token: String,
        body: Any
    ) {
        rest.post()
            .uri(path)
            .headers { it.setBearerAuth(token) }
            .contentType(MediaType.APPLICATION_JSON)
            .body(body)
            .exchange()
            .expectStatus().is2xxSuccessful()
    }

    fun <T : Any> put(
        rest: RestTestClient,
        path: String,
        token: String,
        body: Any,
        responseType: Class<T>
    ): T? = rest.put()
        .uri(path)
        .headers { it.setBearerAuth(token) }
        .contentType(MediaType.APPLICATION_JSON)
        .body(body)
        .exchange()
        .expectStatus().is2xxSuccessful()
        .returnResult(responseType)
        .responseBody

    fun delete(
        rest: RestTestClient,
        path: String,
        token: String
    ) {
        rest.delete()
            .uri(path)
            .headers { it.setBearerAuth(token) }
            .exchange()
            .expectStatus().is2xxSuccessful()
    }

    fun postExpectError(
        rest: RestTestClient,
        path: String,
        body: Any
    ): HttpStatusCode = rest.post()
        .uri(path)
        .contentType(MediaType.APPLICATION_JSON)
        .body(body)
        .exchange()
        .returnResult(String::class.java)
        .status

    fun getExpectError(
        rest: RestTestClient,
        path: String
    ): HttpStatusCode = rest.get()
        .uri(path)
        .exchange()
        .returnResult(String::class.java)
        .status
}
