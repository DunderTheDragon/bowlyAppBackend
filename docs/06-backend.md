# 6. Backend — architektura i kod

## 6.1 Struktura pakietów

```
com.example.bowlyApp/
├── controller/     # REST endpoints
├── service/        # Logika biznesowa
├── repository/     # Spring Data JPA
├── model/          # Encje JPA
├── dto/            # Request/response JSON
├── security/       # JWT, SecurityConfig
└── config/         # WebClient
```

## 6.2 Bezpieczeństwo JWT

Publiczne ścieżki: `/api/auth/**`, `/api/system/status`. Pozostałe wymagają tokena Bearer.

```kotlin
// SecurityConfig.kt — fragment
.authorizeHttpRequests { auth ->
    auth
        .requestMatchers("/api/auth/**").permitAll()
        .requestMatchers("/api/system/status").permitAll()
        .anyRequest().authenticated()
}
.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
```

`JwtUtil` generuje token z `subject = username`; filtr ustawia `SecurityContext` bez pełnego `UserDetailsService`.

## 6.3 Patelnie — consume z blokadą

Przy nakładaniu porcji używana jest **pesymistyczna blokada** segmentu, aby uniknąć race condition przy współdzieleniu patelni:

```kotlin
// BatchMealService.kt — fragment consumePortion
val segment = batchMealSegmentRepository.findByIdWithPessimisticLock(request.segmentId)
    ?: throw IllegalArgumentException("Segment not found")
// ...
segment.currentWeightG -= request.weightG
val consumedPortion = ConsumedPortion(
    user = user,
    segment = segment,
    consumedWeightG = request.weightG,
    weightBasisG = segment.initialWeightG  // snapshot dla dziennika
)
```

## 6.4 Dziennik — przeliczanie makro

`DiaryService.getDailySummary` rozróżnia wpisy z patelni (proporcja do `weight_basis_g`) i zwykłe produkty (na 100 g):

```kotlin
if (segment != null) {
    val basis = portion.weightBasisG ?: segment.initialWeightG
    val ratio = portion.consumedWeightG / basis
    kcal = segment.totalKcal * ratio
} else if (product != null) {
    val ratio = portion.consumedWeightG / 100.0
    kcal = product.kcalPer100g * ratio
}
```

## 6.5 Główne endpointy

Pełna lista: [ENDPOINTS.md](../ENDPOINTS.md).

| Grupa | Przykłady |
|-------|-----------|
| Auth | POST `/api/auth/login`, `/register` |
| Diary | GET `/api/diary/daily`, POST `/consume` |
| Batch meals | POST `/api/batch-meals`, POST `/consume`, PUT `/{id}/segments/{segmentId}` |
| Containers | CRUD `/api/containers` |
| Products | GET `/search`, POST `/local` |

## 6.6 Obsługa błędów

`ApiExceptionHandler` (`@RestControllerAdvice`) mapuje m.in. `IllegalArgumentException` na HTTP 400 z czytelnym komunikatem JSON — ważne dla klienta mobilnego.
