# Daily Hisab Java

A university Android project for personal income and expense tracking.

## Architecture

Java + XML Android app → Java Spring Boot REST API → MySQL.
Database credentials stay on the server. Each user has a separate ledger.

## Version 1 scope

- Register, login and logout
- Income and expense CRUD
- Default and custom categories
- Date, category and type filters
- Dashboard totals and monthly category reports

Implementation is recorded in small, ordered Git commits. See docs/ for setup and presentation notes.

- [Setup and build](docs/SETUP.md)
- [Database / ER diagram / university demo](docs/DATABASE.md)
- [API reference](docs/API.md)

Android minimum: 8.0 (API 26). Java application code throughout; no Kotlin or JavaScript runtime.
