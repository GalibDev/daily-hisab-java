# Version 1 verification

## Phase 2 verification — 15 October 2026

- Eight API integration tests passed against H2 and real MySQL 8.4.11.
- Android debug APK and executable backend JAR built successfully.
- Android lint: 0 errors; remaining warnings concern localization implementation and SDK guidance.
- Extended emulator test passed: registration, expense entry, monthly report/chart, 80% budget warning, literal note search and persistent Bangla language selection.
- API tests also cover category rename/delete protection, overall/category budget scoping, full export beyond 500 rows, CSV formula protection, profile changes and password/session revocation.
- Offline sync, background budget notifications, hosted HTTPS deployment and password reset remain outside phase 2.

The phase 2 emulator test covers more flows than the original version 1 test below; it does not claim every UI path or Android device has been tested.

---

Verified locally on 15 October 2026:

| Check | Result |
| --- | --- |
| Java backend compilation / executable JAR | Passed |
| Android debug APK build | Passed |
| Android lint | 0 errors; 14 warnings (localization / SDK upgrade guidance) |
| Four API integration tests with H2 MySQL mode | Passed |
| Same four integration tests with real MySQL 8.4.11 | Passed |
| Android emulator UI smoke test | Passed on API 37 preview emulator |

The integration tests cover exact monetary totals, editing/deleting, all transaction filters,
monthly boundaries, user isolation, category ownership, password hashing, login/logout,
session expiry, invalid amounts/dates, custom categories and duplicate rejection.

The UI smoke test covers registration, adding an expense, displaying its balance and opening its monthly report.
It does not claim automated UI coverage of every screen, or device testing on every supported Android release.

The CI workflow is included; its hosted result is separate from these local results.

## Read the development sequence

```shell
git log --reverse --oneline
```

The history progresses through scope → schema/build → authentication → ledger API → Android layouts/networking → connected screens → tests → documentation → UI refinements → CI → emulator verification.
