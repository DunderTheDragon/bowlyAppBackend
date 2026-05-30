# Bowly — backend API

Self-hosted backend for **Bowly** — an open-source calorie tracker with **virtual batch meals** (*patelnie*): cook once, log portions over several days, share within a household.

Built with **Kotlin**, **Spring Boot 4**, **PostgreSQL**, and **Flyway** migrations.

> **Note:** The mobile/desktop client (Kotlin Multiplatform) lives in a **separate repository**.  
> The `bowly/` directory may exist locally for development convenience but is **gitignored** here and must not be committed to this repo.

---

## Features

- JWT authentication, multi-user households, registration secret for self-hosted instances
- Calorie diary with meal types and daily macro summaries
- **Virtual batch meals** — multi-segment pots with pessimistic locking on portion consume
- Product search: local cache → Open Food Facts (incl. Polish products)
- User recipes with sections; batch meal creation from recipes
- Workout activities that increase the daily calorie budget
- User profile: BMR/TDEE targets, macro ratio splits, theme preference

---

## Requirements

- [Docker](https://docs.docker.com/get-docker/) and Docker Compose v2
- (Optional) JDK 17+ and Gradle for local development without Docker

---

## Quick start (Docker)

### 1. Configure environment

```bash
cp .env.example .env
```

Edit `.env` and set at minimum:

| Variable | Description |
|----------|-------------|
| `JWT_SECRET` | Random string, **at least 32 characters** (HS256) |
| `REGISTRATION_SECRET` | Instance password required to register — prevents bots on publicly exposed backends |
| `POSTGRES_PASSWORD` | Database password (change from default for production) |

### 2. Start the stack

```bash
docker compose up -d --build
```

This starts:

| Service | URL / port |
|---------|------------|
| API | `http://localhost:8080` |
| PostgreSQL | `localhost:5433` (host) → `5432` (container) |

Database data is stored in the Docker volume `postgres_data`.

### 3. Verify

```bash
docker compose logs -f backend
```

Wait for: `Started BowlyAppApplicationKt`

```bash
curl http://localhost:8080/api/system/status
```

### 4. Create your account

Register the first user (and any household members) with the instance password from `.env`:

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "jan",
    "password": "your-secure-password",
    "registrationSecret": "value-from-REGISTRATION_SECRET-in-env"
  }'
```

The response includes a JWT. You can also log in later:

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "jan", "password": "your-secure-password"}'
```

Use the returned `token` as `Authorization: Bearer <token>` for all other endpoints.

---

## Configuration reference

| Variable | Default (local) | Description |
|----------|-----------------|-------------|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://db:5432/bowly_db` | JDBC URL |
| `SPRING_DATASOURCE_USERNAME` | `postgres` | DB user |
| `SPRING_DATASOURCE_PASSWORD` | `postgres` | DB password |
| `JWT_SECRET` | *(required)* | JWT signing key |
| `JWT_EXPIRATION_MS` | `86400000` | Token lifetime (ms) |
| `REGISTRATION_SECRET` | *(required)* | Instance password required to register new users |

Example Spring config: [`src/main/resources/application.properties.example`](src/main/resources/application.properties.example)

---

## Development without Docker

1. Run PostgreSQL on port `5433` (or adjust URL).
2. Export required env vars (`JWT_SECRET`, `REGISTRATION_SECRET`).
3. Run:

```bash
./gradlew bootRun
```

Migrations run automatically via Flyway on startup.

---

## API documentation

See **[ENDPOINTS.md](ENDPOINTS.md)** for the full REST API reference.

Public endpoints (no JWT):

- `GET /api/system/status`
- `POST /api/auth/login`
- `POST /api/auth/register` *(requires `registrationSecret` from `.env`)*

All other `/api/**` routes require `Authorization: Bearer <JWT>`.

---

## Project structure

```
├── src/main/kotlin/com/example/bowlyApp/
│   ├── controller/     REST endpoints
│   ├── service/        Business logic
│   ├── model/          JPA entities
│   ├── repository/     Spring Data
│   ├── dto/            Request/response models
│   └── security/       JWT filter & config
├── src/main/resources/db/migration/   Flyway SQL (V1–V8)
├── compose.yaml        Production-like local stack
├── Dockerfile          Multi-stage JAR build
├── .env.example        Copy to .env before first run
└── ENDPOINTS.md        API reference
```

---

## Stopping & resetting

```bash
# Stop containers (data preserved)
docker compose down

# Stop and remove database volume
docker compose down -v
```

---

## Troubleshooting

**Backend exits on startup — JWT / registration secret**

Ensure `.env` defines `JWT_SECRET` (≥ 32 chars) and `REGISTRATION_SECRET`.

**Flyway migration checksum error**

Only on databases that ran older migration files. On a dev instance: `docker compose down -v` and start fresh. Production: use `flyway repair` or restore from backup.

**Client cannot reach API**

- Emulator Android: `http://10.0.2.2:8080`
- Physical device: `http://<your-LAN-IP>:8080`
- Ensure port 8080 is not blocked by firewall

---

## License

[MIT](LICENSE)
