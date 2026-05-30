# Bowly API — endpoint reference

All endpoints except those listed under **Public** require:

```
Authorization: Bearer <JWT>
```

Base URL: `http://<host>:8080`

---

## Public (no JWT)

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/system/status` | Health + whether initial setup is done |
| POST | `/api/system/setup` | Create first admin (empty DB only) |
| POST | `/api/auth/login` | Login → JWT |
| POST | `/api/auth/register` | Register user (requires `registrationSecret`) |

### Login

```json
POST /api/auth/login
{ "username": "user", "password": "password" }
```

### Register

```json
POST /api/auth/register
{
  "username": "user",
  "password": "password",
  "registrationSecret": "<REGISTRATION_SECRET from .env>"
}
```

---

## Users & profile (`/api/users`)

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/users` | List users (ADMIN) |
| DELETE | `/api/users/{id}` | Delete user (ADMIN) |
| GET | `/api/users/profile` | Current user profile |
| PUT | `/api/users/profile` | Update profile (weight, macros, theme, …) |

---

## Products (`/api/products`)

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/products/local` | All cached/local products |
| GET | `/api/products/search/local?query=` | Search local DB only |
| GET | `/api/products/search/external?query=` | Open Food Facts |
| GET | `/api/products/search?query=` | Combined search |
| GET | `/api/products/search/stream?query=` | SSE: local first, then external |
| GET | `/api/products/barcode/{code}` | Lookup by barcode |
| POST | `/api/products/local` | Save custom product |

External search requires at least **3 characters**. Selected products are cached automatically.

---

## Diary (`/api/diary`)

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/diary/daily?date=YYYY-MM-DD` | Daily summary + meals + workouts |
| POST | `/api/diary/consume` | Log product consumption |
| PUT | `/api/diary/meals/{id}` | Update portion weight |
| DELETE | `/api/diary/meals/{id}` | Remove diary entry |

### Consume product

```json
{
  "product": { "name": "...", "calories": 100, "protein": 10, "fat": 5, "carbohydrates": 12 },
  "weightG": 150,
  "mealDate": "2026-05-30",
  "mealType": "BREAKFAST"
}
```

`mealType`: `BREAKFAST`, `SECOND_BREAKFAST`, `LUNCH`, `SNACK`, `DINNER`, `OTHER`

Daily summary includes `burnedKcal` and `workouts[]` for calorie budget bonus.

---

## Recipes (`/api/recipes`)

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/recipes?scope=MINE&query=` | List/search recipes |
| GET | `/api/recipes/search?query=&scope=` | Search |
| POST | `/api/recipes` | Create recipe |
| PUT | `/api/recipes/{id}` | Update recipe |
| DELETE | `/api/recipes/{id}` | Delete recipe |

Recipes support **sections** (e.g. pasta + sauce). Scope: `MINE` or `ALL`.

---

## Batch meals — patelnie (`/api/batch-meals`)

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/batch-meals/active` | Active (non-depleted) batch meals |
| POST | `/api/batch-meals` | Create batch meal (+ optional `saveAsRecipe`) |
| POST | `/api/batch-meals/consume` | Take portion → diary |
| DELETE | `/api/batch-meals/{id}` | Delete batch meal |

### Consume portion

```json
{
  "segmentId": 5,
  "weightG": 200,
  "mealDate": "2026-05-30",
  "mealType": "DINNER"
}
```

---

## Workouts (`/api/workouts`)

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/workouts?date=YYYY-MM-DD` | List activities for date |
| POST | `/api/workouts` | Add activity (increases daily calorie budget) |
| DELETE | `/api/workouts/{id}` | Remove activity |

```json
POST /api/workouts
{
  "name": "Running",
  "caloriesBurned": 350,
  "activityDate": "2026-05-30"
}
```

---

## Meal type mapping (client)

| Polish (UI) | API value |
|-------------|-----------|
| Śniadanie | `BREAKFAST` |
| Drugie śniadanie | `SECOND_BREAKFAST` |
| Obiad | `LUNCH` |
| Podwieczorek | `SNACK` |
| Kolacja | `DINNER` |
| Inne | `OTHER` |
