# Phase 2

Android version 2.0 adds practical personal-finance tools while keeping Java, XML, JDBC and MySQL.

## User flows

1. Home shows the all-time balance, recent transactions and current-month budgets.
2. Bottom navigation opens Home, Transactions, Budgets and Settings. Monthly report is available above the transaction list.
3. Language is available from login and Settings. The choice persists across app restarts. API enum values and saved notes stay unchanged.
4. Reports show monthly income/expense bars, an expense-share donut and exact textual totals. More than five expense categories are grouped as remaining categories in the chart, while the text retains every category.
5. Budgets support an overall monthly limit and separate expense-category limits. At 80% an in-app warning appears; at 100% the limit warning appears. There are no background notifications.
6. Search matches literal text in notes or category names, together with existing filters. Clearing the search restores unsearched results.
7. Rename categories without changing their type/history. Delete only unused categories; transactions and budgets block deletion.
8. Export fetches the complete ledger and asks where to save a UTF-8 CSV. The app does not need storage permissions. CSV text is quoted, and formula-like text is neutralized for spreadsheet use.
9. Settings supports name updates and password changes. Changing a password requires the current password and signs out every device.

## Existing installations

Restart the updated backend before using the new app. The added `CREATE TABLE IF NOT EXISTS budgets` statement creates the new table without clearing previous accounts or transactions. Install the updated APK over version 1 to retain app preferences.

## University demo

- Add a Food expense of ৳250.50.
- Set an overall month budget of ৳300; show the 80% warning.
- Rename Food to Meals and search for Meals; show that the original amount remains.
- Open the month report and explain chart totals versus the all-time dashboard balance.
- Change language to Bangla, then back to English.
- Export CSV and open it in Excel.
- Update your name, then change your password and show the new login requirement.

Tests cover both feature behavior and cross-user isolation. See `VERIFICATION.md` for tested environments.
