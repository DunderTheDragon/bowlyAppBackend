# Etap 1: Budowanie aplikacji (z uzyciem Gradle)
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

# Kopiowanie plikow Gradle
COPY gradlew .
RUN chmod +x gradlew
COPY gradle/ gradle/
COPY build.gradle.kts .
COPY settings.gradle.kts .
# Jeśli masz gradle.properties to również skopiuj
# COPY gradle.properties .

# Pobieranie zaleznosci (uruchamiane przed kopiowaniem kodu zrodlowego,
# zeby zoptymalizowac cache warstw Dockera)
RUN ./gradlew dependencies --no-daemon

# Kopiowanie kodu zrodlowego
COPY src/ src/

# Budowanie aplikacji (tworzenie pliku .jar)
RUN ./gradlew bootJar --no-daemon

# Etap 2: Środowisko uruchomieniowe
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Kopiowanie zbudowanego pliku .jar z etapu 1
COPY --from=builder /app/build/libs/*SNAPSHOT.jar app.jar

# Udostępnianie portu dla aplikacji Spring Boot
EXPOSE 8080

# Komenda uruchamiajaca aplikacje
ENTRYPOINT ["java", "-jar", "app.jar"]