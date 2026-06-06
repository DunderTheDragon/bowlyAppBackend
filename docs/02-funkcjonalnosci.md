# 2. Funkcjonalności

## 2.1 Autentykacja i użytkownicy

| Funkcja | Opis |
|---------|------|
| Rejestracja | Wymaga `registrationSecret` z konfiguracji serwera — ochrona przed botami |
| Logowanie JWT | Token Bearer w nagłówku `Authorization` |
| Profile | Waga, wzrost, TDEE, proporcje makro, motyw jasny/ciemny |
| Panel admina | Lista użytkowników, klucze rejestracji (rola ADMIN) |

## 2.2 Dziennik kalorii

- Podgląd dnia z podziałem na typ posiłku (śniadanie, obiad, kolacja, przekąska).
- Sumy kcal, białka, tłuszczu, węglowodanów.
- Logowanie **zwykłego produktu** (bez patelni) lub **porcji z patelni**.
- Edycja i usuwanie wpisów; przy usunięciu porcji z patelni waga wraca na segment.
- **Treningi** — dodatkowy budżet kaloryczny (`burnedKcal` w podsumowaniu dnia).

## 2.3 Wirtualne patelnie (*batch meals*)

Patelnia składa się z **sekcji** (np. mięso, ryż, warzywa). Każda sekcja ma:
- wagę początkową i bieżącą,
- makro całej sekcji (przeliczane proporcjonalnie przy nakładaniu porcji).

Operacje:
- utworzenie patelni (z produktów lub z przepisu),
- opcjonalny zapis jako przepis lokalny,
- nakładanie porcji → wpis w dzienniku + zmniejszenie `currentWeightG`,
- edycja wagi **po gotowaniu** (skalowanie porcji),
- archiwizacja gdy wszystkie sekcje wyczerpane.

## 2.4 Produkty i przepisy

- Baza lokalna produktów (cache).
- Wyszukiwanie w **Open Food Facts** (min. 3 znaki).
- Skan kodu kreskowego (Android — ML Kit).
- Przepisy z **sekcjami składników**; filtrowanie MINE / ALL.

## 2.5 Naczynia (tara)

Globalne naczynia instancji (talerz, patelnia, garnek):
- CRUD `/api/containers`,
- waga tary w gramach, opcjonalne zdjęcie base64,
- użycie w UI przy ważeniu (odejmowanie tary od wagi brutto).

## 2.6 Frontend — ekrany

| Ekran | Odpowiedzialność |
|-------|------------------|
| ServerAddress / Login | Konfiguracja URL serwera, logowanie, rejestracja |
| Dashboard | Dziennik dnia, nawigacja dat, treningi |
| BatchMeals | Lista patelni, tworzenie, edycja, onboarding |
| AddMealSelection | Dodawanie posiłku, wyszukiwanie, skaner, porcje z patelni |
| Profile | TDEE, makro, motyw, link do produktów/naczyń |
| MyProducts / MyContainers | Zarządzanie lokalnymi danymi |
| Admin | Użytkownicy (rola ADMIN) |
