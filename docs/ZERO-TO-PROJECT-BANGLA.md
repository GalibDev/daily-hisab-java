# Daily Hisab: zero theke nijer hate build, edit ar explain

Ei guide tomar current **Java + XML Android app, Java Spring Boot API, MySQL** project-er code onujayi lekha. Codex charao Android Studio, terminal ar MySQL diye project build/edit kora jay. Shob command repository root theke run korbe.

## 1. Age puro system-ta bojho

```text
User button chape
    ↓
Android XML screen + MainActivity.java
    ↓  HTTP request / JSON
ApiClient.java
    ↓
Java Spring Boot controller
    ↓  JDBC / parameterized SQL
MySQL database
    ↓  result
JSON response → Android screen update
```

**Java:** button-er kaj, calculation, network ar backend logic.
**XML:** Android screen-er layout, color, size, text ar icon.
**SQL:** database-e table banano, data save/read/update/delete, report calculate kora.
**Spring Boot:** Java backend start kore API endpoint provide kore.
**JDBC/JdbcTemplate:** Java theke SQL execute kore.
**Gradle:** dependency download, Java compile, tests ar APK build kore.

Android app sorasori MySQL-e connect kore na. Database password backend-e thake. App API-te request pathay.

## 2. Ki ki age shikhbe

Ei order follow koro; shudhu code mukhosto korbe na.

1. Java variable, String, int, boolean, if/else, loop, method.
2. Class, object, constructor, interface, access modifier.
3. List, Map, exception, callback, BigDecimal.
4. Android Activity, onCreate, View, Button, EditText, click listener.
5. XML LinearLayout, ScrollView, layout_width/height, dp/sp.
6. SQL CREATE TABLE, INSERT, SELECT, UPDATE, DELETE, JOIN, GROUP BY.
7. HTTP GET/POST/PUT/DELETE, status code, JSON, authentication token.
8. Spring Boot controller, validation, JdbcTemplate, transaction.

Choto practice: console Java-te income ar expense niye balance ber koro. Tarpor XML-e amount input ar Save button banao. Tarpor database-er ekta table-e data insert koro. Ei tin part bujhle main app-er flow dhora easy.

## 3. Existing project nijer hate open ar run

### Android Studio

1. File → Open → `D:\PROJECTS\daily-hisab-java` select koro.
2. Folder-e `settings.gradle`, `app`, `backend` ache kina dekho.
3. Gradle sync complete hote dao.
4. Run configuration `app` select koro.
5. Emulator select kore Run, ba Shift+F10 dao.
6. Emulator screen dekhte View → Tool Windows → Running Devices kholo.

`C:\projects\daily-hisab-java`-er khali `.idea` folder khulle app import hobe na. `.idea` IDE-r settings; application-er source code na.

Green Run button abar enabled howa nijer moddhe error na. App screen, Build output ar Logcat diye result check korbe.

### Ei PC-r terminal environment

Android Studio-r terminal PowerShell hole:

```powershell
cd D:\PROJECTS\daily-hisab-java
$env:JAVA_HOME = 'D:\Apps\Android Studio\jbr'
$env:ANDROID_HOME = 'D:\Android\Sdk'
.\gradlew.bat :app:assembleDebug
```

Onno PC-te installed JDK ar SDK-r actual path use korbe. `local.properties`-e `sdk.dir=D:/Android/Sdk` ei PC-r jonno. File-ta Git-e commit korbe na.

APK ber hoy: `app/build/outputs/apk/debug/app-debug.apk`.

### MySQL ar backend

MySQL server age start korte hobe. MySQL Workbench ba mysql client-e admin connection diye:

```sql
CREATE DATABASE daily_hisab CHARACTER SET utf8mb4;
CREATE USER 'daily_hisab'@'localhost' IDENTIFIED BY 'your-local-password';
GRANT ALL PRIVILEGES ON daily_hisab.* TO 'daily_hisab'@'localhost';
```

Egulo fresh setup-er command. Database/user age theke thakle abar CREATE na kore existing setup use koro. Workbench ekta client; shudhu Workbench khulle MySQL server automatically running hobe emon na.

Tarpor PowerShell terminal-e:

```powershell
$env:JAVA_HOME = 'D:\Apps\Android Studio\jbr'
$env:ANDROID_HOME = 'D:\Android\Sdk'
$env:DB_URL = 'jdbc:mysql://localhost:3306/daily_hisab'
$env:DB_USER = 'daily_hisab'
$env:DB_PASSWORD = 'your-local-password'
.\gradlew.bat :backend:bootRun
```

