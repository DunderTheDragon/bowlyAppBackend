package com.example.bowlyApp

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class BowlyAppApplication

fun main(args: Array<String>) {
	runApplication<BowlyAppApplication>(*args)
}
