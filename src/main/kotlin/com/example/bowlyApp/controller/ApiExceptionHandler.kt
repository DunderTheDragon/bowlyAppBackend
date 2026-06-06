package com.example.bowlyApp.controller

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ApiExceptionHandler {
    private val logger = LoggerFactory.getLogger(ApiExceptionHandler::class.java)

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleUnreadableBody(ex: HttpMessageNotReadableException): ResponseEntity<Map<String, String>> {
        val message = ex.mostSpecificCause.message ?: "Nieprawidłowy format żądania"
        logger.warn("Nie udało się odczytać body requestu: {}", message)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            mapOf(
                "error" to "Nieprawidłowe dane wejściowe",
                "message" to message
            )
        )
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException): ResponseEntity<Map<String, String>> {
        logger.warn("Odrzucono żądanie: {}", ex.message)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            mapOf(
                "error" to "Bad Request",
                "message" to (ex.message ?: "Nieprawidłowe żądanie")
            )
        )
    }
}