`your-local-password` nijer MySQL user-er password diye replace korbe. Terminal khola rakhbe. Backend normally port 8080-te chole; schema.sql theke missing table create hoy.

Ei PC-te testing-er jonno alada portable MySQL **3307** port-e prepare kora hoyeche. Oita use korle, MySQL server running obosthay ignored local config load kora jay:

```powershell
. .\.local\env.ps1
.\gradlew.bat :backend:bootRun
```

`.local/env.ps1` credential-bearing local file; eta GitHub-e upload ba presentation-e show korbe na. Ei file onno PC-te thakbe na. Normal installation-er 3306 ar prepared local server-er 3307 gulie felbe na.

Android app-er Server settings:

- Emulator: `http://10.0.2.2:8080`
- Physical phone: computer-er LAN IP, jemon `http://192.168.1.10:8080`; actual IP use korbe, phone/PC same network-e thakbe.

Emulator-er `localhost` emulator-ke bojhay. `10.0.2.2` diye host computer-e pouchhay. Debug build local HTTP use korte pare; release app-er API HTTPS lagbe.

## 4. Kon file kon kaj kore

| File / folder | Kaj |
| --- | --- |
| settings.gradle | app ar backend module declare kore |
| build.gradle | Android/Spring Boot plugin version |
| app/build.gradle | app ID, SDK, version, Java compatibility |
| app/src/main/AndroidManifest.xml | Activity, internet permission, label, icon |
| app/src/main/res/layout/auth.xml | login/register layout |
| app/src/main/res/layout/home.xml | dashboard, search, bottom navigation |
| app/src/main/res/values/strings.xml | English layout text |
| app/src/main/res/values-bn/strings.xml | Bangla layout text |
| app/src/main/res/values/styles.xml | theme, navigation button style |
| app/src/main/res/drawable/ | card backgrounds ar vector icons |
| app/src/main/java/com/dailyhisab/javaapp/MainActivity.java | screen, click listener, forms, API callbacks |
| app/src/main/java/com/dailyhisab/javaapp/ApiClient.java | background HTTP request ar JSON response |
| app/src/main/java/com/dailyhisab/javaapp/ReportChart.java | Canvas diye bar ar donut chart draw kore |
| app/src/main/java/com/dailyhisab/javaapp/Translations.java | dynamic Java UI text-er Bangla translation |
| backend/src/main/java/com/dailyhisab/api/HisabApplication.java | backend-er main entry point |
| backend/src/main/java/com/dailyhisab/api/AuthController.java | register/login/logout, password hash, session |
| backend/src/main/java/com/dailyhisab/api/WebConfig.java | protected request-er token verify kore |
| backend/src/main/java/com/dailyhisab/api/LedgerController.java | transaction CRUD, search, summary, monthly report |
| backend/src/main/java/com/dailyhisab/api/PlanningController.java | category rename/delete, budgets |
| backend/src/main/java/com/dailyhisab/api/ProfileController.java | profile ar password change |
| backend/src/main/java/com/dailyhisab/api/ExportController.java | full-ledger CSV generate kore |
| backend/src/main/java/com/dailyhisab/api/ApiErrors.java | exception-ke consistent HTTP error response banay |
| backend/src/main/resources/application.properties | database/server configuration |
| backend/src/main/resources/schema.sql | 5ta table-er SQL |
| backend/src/test/ | API integration tests |
| app/src/androidTest/ | emulator UI test |
| .github/workflows/verify.yml | GitHub-e automatic test/build |

`.gradle`, `build`, APK ar `.idea`-r generated file diye shuru korbe na. Source code edit korbe `src` folder-e.

## 5. Zero theke notun kore banate chaile

Existing project-er upor overwrite na kore alada practice folder nao.

### Step A: project scope ar screen list

Prothome kagoj-e lekho: login, dashboard, add expense, transaction list. Ekbare budget/chart/export diye shuru korbe na.

### Step B: Java Android skeleton

Android Studio → New Project → Views/XML-based Activity template nao. Language option-e Java select koro; Compose/Kotlin template nile ei guide-er UI workflow milbe na. Template-er naam Studio version onujayi alada hote pare.

Minimum SDK API 26 rakha jay; ei project-er compile/target SDK 36. Prothom success: emulator-e ekta TextView ar Button show kora.

### Step C: screen ar local calculation

XML-e EditText + Button + TextView banao. Java click listener-e input read kore result dekhao. Prothome database chara flow bujho.

```java
Button save = findViewById(R.id.save);
save.setOnClickListener(view -> {
    String raw = amountInput.getText().toString();
    try {
        BigDecimal amount = new BigDecimal(raw);
        if (amount.signum() <= 0) {
            amountInput.setError("Enter a positive amount");
            return;
        }
        resultView.setText(amount.toPlainString());
    } catch (NumberFormatException error) {
        amountInput.setError("Enter a valid amount");
    }
});
```

