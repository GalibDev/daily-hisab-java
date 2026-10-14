# Version 1 verification

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
