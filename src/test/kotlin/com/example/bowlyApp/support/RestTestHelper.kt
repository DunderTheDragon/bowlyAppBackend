package com.example.bowlyApp.support

import com.example.bowlyApp.dto.AuthResponse
import com.example.bowlyApp.dto.LoginRequest
import com.example.bowlyApp.dto.RegisterRequest
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.http.HttpStatusCode
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

object RestTestHelper {

    private val objectMapper = jacksonObjectMapper()

    fun registerAndGetToken(
        mockMvc: MockMvc,
        username: String = "testuser",
        password: String = TestFixtures.DEFAULT_PASSWORD
    ): String {
        val register = RegisterRequest(
            username = username,
            password = password,
            registrationSecret = TestFixtures.REGISTRATION_SECRET
        )
        val registerResult = mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(register))
        ).andReturn()

        if (registerResult.response.status in 200..299) {
            val token = objectMapper.readValue<AuthResponse>(registerResult.response.contentAsString).token
            if (!token.isNullOrBlank()) return token
        }

        val login = LoginRequest(username = username, password = password)
        val loginResult = mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login))
        ).andExpect(status().isOk).andReturn()

        return requireNotNull(
            objectMapper.readValue<AuthResponse>(loginResult.response.contentAsString).token
        ) { "Brak tokena po logowaniu" }
    }

    fun <T : Any> get(
        mockMvc: MockMvc,
        path: String,
        token: String,
        responseType: Class<T>
    ): T? {
        val result = mockMvc.perform(
            get(path).header("Authorization", "Bearer $token")
        ).andExpect(status().isOk).andReturn()
        return objectMapper.readValue(result.response.contentAsString, responseType)
    }

    fun <T : Any> getList(
        mockMvc: MockMvc,
        path: String,
        token: String,
        elementClass: Class<T>
    ): List<T>? {
        val result = mockMvc.perform(
            get(path).header("Authorization", "Bearer $token")
        ).andExpect(status().isOk).andReturn()
        val type = objectMapper.typeFactory.constructCollectionType(List::class.java, elementClass)
        return objectMapper.readValue(result.response.contentAsString, type)
    }

    fun getMap(
        mockMvc: MockMvc,
        path: String,
        token: String? = null
    ): Map<String, Any>? {
        var request = get(path)
        if (token != null) {
            request = request.header("Authorization", "Bearer $token")
        }
        val result = mockMvc.perform(request).andExpect(status().isOk).andReturn()
        @Suppress("UNCHECKED_CAST")
        return objectMapper.readValue(result.response.contentAsString, Map::class.java) as Map<String, Any>?
    }

    fun getPublicMap(mockMvc: MockMvc, path: String): Map<String, Any>? =
        getMap(mockMvc, path)

    fun <T : Any> post(
        mockMvc: MockMvc,
        path: String,
        token: String,
        body: Any,
        responseType: Class<T>
    ): T? {
        val result = mockMvc.perform(
            post(path)
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body))
        ).andExpect(status().isOk).andReturn()
        return objectMapper.readValue(result.response.contentAsString, responseType)
    }

    fun postEmpty(
        mockMvc: MockMvc,
        path: String,
        token: String,
        body: Any
    ) {
        mockMvc.perform(
            post(path)
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body))
        ).andExpect(status().isOk)
    }

    fun <T : Any> put(
        mockMvc: MockMvc,
        path: String,
        token: String,
        body: Any,
        responseType: Class<T>
    ): T? {
        val result = mockMvc.perform(
            put(path)
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body))
        ).andExpect(status().isOk).andReturn()
        return objectMapper.readValue(result.response.contentAsString, responseType)
    }

    fun delete(
        mockMvc: MockMvc,
        path: String,
        token: String
    ) {
        mockMvc.perform(
            delete(path).header("Authorization", "Bearer $token")
        ).andExpect(status().isOk)
    }

    fun postExpectError(
        mockMvc: MockMvc,
        path: String,
        body: Any
    ): HttpStatusCode {
        val result = mockMvc.perform(
            post(path)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body))
        ).andReturn()
        return HttpStatusCode.valueOf(result.response.status)
    }

    fun getExpectError(
        mockMvc: MockMvc,
        path: String
    ): HttpStatusCode {
        val result = mockMvc.perform(get(path)).andReturn()
        return HttpStatusCode.valueOf(result.response.status)
    }
}
