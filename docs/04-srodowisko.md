# 4. Środowisko uruchomieniowe

## 4.1 Wymagania

| Komponent | Wymaganie |
|-----------|-----------|
| Produkcja / dev Docker | Docker + Docker Compose v2 |
| Dev backend (opcjonalnie) | JDK 17+, Gradle, PostgreSQL |
| Dev frontend | JDK 17, Android SDK |
| Testy integracyjne backendu | **Docker** (Testcontainers) |
| Testy jednostkowe | JDK wystarczy |

## 4.2 Uruchomienie backendu (Docker)

```bash
cp .env.example .env
# Ustaw JWT_SECRET (min. 32 znaki), REGISTRATION_SECRET, POSTGRES_PASSWORD
docker compose up -d --build
curl http://localhost:8742/api/system/status
```

| Usługa | Port |
|--------|------|
| API | 8742 (domyślnie) |
| PostgreSQL (host) | 5433 → 5432 w kontenerze |

## 4.3 Zmienne środowiskowe (.env)

| Zmienna | Opis |
|---------|------|
| `JWT_SECRET` | Klucz HS256 (min. 32 znaki) |
| `REGISTRATION_SECRET` | Hasło instancji przy rejestracji |
| `POSTGRES_PASSWORD` | Hasło bazy |
| `SERVER_PORT` | Port API (domyślnie 8742) |
| `JWT_EXPIRATION_MS` | Czas życia tokena (domyślnie 24h) |

## 4.4 Frontend — instalacja APK

1. Uruchom backend.
2. Pobierz APK z [GitHub Releases](https://github.com/DunderTheDragon/bowlyApp/releases) (`composeApp-debug.apk`).
3. W aplikacji podaj URL serwera (np. `http://192.168.1.10:8742`).
4. Zarejestruj konto z `REGISTRATION_SECRET`.

## 4.5 CI/CD

**Backend** (`.github/workflows/test.yml`):
- push/PR na `main` → `./gradlew test jacocoTestReport`
- wymaga Docker na runnerze (Testcontainers)

**Frontend** (`bowly/.github/workflows/`):
- `test.yml` — testy jednostkowe przy push/PR
- `release-apk.yml` — tag `v*` → testy + build APK + GitHub Release

## 4.6 Uruchamianie testów (skrót)

Backend:
```bash
./gradlew test
./gradlew test jacocoTestReport   # raport: build/reports/jacoco/test/html/
```

Frontend (w katalogu `bowly/`):
```bash
./gradlew :composeApp:testDebugUnitTest
```

Testy zewnętrzne Open Food Facts (wymaga sieci):
```bash
./gradlew test -DincludeTags=external
```

Smoke test manualny (backend w Docker):
```bash
./scripts/smoke-test.sh http://localhost:8742
```

Szczegóły: [08-testy.md](08-testy.md).
