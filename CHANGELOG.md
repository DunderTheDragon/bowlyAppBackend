# Changelog

All notable changes to this project will be documented in this file.

## [0.1.0] — 2026-05-30

### Added

- Initial open-source release of the Bowly backend API
- JWT auth, multi-user households, registration secret gate
- Calorie diary with meal types and daily macro summaries
- Virtual batch meals (*patelnie*) with multi-segment support and portion locking
- Product search: local cache and Open Food Facts
- User recipes with sections; batch creation from recipes
- Workout activities increasing daily calorie budget
- User profile with TDEE-based targets and precise macro ratios
- Docker Compose stack with PostgreSQL and Flyway migrations (V1–V8)

### Removed

- Spoonacular integration and unused admin API keys endpoints (product search uses Open Food Facts only)