Eta **practice snippet**, current project-e as-is paste korar full file na. `amountInput`, `resultView` declare ar XML-er `save` ID create korte hobe. `R.id...` XML resource ID-r generated reference.

### Step D: MySQL schema

Age users, categories, transactions-er relationship design koro. Pore sessions ar budgets add koro. `schema.sql` pore nijer practice database-e table banao. Real data-thaka database drop korbe na.

### Step E: Java backend skeleton

Spring Initializr (`https://start.spring.io`) diye Java/Gradle project generate kora jay. Ei repo Java 17-compatible Spring Boot 3.5.7 use kore. Nijer chosen version-er Java requirement official documentation-e check korbe.

Dependencies: Spring Web, JDBC, Validation, MySQL driver; password hashing-er jonno spring-security-crypto. Ei project full Spring Security login framework use kore na; bearer-session validation nijer interceptor-e kore.

Current repo-te Android ar backend ek root-er dui Gradle module. Beginner practice-e backend alada project/folder hote pare; functional flow eki.

### Step F: API diye ekta entry save

Age sudhu POST transaction ar GET transaction list banao. Request validation ar current user check add koro. API client diye response test kore tarpor Android button connect koro.

### Step G: login ar user isolation

Register-e hashed password, login-e random token, protected request-e token verify. Client theke arbitrary userId accept kore data select korbe na; token theke user resolve korbe.

### Step H: screen-e live data

Background thread-e HTTP request pathao. Callback-e main thread-e TextView/list update koro. UI thread-e network call korle screen freeze ba exception hote pare.

### Step I: reports, budget ar baki features

Core CRUD kaj korle summary → monthly report → chart → budget → search/export add koro. Prottek stage-e compile ar relevant behavior test kore commit koro.

## 6. Expense Save button-er puro journey

Dhoro Food category-te 250.50 taka expense add korle:

1. `MainActivity.entry()` dialog khole.
2. Input theke categoryId, amount, date, note read kore.
3. Blank/negative amount ba invalid date reject kore.
4. `json(...)` request body banay.
5. `request(...)` → `ApiClient.request()` background-e HTTP POST pathay.
6. URL: `/api/transactions`; header-e `Authorization: Bearer TOKEN`.
7. `WebConfig` token hash ar expiry check kore current userId attach kore.
8. `LedgerController.create()` validation ar category ownership check kore.
9. `JdbcTemplate.update()` parameterized INSERT execute kore.
10. MySQL DECIMAL field-e amount save hoy.
11. Backend success response pathay.
12. Android dialog close kore `load()` call kore; summary, transaction list ar budget panel refresh hoy.

Example JSON:

```json
{"categoryId": 12, "amount": 250.50, "date": "2026-10-15", "note": "Lunch"}
```

12 shudhu example; nijer account-er `/categories` response-er ID lagbe. Request-e type alada pathano hoy na: category-r type theke INCOME/EXPENSE determine hoy.

## 7. Database-er 5ta table bujho

| Table | Keno ache |
| --- | --- |
| users | name, unique email, password hash |
| sessions | hashed login token, user, expiry |
| categories | user-er income/expense category |
| transactions | user, category, amount, date, note |
| budgets | user/month/category scope-er spending limit |

`id` primary key. `user_id` foreign key. Transaction-er composite category/user foreign key onno user-er category use korte badha dey.

Budget-e `category_key=0` mane overall monthly budget. Positive value expense category-r ID. Ekhane category foreign key nei; API ownership/type validation ar deletion checks kore. Eta design-er ekta tradeoff, sir-ke spasto kore bolbe.

### SQL practice: nijer database-e SELECT

```sql
SELECT id, name, type FROM categories WHERE user_id = 1;

SELECT t.occurred_on, c.name, c.type, t.amount, t.note
FROM transactions t
JOIN categories c ON c.id = t.category_id
WHERE t.user_id = 1
ORDER BY t.occurred_on DESC;
```

1 nijer actual user ID diye replace korbe. Ei literal SQL learning-er jonno; API-r production query parameterized.

```sql
SELECT c.type, SUM(t.amount) AS total
FROM transactions t
JOIN categories c ON c.id = t.category_id
WHERE t.user_id = 1
GROUP BY c.type;
```

JOIN table combine kore. SUM total ber kore. GROUP BY income/expense alada kore.

## 8. Balance ar budget-er math

```text
Income  = 10000.00
Expense =   250.50
Balance =  9749.50
```

