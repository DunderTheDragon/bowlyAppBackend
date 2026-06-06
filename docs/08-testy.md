# 8. Testy

## 8.1 Strategia

| Poziom | Backend | Frontend |
|--------|---------|----------|
| Jednostkowe | Serwisy z MockK (repozytoria zamockowane) | Helpery, mapowania, SettingsManager, ApiService + MockEngine |
| Integracyjne | REST + PostgreSQL (Testcontainers) + Flyway V1–V10 | — |
| Wdrożeniowe | `DeploymentFlowIntegrationTest` — pełny scenariusz HTTP→DB | CI: build APK po testach |
| Zewnętrzne | Open Food Facts live (`@Tag("external")`) | — |

**Frontend:** świadomie **bez** testów UI/instrumented — niska wartość vs. koszt; logika w helperach i ApiService.

## 8.2 Backend — klasy testowe

### Jednostkowe (`src/test/kotlin/.../service/`)

| Klasa testowa | Testowana logika |
|---------------|------------------|
| `AuthServiceTest` | Rejestracja, login, secret, duplikat user |
| `BatchMealServiceTest` | Tworzenie, consume, edycja wagi gotowej |
| `DiaryServiceTest` | Summary makro, delete przywraca patelnię |
| `MealRecipeServiceTest` | CRUD, ownership, scope MINE |
| `ProductServiceTest` | Local search, findOrCreate, dedup key |
| `WorkoutServiceTest` | Dodawanie, walidacja, uprawnienia |
| `WeighingContainerServiceTest` | CRUD, walidacja wagi |
| `JwtUtilTest` | Generowanie i walidacja tokena |
| `OffNutrimentsTest` | Mapowanie kcal z OFF |

### Integracyjne (`src/test/kotlin/.../integration/`)

| Klasa | Zakres |
|-------|--------|
| `AuthControllerIntegrationTest` | JWT, 401, zły secret |
| `SystemControllerIntegrationTest` | Status publiczny |
| `ProductControllerIntegrationTest` | POST/GET produkt |
| `BatchMealControllerIntegrationTest` | Patelnia → consume → PUT segment |
| `DiaryControllerIntegrationTest` | Consume + daily |
| `MealRecipeControllerIntegrationTest` | CRUD przepis |
| `WorkoutControllerIntegrationTest` | POST/DELETE trening |
| `WeighingContainerControllerIntegrationTest` | CRUD naczynia |
| `DeploymentFlowIntegrationTest` | **Smoke E2E** całego stosu |

Infrastruktura: `IntegrationTestBase`, `TestFixtures`, `RestTestHelper`.

## 8.3 Frontend — klasy testowe (`composeApp/src/commonTest/`)

| Plik | Zakres |
|------|--------|
| `MacroRatioUtilsTest` | Normalizacja, display 100%, adjust |
| `BatchMealHelpersTest` | Kcal porcji, barcode, validate, buildRequest |
| `ClockUtilsTest` | Format daty API |
| `RecipeMappingTest` | DTO ↔ API model |
| `SettingsManagerTest` | clearSession vs clear |
| `ApiServiceTest` | MockEngine: status, login |

## 8.4 Uruchamianie

**Backend** (wymaga Docker dla testów integracyjnych):
```bash
./gradlew test
./gradlew test jacocoTestReport
open build/reports/tests/test/index.html
open build/reports/jacoco/test/html/index.html
```

**Frontend** (katalog `bowly/`):
```bash
./gradlew :composeApp:testDebugUnitTest
```

**Wyłączenie testów wymagających Docker** — obecnie integracyjne są w domyślnym `test`; bez Docker testy integracyjne się nie uruchomią. Na CI (ubuntu-latest) Docker jest dostępny.

## 8.5 CI

- Backend: `.github/workflows/test.yml`
- Frontend: `bowly/.github/workflows/test.yml` + testy przed APK w `release-apk.yml`

## 8.6 Przykładowy wynik (jednostkowe)

Po `./gradlew test` (z Docker): ~50 testów, w tym ~40 jednostkowych przechodzi lokalnie bez integracji; pełny zielony build na GitHub Actions z Testcontainers.

Frontend: 21 testów w `testDebugUnitTest` (BUILD SUCCESSFUL).
