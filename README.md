# Daily Hisab Java

A university Android project for personal income and expense tracking.

## Architecture

Java + XML Android app → Java Spring Boot REST API → MySQL.
Database credentials stay on the server. Each user has a separate ledger.

## Version 2 features

- Register, login and logout
- Income and expense CRUD
- Default and custom categories
- Date, category and type filters
- Dashboard totals and monthly category reports
- Bangla / English interface and persistent language preference
- Monthly income/expense charts and category expense share
- Overall and category budgets with 80% / limit warnings
- Literal note/category search combined with transaction filters
- Safe category rename/delete; used categories are protected
- Full-ledger CSV export through Android's document picker
- Profile editing and password changes with session revocation

Implementation is recorded in small, ordered Git commits. See docs/ for setup and presentation notes.

- [Setup and build](docs/SETUP.md)
- [Database / ER diagram / university demo](docs/DATABASE.md)
- [API reference](docs/API.md)
- [Phase 2 behavior and demo](docs/PHASE2.md)

Android minimum: 8.0 (API 26). Java application code throughout; no Kotlin or JavaScript runtime.
