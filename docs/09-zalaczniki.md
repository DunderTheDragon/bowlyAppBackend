# 9. Załączniki

## 9.1 Linki

| Zasób | URL |
|-------|-----|
| Backend GitHub | https://github.com/DunderTheDragon/bowlyAppBackend |
| Frontend GitHub | https://github.com/DunderTheDragon/bowlyApp |
| APK (latest) | https://github.com/DunderTheDragon/bowlyApp/releases/latest |
| Open Food Facts | https://world.openfoodfacts.org |

## 9.2 Pliki projektu

| Plik | Opis |
|------|------|
| [ENDPOINTS.md](../ENDPOINTS.md) | REST API |
| [CHANGELOG.md](../CHANGELOG.md) | Historia wersji backendu |
| [compose.yaml](../compose.yaml) | Docker Compose |
| [.env.example](../.env.example) | Szablon zmiennych |

## 9.3 Smoke test shell

Skrypt `scripts/smoke-test.sh` — manualna weryfikacja API na działającym Docker Compose:

```bash
chmod +x scripts/smoke-test.sh
./scripts/smoke-test.sh http://localhost:8742
```

Wykonuje: status → rejestracja → login → produkt → patelnia → consume → diary.

## 9.4 Wersjonowanie APK

| Wersja | Tag Git | versionCode |
|--------|---------|-------------|
| 1.0 | v1.0.0 | 1 |
| 1.1 | v1.1.0 | 2 |

Workflow `release-apk.yml` buduje APK przy push tagu `v*`.

## 9.5 Licencja

MIT — patrz [LICENSE](../LICENSE).
