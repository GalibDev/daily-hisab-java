# Run Daily Hisab

## Requirements

- JDK 17 or 21 (Android Studio's bundled JBR works)
- Android Studio and Android SDK 36
- MySQL 8.4, either locally installed or via Docker
- Internet for the first Gradle dependency download

The Gradle wrapper is included. Open this repository root in Android Studio.
Set `JAVA_HOME` to your JDK and `ANDROID_HOME` to the Android SDK for terminal builds.

## 1. MySQL

For Docker, in PowerShell:

```powershell
$env:DB_PASSWORD = 'choose-your-local-password'
$env:MYSQL_ROOT_PASSWORD = 'choose-another-local-password'
docker compose up -d
```

Or create a database/user in your existing MySQL server:

```sql
CREATE DATABASE daily_hisab CHARACTER SET utf8mb4;
CREATE USER 'daily_hisab'@'localhost' IDENTIFIED BY 'choose-your-local-password';
GRANT ALL PRIVILEGES ON daily_hisab.* TO 'daily_hisab'@'localhost';
```

The backend initializes the four tables from `backend/src/main/resources/schema.sql`.
`CREATE TABLE IF NOT EXISTS` creates a fresh schema; it is not an upgrade/migration system.

## 2. Backend

In a PowerShell terminal at the repository root:

```powershell
$env:DB_PASSWORD = 'choose-your-local-password'
# Optional overrides:
$env:DB_URL = 'jdbc:mysql://localhost:3306/daily_hisab'
$env:DB_USER = 'daily_hisab'
.\gradlew.bat :backend:bootRun
```

Keep that terminal running. API default port: 8080.
Database credentials are environment variables, never Android app settings.

## 3. Android

```powershell
.\gradlew.bat :app:assembleDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`.
Install on an Android 8+ emulator/device. On the login screen, open **Server settings**:

- Emulator: `http://10.0.2.2:8080`
- Physical phone: `http://YOUR_PC_LAN_IP:8080`, with phone and PC on the same network.

If a physical phone cannot connect, allow your Java backend through Windows Firewall on your private network.
Local HTTP is enabled only for debug builds. A deployed/release app needs an HTTPS API.
Create an account; default categories are created automatically. There is no hardcoded demo password.

## Tests

```powershell
.\gradlew.bat :backend:test :app:lintDebug :app:assembleDebug
```

Default integration tests use a disposable H2 database in MySQL compatibility mode.
To verify real MySQL, create a **separate empty test database** and set:

```powershell
$env:TEST_DB_URL = 'jdbc:mysql://localhost:3306/daily_hisab_test'
$env:TEST_DB_USER = 'daily_hisab_test'
$env:TEST_DB_PASSWORD = 'your-test-password'
.\gradlew.bat :backend:test --rerun-tasks
```

Tests delete all rows from the selected database. Never point `TEST_DB_URL` at your personal ledger.

For the Android UI smoke test, start the backend and an emulator first, then run:

```powershell
.\gradlew.bat :app:connectedDebugAndroidTest
```

This test registers a uniquely named demo account, adds an expense and checks the dashboard/monthly report. It expects the emulator default URL (`http://10.0.2.2:8080`) and leaves demo data in the backend database.

GitHub Actions also runs the API tests against a MySQL service, checks Android lint, and uploads the debug APK. The emulator test is run locally.

## Version 1 boundaries

- Server connection required; no offline synchronization.
- Transaction list shows latest 500 matching entries; narrow filters for older records.
- Money uses BDT, SQL DECIMAL and Java BigDecimal.
- English interface; Bangla translation is future work.
- Categories can be added; renaming/deleting categories, export and charts are future work.
- Sessions expire after seven days. HTTPS deployment, password reset and login rate limiting are future production work.
