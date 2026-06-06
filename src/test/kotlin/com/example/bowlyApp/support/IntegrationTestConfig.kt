package com.example.bowlyApp.support

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestTemplate

@TestConfiguration
class IntegrationTestConfig {

    @Bean
    fun restClientBuilder(objectMapper: ObjectMapper): RestClient.Builder {
        val restTemplate = RestTemplate().apply {
            messageConverters = listOf(MappingJackson2HttpMessageConverter(objectMapper))
        }
        return RestClient.builder(restTemplate)
    }
}
