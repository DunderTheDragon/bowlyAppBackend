package com.example.bowlyApp.support

import org.testcontainers.containers.PostgreSQLContainer

object PostgresTestSupport {
    val container: PostgreSQLContainer<*> = PostgreSQLContainer("postgres:15-alpine")
        .withDatabaseName("bowly_test")
        .withUsername("test")
        .withPassword("test")

    init {
        if (System.getenv("SPRING_DATASOURCE_URL") == null) {
            container.start()
        }
    }
}