Java-te money-r jonno BigDecimal, MySQL-e DECIMAL(14,2). Float/double diye stored money calculate korle rounding error hote pare. Chart-er visual proportion double use kore; exact money text/backend calculation BigDecimal/DECIMAL use kore.

Dashboard-er balance **all-time**. Monthly report sudhu selected month. Duitake same scope na dhorle totals alada mone hobe.

```text
Budget 300, spent 250.50
Usage = 250.50 / 300 × 100 = 83.5%
Remaining = 49.50
```

80%-er niche OK, 80%-100%-er niche NEAR, 100% ba beshi OVER. Warning app-er bhitore; background notification na. Overall ar category budget alada limit, tai ek expense duitar spending-e count hote pare—eta intentional.

## 9. Nijer hate edit korar 5ta exercise

### Exercise 1: app-er title/text

`values/strings.xml`-e `daily_hisab` text edit koro. Matching Bangla text `values-bn/strings.xml`-e edit koro. Run diye duita language check koro.

### Exercise 2: dashboard color

`drawable/balance_card.xml`-er gradient color edit koro. `home.xml`-e TextView-r textColor ba textSize change koro. `dp` layout size/padding, `sp` text size.

### Exercise 3: click-er por choto message

Ekta practice button-er listener-e Toast show koro. Existing Save listener-er network logic delete korbe na.

```java
Toast.makeText(this, "Button kaj korche", Toast.LENGTH_SHORT).show();
```

### Exercise 4: notun default category

`AuthController.register()`-er expense category list-e `Education` add koro. Backend restart kore **notun test account** register koro. Existing user-er category list automatically update hobe na, karon oi code sudhu registration-e chole. English/Bangla display lagle Translations map-e pair add koro.

### Exercise 5: search ar budget behavior bojho

Ekta transaction-er note `Lunch test` dao. Search-e `lunch` diye dekho. `LedgerController.list()`-er q handling pore dekho LIKE query parameter kivabe banano hoy. Tarpor budget 300 → 200 kore warning-er difference dekho.

Prottek exercise-e age expected behavior lekho, change koro, run kore verify koro. Tarpor commit.

## 10. Notun database field add korar real process

Dhoro transaction-e payment method add korte chao:

1. Allowed values decide koro: CASH, BANK, MOBILE.
2. Fresh schema.sql update koro.
3. Existing database-er jonno **alada ALTER TABLE migration** lekho. CREATE TABLE IF NOT EXISTS existing table-e column add kore na.
4. Backend Entry request, validation, INSERT/UPDATE/SELECT ar response update koro.
5. Android form-e Spinner add koro ar JSON body-te field pathao.
6. List/report/export-e field lagbe kina decide koro.
7. Old entries-r default value decide koro.
8. Valid/invalid values, existing data ar user isolation test koro.

Eta future exercise; current project-e payment method implement kora hoyni.

## 11. Authentication sir-ke kivabe bujhabe

- Register-e BCrypt password hash save hoy; plain password save hoy na.
- Login-e hash-er sathe provided password match check hoy.
- Server random session token dey; app private SharedPreferences-e token rakhe.
- Database-e raw token-er bodole SHA-256 hash save hoy, sathe expiry.
- Prottek protected API call-e Authorization header jay.
- Interceptor token verify kore user resolve kore; query-te oi userId use hoy.
- Session 7 din por expire hoy. Logout current token remove kore.
- Password change-e current password verify kore shob session revoke hoy.

BCrypt password hashing ar SHA-256 token hashing-ke encryption bolbe na. Current private app preference storage hardware-backed encrypted credential vault na; production hardening future work.

## 12. Nijer hate test korbe

```powershell
.\gradlew.bat :backend:test
.\gradlew.bat :app:lintDebug :app:assembleDebug
```

Backend-er default tests disposable H2 database use kore. Real MySQL test-er setup `SETUP.md`-e ache. Test database alada hote hobe: integration test selected database-er rows delete kore.

Backend running ar emulator ready hole:

```powershell
.\gradlew.bat :app:connectedDebugAndroidTest
```

Manual checklist:

- Register/login/logout.
- Income 10000, expense 250.50 → balance 9749.50.
- Expense edit/delete korle balance update.
- Date/type/category filter ar note search.
- Monthly report-e correct month-er data.
- Budget 80% ar 100% warning.
- Used category delete blocked; rename history-te reflected.
- CSV save ar spreadsheet-e open.
- Language change-er por app restart-e preference thake.
- Name change ar password change-er por abar login.
- Second account-e first account-er hisab dekha jay na.

## 13. Error hole ki dekhbe

