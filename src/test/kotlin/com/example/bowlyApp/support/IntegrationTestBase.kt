package com.example.bowlyApp.support

import com.example.bowlyApp.dto.AuthResponse
import com.example.bowlyApp.dto.LoginRequest
import com.example.bowlyApp.dto.RegisterRequest
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestTemplate
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
abstract class IntegrationTestBase {

    @LocalServerPort
    protected var port: Int = 0

    @Autowired
    protected lateinit var objectMapper: ObjectMapper

    protected lateinit var rest: RestClient

    @BeforeEach
    fun setUpRestClient() {
        val restTemplate = RestTemplate().apply {
            messageConverters = listOf(MappingJackson2HttpMessageConverter(objectMapper))
        }
        rest = RestClient.builder(restTemplate)
            .baseUrl("http://127.0.0.1:$port")
            .build()
    }

    companion object {
        @Container
        @JvmStatic
        val postgres: PostgreSQLContainer<*> = PostgreSQLContainer("postgres:15-alpine")
            .withDatabaseName("bowly_test")
            .withUsername("test")
            .withPassword("test")

        @DynamicPropertySource
        @JvmStatic
        fun configureProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
        }
    }
}

object RestTestHelper {

    fun registerAndGetToken(
        rest: RestClient,
        username: String = "testuser",
        password: String = TestFixtures.DEFAULT_PASSWORD
    ): String {
        val register = RegisterRequest(
            username = username,
            password = password,
            registrationSecret = TestFixtures.REGISTRATION_SECRET
        )
        try {
            val registerBody = rest.post()
                .uri("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .body(register)
                .retrieve()
                .body(AuthResponse::class.java)

            if (registerBody?.token?.isNotBlank() == true) return registerBody.token
        } catch (_: org.springframework.web.client.HttpClientErrorException) {
            // Użytkownik mógł już istnieć z wcześniejszego testu integracyjnego
        }

        val login = LoginRequest(username = username, password = password)
        val loginBody = rest.post()
            .uri("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .body(login)
            .retrieve()
            .body(AuthResponse::class.java)

        return requireNotNull(loginBody?.token) { "Brak tokena po logowaniu" }
    }

    fun <T : RestClient.RequestHeadersSpec<T>> T.bearer(token: String): T = apply {
        header("Authorization", "Bearer $token")
    }
}