| Symptom | Ki check korbe |
| --- | --- |
| Add Configuration, Run disabled | Correct root folder, Gradle sync, app module |
| Emulator list ache, screen nei | Running Devices tool window |
| App install hoye bondho hoy | Logcat-e package filter, AndroidRuntime/FATAL EXCEPTION |
| Cannot reach server | backend running, URL, 8080 port, phone/PC connection |
| Backend Access denied | DB_USER, DB_PASSWORD, MySQL grants |
| Connection refused | MySQL server running? 3306 naki 3307? |
| Port 8080 already in use | Existing backend process cholche kina dekho; duplicate start korbe na |
| SDK location not found | local.properties-er sdk.dir ar SDK installation |
| Existing table-e new column nei | ALTER TABLE migration lagbe |
| GitHub red check | Actions → failed job → failed step-er log |

Guess kore repeatedly Run na diye prothom meaningful error line pore diagnosis korbe.

## 14. Git diye kajer progression rakhbe

```powershell
git status
git diff
git add app/src/main/res/layout/home.xml
git commit -m "ui: improve dashboard spacing"
git push origin main
```

Eta example; je file edit korbe oi file stage korbe. Password, .local, .env, local.properties ba database data commit korbe na.

Sir-ke existing history dekhate:

```powershell
git log --reverse --oneline
git show edb1097 --stat
```

Small commit-er title actual kajer sathe match korbe. Fake dates/progression banabe na. Commit history code-er evolution dekhay; code bujhte parar proof holo nijer hate choto change explain ar verify kora.

## 15. Sir-ke 3 minute-e bolar outline

**Problem:** daily income-expense track kora, balance ar monthly khoroch bujha.

**Architecture:** Android client Java/XML use kore. Spring Boot Java backend REST API provide kore. Backend JDBC diye MySQL-e query kore.

**Data:** users, sessions, categories, transactions, budgets—5ta table. Foreign key ar API user checks data relationship ar account isolation maintain kore.

**Demo:** register → income/expense add → balance → edit/filter → monthly chart → budget warning → language/export.

**Code walkthrough:** XML Save button → MainActivity entry method → ApiClient POST → WebConfig authentication → LedgerController INSERT → schema table → JSON response → screen refresh.

**Validation:** money positive, decimal scale checked, category owner verified. Server validation final decision ney; Android validation user-ke early feedback dey.

**Limitations:** offline sync nei, server connection lage, in-app budget alert, production HTTPS deployment/password reset/rate limiting baki.

Code bujhe nijer bhashay bolbe. AI assistance use kore thakle course-er rule onujayi seta acknowledge korbe; bujhe na bujhe shob code nijer hate lekha bole claim korbe na.

## 16. Viva practice

| Sir-er possible question | Short answer |
| --- | --- |
| Java kothay use hoyeche? | Android behavior/network ar Spring Boot backend-e |
| XML keno? | Android View screen-er layout/resource define korte |
| MySQL kothay? | Backend-er sathe connected server database-e |
| App direct database-e jay? | API-r madhyome; DB credentials server-e |
| POST ar GET difference? | POST data/create action-er jonno, GET data read-er jonno |
| PUT ar DELETE? | Existing resource update ar remove |
| Primary/foreign key? | Record identify ar related table-er integrity maintain |
| SQL injection kivabe avoid? | JdbcTemplate query parameter placeholder use kore |
| Onno user-er record access? | Token theke userId resolve, ownership-scoped query |
| Balance kothay calculate? | SQL SUM/CASE-er totals, Java BigDecimal subtraction |
| Transaction type duibar store na keno? | Category theke determine kore inconsistency komano |
| Used category delete hoy na keno? | Transaction/budget history protect korte |
| UI freeze avoid kivabe? | Executor-e network, main thread-e UI callback |
| Chart library? | Native Android Canvas custom View |
| Language support? | Localized XML strings, dynamic text map, saved locale preference |
| CSV-te formula safety? | Quoted fields ar formula-like text neutralization |
| GitHub Actions keno? | Push-er por automatic MySQL tests, lint, APK build |

## 17. Nijer learning milestone

1. Ekta screen-er text/color nijer hate change ar explain.
2. Ekta click listener input read kore ki kore explain.
3. Ekta request body ar response read kore bujha.
4. Ekta SELECT/JOIN query nijer hate lekha.
5. Ekta existing API endpoint-er full flow trace kora.
6. Nijer practice feature add kore test ar commit.

Ei milestone-gulo complete korle Codex charao project maintain korar foundation hobe. Prothome `auth.xml`, `MainActivity.auth()`, `ApiClient.request()`, `AuthController.login()` ei char jayga pore ekta login request trace koro.
