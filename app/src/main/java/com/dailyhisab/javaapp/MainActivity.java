package com.dailyhisab.javaapp;

import android.app.*;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.*;
import android.widget.*;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.text.NumberFormat;
import java.time.*;
import java.util.*;
import org.json.*;

public class MainActivity extends Activity {
  private final ApiClient api = new ApiClient();
  private SharedPreferences prefs;
  private String token = "", base, from = "", to = "", filterType = "";
  private long filterCategory = 0;
  private JSONArray categories = new JSONArray();
  private boolean register = false;
  private int generation = 0;
  private String searchQuery = "";
  private boolean bangla;
  private final List<Dialog> openDialogs = new ArrayList<>();

  private AlertDialog.Builder builder() {
    return new AlertDialog.Builder(this) {
      @Override
      public AlertDialog create() {
        AlertDialog dialog = super.create();
        openDialogs.add(dialog);
        return dialog;
      }
    };
  }

  private final java.util.concurrent.ExecutorService files =
      java.util.concurrent.Executors.newSingleThreadExecutor();

  @Override
  protected void attachBaseContext(android.content.Context baseContext) {
    String language =
        baseContext.getSharedPreferences("session", MODE_PRIVATE).getString("language", "en");
    android.content.res.Configuration config =
        new android.content.res.Configuration(baseContext.getResources().getConfiguration());
    config.setLocale(new Locale(language));
    super.attachBaseContext(baseContext.createConfigurationContext(config));
  }

  @Override
  public void onCreate(Bundle state) {
    super.onCreate(state);
    prefs = getSharedPreferences("session", MODE_PRIVATE);
    bangla = prefs.getString("language", "en").equals("bn");
    base = prefs.getString("server", "http://10.0.2.2:8080");
    token = prefs.getString("token", "");
    if (token.isEmpty()) auth();
    else home();
  }

  private void screen(int layout) {
    generation++;
    for (Dialog dialog : new ArrayList<>(openDialogs)) dialog.dismiss();
    openDialogs.clear();
    setContentView(layout);
    View root = findViewById(R.id.root);
    int left = root.getPaddingLeft(),
        top = root.getPaddingTop(),
        right = root.getPaddingRight(),
        bottom = root.getPaddingBottom();
    if (android.os.Build.VERSION.SDK_INT >= 30)
      root.setOnApplyWindowInsetsListener(
          (v, insets) -> {
            var bars = insets.getInsets(WindowInsets.Type.systemBars() | WindowInsets.Type.ime());
            v.setPadding(
                left + bars.left, top + bars.top, right + bars.right, bottom + bars.bottom);
            return insets;
          });
    if (android.os.Build.VERSION.SDK_INT < 30)
      root.setOnApplyWindowInsetsListener(
          (v, insets) -> {
            v.setPadding(
                left + insets.getSystemWindowInsetLeft(),
                top + insets.getSystemWindowInsetTop(),
                right + insets.getSystemWindowInsetRight(),
                bottom + insets.getSystemWindowInsetBottom());
            return insets;
          });
    root.requestApplyInsets();
  }

  private void request(String method, String path, JSONObject body, ApiClient.Callback cb) {
    int current = generation;
    api.request(
        base,
        token,
        method,
        path,
        body,
        (data, error, status) -> {
          if (isDestroyed() || current != generation) return;
          if (status == 401 && !token.isEmpty()) {
            clearSession();
            auth();
            text(R.id.authError, tr("Session expired. Please log in."));
            return;
          }
          cb.done(data, error, status);
        });
  }

  private void auth() {
    screen(R.layout.auth);
    findViewById(R.id.name).setVisibility(register ? View.VISIBLE : View.GONE);
    ((Button) findViewById(R.id.submit)).setText(register ? tr("Create account") : tr("Log in"));
    ((Button) findViewById(R.id.switchAuth))
        .setText(register ? tr("Already registered? Log in") : tr("New here? Create an account"));
    findViewById(R.id.switchAuth)
        .setOnClickListener(
            v -> {
              register = !register;
              auth();
            });
    findViewById(R.id.server).setOnClickListener(v -> server());
    findViewById(R.id.language).setOnClickListener(v -> language());
    findViewById(R.id.submit)
        .setOnClickListener(
            v -> {
              String email = value(R.id.email),
                  password = ((EditText) findViewById(R.id.password)).getText().toString(),
                  name = value(R.id.name);
              if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
                  || password.length() < 8
                  || (register && name.isEmpty())) {
                text(
                    R.id.authError,
                    "Enter a valid email, password of 8+ characters, and your name when"
                        + " registering.");
                return;
              }
              JSONObject body = json("email", email, "password", password, "name", name);
              v.setEnabled(false);
              text(R.id.authError, tr("Connecting…"));
              request(
                  "POST",
                  register ? "/auth/register" : "/auth/login",
                  body,
                  (data, error, status) -> {
                    v.setEnabled(true);
                    if (error != null) {
                      text(R.id.authError, error);
                      return;
                    }
                    JSONObject response = (JSONObject) data;
                    token = response.optString("token");
                    prefs
                        .edit()
                        .putString("token", token)
                        .putString("name", response.optString("name"))
                        .apply();
                    home();
                  });
            });
  }

  private void server() {
    LinearLayout form = form();
    EditText field =
        input(
            form,
            tr("API server URL"),
            base,
            InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
    label(
        form,
        "Emulator: http://10.0.2.2:8080\n"
            + "Phone: use your computer's LAN IP. Release builds require HTTPS.");
    AlertDialog dialog =
        builder()
            .setTitle(tr("Server settings"))
            .setView(scroll(form))
            .setPositiveButton(tr("Save"), null)
            .setNegativeButton(tr("Cancel"), null)
            .create();
    dialog.setOnShowListener(
        x ->
            dialog
                .getButton(-1)
                .setOnClickListener(
                    v -> {
                      String url = field.getText().toString().trim().replaceAll("/+$", "");
                      try {
                        URI uri = URI.create(url);
                        if (!Arrays.asList("http", "https").contains(uri.getScheme())
                            || uri.getHost() == null
                            || uri.getUserInfo() != null
                            || uri.getQuery() != null
                            || uri.getFragment() != null
                            || !uri.getPath().isEmpty()) throw new IllegalArgumentException();
                        base = url;
                        prefs.edit().putString("server", base).apply();
                        dialog.dismiss();
                      } catch (Exception ex) {
                        field.setError(tr("Use http(s)://host:port without a path"));
                      }
                    }));
    dialog.show();
  }

  private void home() {
    screen(R.layout.home);
    text(R.id.greeting, tr("Hello, ") + prefs.getString("name", "friend"));
    findViewById(R.id.add).setEnabled(false);
    findViewById(R.id.categories).setEnabled(false);
    findViewById(R.id.add).setOnClickListener(v -> entry(null));
    findViewById(R.id.categories).setOnClickListener(v -> manageCategories());
    findViewById(R.id.report).setOnClickListener(v -> monthly());
    findViewById(R.id.filter).setOnClickListener(v -> filters());
    findViewById(R.id.navHome)
        .setOnClickListener(v -> ((ScrollView) findViewById(R.id.homeScroll)).smoothScrollTo(0, 0));
    findViewById(R.id.navLedger)
        .setOnClickListener(
            v ->
                ((ScrollView) findViewById(R.id.homeScroll))
                    .smoothScrollTo(0, findViewById(R.id.transactionHeading).getTop()));
    findViewById(R.id.navBudget).setOnClickListener(v -> budgets());
    findViewById(R.id.navSettings).setOnClickListener(v -> settings());
    ((EditText) findViewById(R.id.search)).setText(searchQuery);
    findViewById(R.id.searchButton).setOnClickListener(v -> search());
    ((EditText) findViewById(R.id.search))
        .setOnEditorActionListener(
            (v, action, event) -> {
              if (action == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                search();
                return true;
              }
              return false;
            });
    findViewById(R.id.logout)
        .setOnClickListener(
            v -> {
              v.setEnabled(false);
              request(
                  "POST",
                  "/auth/logout",
                  null,
                  (data, error, status) -> {
                    v.setEnabled(true);
                    if (error != null) {
                      text(R.id.homeError, error);
                      return;
                    }
                    clearSession();
                    auth();
                  });
            });
    load();
  }

  private void clearSession() {
    token = "";
    prefs.edit().remove("token").remove("name").apply();
    categories = new JSONArray();
    from = "";
    to = "";
    filterType = "";
    filterCategory = 0;
    searchQuery = "";
  }

  private void load() {
    text(R.id.homeError, tr("Loading…"));
    request(
        "GET",
        "/categories",
        null,
        (data, error, status) -> {
          if (error != null) {
            text(R.id.homeError, error);
            return;
          }
          categories = (JSONArray) data;
          findViewById(R.id.add).setEnabled(true);
          findViewById(R.id.categories).setEnabled(true);
          request(
              "GET",
              "/summary",
              null,
              (summary, problem, code) -> {
                if (problem != null) {
                  text(R.id.homeError, problem);
                  return;
                }
                JSONObject s = (JSONObject) summary;
                text(R.id.balance, money(s.optString("balance")));
                text(
                    R.id.totals,
                    tr("Income  ")
                        + money(s.optString("income"))
                        + "\nExpenses  "
                        + money(s.optString("expense")));
                loadEntries();
                loadBudgetPanel();
              });
        });
  }

  private void loadEntries() {
    String query = "?";
    if (!from.isEmpty()) query += "from=" + encode(from) + "&";
    if (!to.isEmpty()) query += "to=" + encode(to) + "&";
    if (!filterType.isEmpty()) query += "type=" + filterType + "&";
    if (filterCategory != 0) query += "categoryId=" + filterCategory;
    if (!searchQuery.isEmpty()) query += "&q=" + encode(searchQuery);
    text(
        R.id.filterLabel,
        (from.isEmpty() ? tr("Any start") : from)
            + " → "
            + (to.isEmpty() ? tr("Any end") : to)
            + " · "
            + (filterType.isEmpty() ? tr("All types") : tr(filterType))
            + (filterCategory == 0 ? tr(" · All categories") : tr(" · Selected category")));
    request(
        "GET",
        "/transactions" + query,
        null,
        (data, error, status) -> {
          if (error != null) {
            text(R.id.homeError, error);
            return;
          }
          text(R.id.homeError, "");
          LinearLayout list = findViewById(R.id.entries);
          list.removeAllViews();
          JSONArray rows = (JSONArray) data;
          if (rows.length() == 0)
            label(list, tr("No transactions yet. Add one or change your filters."));
          for (int i = 0; i < rows.length(); i++) {
            JSONObject row = rows.optJSONObject(i);
            boolean income = row.optString("type").equals("INCOME");
            LinearLayout card = form();
            card.setBackgroundResource(R.drawable.card);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
            lp.topMargin = dp(10);
            card.setLayoutParams(lp);
            TextView heading =
                label(
                    card,
                    tr(row.optString("category"))
                        + "   "
                        + (income ? "+" : "−")
                        + money(row.optString("amount")));
            heading.setTextSize(18);
            heading.setTextColor(Color.parseColor(income ? "#137C66" : "#B34F3B"));
            label(
                card,
                row.optString("date")
                    + (row.optString("note").isEmpty() ? "" : "\n" + row.optString("note")));
            label(card, tr("Tap to edit or delete")).setTextSize(12);
            card.setContentDescription(
                tr("Transaction ") + heading.getText() + tr(". Tap to edit or delete"));
            card.setOnClickListener(
                v ->
                    builder()
                        .setTitle(tr("Transaction"))
                        .setItems(
                            new String[] {tr("Edit"), tr("Delete")},
                            (d, index) -> {
                              if (index == 0) entry(row);
                              else
                                builder()
                                    .setTitle(tr("Delete transaction?"))
                                    .setMessage(tr("This removes the entry from your balance."))
                                    .setNegativeButton(tr("Cancel"), null)
                                    .setPositiveButton(
                                        tr("Delete"),
                                        (confirm, which) ->
                                            request(
                                                "DELETE",
                                                "/transactions/" + row.optLong("id"),
                                                null,
                                                (result, problem, code) -> {
                                                  if (problem != null)
                                                    text(R.id.homeError, problem);
                                                  else load();
                                                }))
                                    .show();
                            })
                        .show());
            list.addView(card);
          }
          if (rows.length() == 500)
            label(
                list,
                tr("Showing the latest 500 matches. Narrow the date filter to see older entries."));
        });
  }

  private void entry(JSONObject existing) {
    LinearLayout form = form();
    Spinner type = spinner(form, new String[] {"EXPENSE", "INCOME"});
    Spinner cat = new Spinner(this);
    form.addView(cat);
    List<Long> ids = new ArrayList<>();
    Runnable updateCategories =
        () -> {
          ids.clear();
          List<String> names = new ArrayList<>();
          for (int i = 0; i < categories.length(); i++) {
            JSONObject c = categories.optJSONObject(i);
            if (c.optString("type").equals(type.getSelectedItem().toString())) {
              ids.add(c.optLong("id"));
              names.add(tr(c.optString("name")));
            }
          }
          cat.setAdapter(
              new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, names));
          if (existing != null) {
            int index = ids.indexOf(existing.optLong("categoryId"));
            if (index >= 0) cat.setSelection(index);
          }
        };
    if (existing != null && existing.optString("type").equals("INCOME")) type.setSelection(1);
    type.setOnItemSelectedListener(
        new AdapterView.OnItemSelectedListener() {
          public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
            updateCategories.run();
          }

          public void onNothingSelected(AdapterView<?> p) {}
        });
    updateCategories.run();
    EditText amount =
        input(
            form,
            tr("Amount (BDT)"),
            existing == null ? "" : existing.optString("amount"),
            InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
    EditText date =
        input(
            form,
            tr("Date: YYYY-MM-DD"),
            existing == null ? LocalDate.now().toString() : existing.optString("date"),
            InputType.TYPE_CLASS_DATETIME);
    datePicker(date);
    EditText note =
        input(
            form,
            tr("Note (optional)"),
            existing == null ? "" : existing.optString("note"),
            InputType.TYPE_CLASS_TEXT);
    note.setFilters(
        new android.text.InputFilter[] {new android.text.InputFilter.LengthFilter(250)});
    TextView error = label(form, "");
    AlertDialog dialog =
        builder()
            .setTitle(existing == null ? tr("Add transaction") : tr("Edit transaction"))
            .setView(scroll(form))
            .setPositiveButton(tr("Save"), null)
            .setNegativeButton(tr("Cancel"), null)
            .create();
    dialog.setOnShowListener(
        d ->
            dialog
                .getButton(-1)
                .setOnClickListener(
                    v -> {
                      try {
                        BigDecimal value = new BigDecimal(amount.getText().toString());
                        LocalDate.parse(date.getText().toString());
                        if (value.signum() <= 0
                            || value.scale() > 2
                            || value.compareTo(new BigDecimal("999999999999.99")) > 0
                            || ids.isEmpty()) throw new IllegalArgumentException();
                        JSONObject body =
                            json(
                                "categoryId",
                                ids.get(cat.getSelectedItemPosition()),
                                "amount",
                                value,
                                "date",
                                date.getText().toString(),
                                "note",
                                note.getText().toString());
                        v.setEnabled(false);
                        request(
                            existing == null ? "POST" : "PUT",
                            "/transactions"
                                + (existing == null ? "" : "/" + existing.optLong("id")),
                            body,
                            (data, problem, code) -> {
                              v.setEnabled(true);
                              if (problem != null) error.setText(tr(problem));
                              else {
                                dialog.dismiss();
                                load();
                              }
                            });
                      } catch (Exception ex) {
                        error.setText(
                            tr("Enter a positive amount with up to 2 decimals and a valid date."));
                      }
                    }));
    dialog.show();
  }

  private void category() {
    LinearLayout form = form();
    EditText name = input(form, tr("Category name"), "", InputType.TYPE_CLASS_TEXT);
    Spinner type = spinner(form, new String[] {"EXPENSE", "INCOME"});
    TextView error = label(form, "");
    AlertDialog dialog =
        builder()
            .setTitle(tr("Add category"))
            .setView(scroll(form))
            .setPositiveButton(tr("Add"), null)
            .setNegativeButton(tr("Cancel"), null)
            .create();
    dialog.setOnShowListener(
        d ->
            dialog
                .getButton(-1)
                .setOnClickListener(
                    v -> {
                      String text = name.getText().toString().trim();
                      if (text.isEmpty() || text.length() > 60) {
                        error.setText(tr("Name must contain 1–60 characters."));
                        return;
                      }
                      v.setEnabled(false);
                      request(
                          "POST",
                          "/categories",
                          json("name", text, "type", type.getSelectedItem()),
                          (data, problem, code) -> {
                            v.setEnabled(true);
                            if (problem != null) error.setText(tr(problem));
                            else {
                              dialog.dismiss();
                              load();
                            }
                          });
                    }));
    dialog.show();
  }

  private void filters() {
    LinearLayout form = form();
    EditText start =
        input(form, tr("From: YYYY-MM-DD (optional)"), from, InputType.TYPE_CLASS_DATETIME);
    datePicker(start);
    EditText end = input(form, tr("To: YYYY-MM-DD (optional)"), to, InputType.TYPE_CLASS_DATETIME);
    datePicker(end);
    Spinner type = spinner(form, new String[] {tr("All types"), "INCOME", "EXPENSE"});
    type.setSelection(filterType.equals("INCOME") ? 1 : filterType.equals("EXPENSE") ? 2 : 0);
    List<String> names = new ArrayList<>(Arrays.asList(tr("All categories")));
    List<Long> ids = new ArrayList<>(Arrays.asList(0L));
    for (int i = 0; i < categories.length(); i++) {
      JSONObject c = categories.optJSONObject(i);
      names.add(tr(c.optString("name")) + " · " + tr(c.optString("type")));
      ids.add(c.optLong("id"));
    }
    Spinner category = spinner(form, names.toArray(new String[0]));
    category.setSelection(Math.max(0, ids.indexOf(filterCategory)));
    TextView error = label(form, "");
    AlertDialog dialog =
        builder()
            .setTitle(tr("Filter transactions"))
            .setView(scroll(form))
            .setPositiveButton(tr("Apply"), null)
            .setNeutralButton(
                tr("Reset"),
                (d, w) -> {
                  from = "";
                  to = "";
                  filterType = "";
                  filterCategory = 0;
                  load();
                })
            .setNegativeButton(tr("Cancel"), null)
            .create();
    dialog.setOnShowListener(
        d ->
            dialog
                .getButton(-1)
                .setOnClickListener(
                    v -> {
                      try {
                        String a = start.getText().toString().trim(),
                            b = end.getText().toString().trim();
                        LocalDate f = a.isEmpty() ? null : LocalDate.parse(a),
                            t = b.isEmpty() ? null : LocalDate.parse(b);
                        if (f != null && t != null && f.isAfter(t))
                          throw new IllegalArgumentException();
                        from = a;
                        to = b;
                        filterType =
                            type.getSelectedItemPosition() == 0
                                ? ""
                                : type.getSelectedItem().toString();
                        filterCategory = ids.get(category.getSelectedItemPosition());
                        dialog.dismiss();
                        load();
                      } catch (Exception ex) {
                        error.setText(tr("Use YYYY-MM-DD. Start must be before end."));
                      }
                    }));
    dialog.show();
  }

  private void monthly() {
    LinearLayout form = form();
    EditText month =
        input(
            form, tr("Month: YYYY-MM"), YearMonth.now().toString(), InputType.TYPE_CLASS_DATETIME);
    TextView result = label(form, "");
    ReportChart chart = new ReportChart(this, bangla);
    form.addView(chart, new LinearLayout.LayoutParams(-1, dp(320)));
    AlertDialog dialog =
        builder()
            .setTitle(tr("Monthly report"))
            .setView(scroll(form))
            .setPositiveButton(tr("View report"), null)
            .setNegativeButton(tr("Close"), null)
            .create();
    dialog.setOnShowListener(
        d ->
            dialog
                .getButton(-1)
                .setOnClickListener(
                    v -> {
                      try {
                        YearMonth.parse(month.getText().toString());
                      } catch (Exception ex) {
                        result.setText(tr("Use YYYY-MM, for example 2026-10."));
                        return;
                      }
                      v.setEnabled(false);
                      result.setText(tr("Loading…"));
                      request(
                          "GET",
                          "/reports/monthly?month=" + encode(month.getText().toString()),
                          null,
                          (data, error, status) -> {
                            v.setEnabled(true);
                            if (error != null) {
                              result.setText(tr(error));
                              return;
                            }
                            JSONObject report = (JSONObject) data,
                                summary = report.optJSONObject("summary");
                            chart.setReport(report);
                            StringBuilder text =
                                new StringBuilder(
                                    tr("Income: ")
                                        + money(summary.optString("income"))
                                        + tr("\nExpenses: ")
                                        + money(summary.optString("expense"))
                                        + tr("\nNet: ")
                                        + money(summary.optString("balance"))
                                        + tr("\n\nCategory totals\n"));
                            JSONArray rows = report.optJSONArray("categories");
                            if (rows.length() == 0)
                              text.append(tr("No transactions for this month."));
                            for (int i = 0; i < rows.length(); i++) {
                              JSONObject c = rows.optJSONObject(i);
                              text.append(tr(c.optString("category")))
                                  .append(" · ")
                                  .append(tr(c.optString("type")))
                                  .append(": ")
                                  .append(money(c.optString("total")))
                                  .append("\n");
                            }
                            result.setText(text);
                          });
                    }));
    dialog.show();
  }

  private String tr(String text) {
    return bangla ? Translations.bn(text) : text;
  }

  private void language() {
    builder()
        .setTitle(tr("Language"))
        .setSingleChoiceItems(
            new String[] {"English", "বাংলা"},
            bangla ? 1 : 0,
            (d, index) -> {
              prefs.edit().putString("language", index == 1 ? "bn" : "en").apply();
              d.dismiss();
              recreate();
            })
        .setNegativeButton(tr("Cancel"), null)
        .show();
  }

  private void search() {
    searchQuery = value(R.id.search);
    android.view.inputmethod.InputMethodManager keyboard =
        (android.view.inputmethod.InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
    keyboard.hideSoftInputFromWindow(findViewById(R.id.search).getWindowToken(), 0);
    loadEntries();
  }

  private Button action(LinearLayout parent, String title, Runnable run) {
    Button button = new Button(this);
    button.setText(tr(title));
    button.setAllCaps(false);
    parent.addView(button, new LinearLayout.LayoutParams(-1, dp(52)));
    button.setOnClickListener(v -> run.run());
    return button;
  }

  private void settings() {
    LinearLayout form = form();
    action(form, tr("Profile"), () -> profile());
    action(form, tr("Change password"), () -> password());
    action(form, tr("Language"), () -> language());
    action(form, tr("Export full ledger (CSV)"), () -> export());
    action(form, tr("Server settings"), () -> server());
    builder()
        .setTitle(tr("Settings"))
        .setView(scroll(form))
        .setNegativeButton(tr("Close"), null)
        .show();
  }

  private void profile() {
    request(
        "GET",
        "/profile",
        null,
        (data, error, status) -> {
          if (error != null) {
            Toast.makeText(this, tr(error), Toast.LENGTH_LONG).show();
            return;
          }
          JSONObject user = (JSONObject) data;
          LinearLayout form = form();
          label(form, user.optString("email"));
          EditText name =
              input(form, tr("Your name"), user.optString("name"), InputType.TYPE_CLASS_TEXT);
          TextView problem = label(form, "");
          AlertDialog dialog =
              builder()
                  .setTitle(tr("Profile"))
                  .setView(scroll(form))
                  .setPositiveButton(tr("Save"), null)
                  .setNegativeButton(tr("Cancel"), null)
                  .create();
          dialog.setOnShowListener(
              d ->
                  dialog
                      .getButton(-1)
                      .setOnClickListener(
                          v -> {
                            String value = name.getText().toString().trim();
                            if (value.isEmpty() || value.length() > 80) {
                              problem.setText(tr("Name must contain 1–80 characters."));
                              return;
                            }
                            v.setEnabled(false);
                            request(
                                "PUT",
                                "/profile",
                                json("name", value),
                                (result, issue, code) -> {
                                  v.setEnabled(true);
                                  if (issue != null) problem.setText(tr(issue));
                                  else {
                                    prefs.edit().putString("name", value).apply();
                                    text(R.id.greeting, tr("Hello, ") + value);
                                    dialog.dismiss();
                                  }
                                });
                          }));
          dialog.show();
        });
  }

  private void password() {
    LinearLayout form = form();
    label(form, tr("Changing your password logs out all devices."));
    EditText current =
        input(
            form,
            tr("Current password"),
            "",
            InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
    EditText next =
        input(
            form,
            tr("New password (8+ characters)"),
            "",
            InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
    EditText confirm =
        input(
            form,
            tr("Confirm new password"),
            "",
            InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
    TextView problem = label(form, "");
    AlertDialog dialog =
        builder()
            .setTitle(tr("Change password"))
            .setView(scroll(form))
            .setPositiveButton(tr("Save"), null)
            .setNegativeButton(tr("Cancel"), null)
            .create();
    dialog.setOnShowListener(
        d ->
            dialog
                .getButton(-1)
                .setOnClickListener(
                    v -> {
                      String pass = next.getText().toString();
                      if (pass.length() < 8
                          || pass.length() > 64
                          || !pass.equals(confirm.getText().toString())) {
                        problem.setText(tr("Use matching passwords of 8–64 characters."));
                        return;
                      }
                      v.setEnabled(false);
                      request(
                          "POST",
                          "/profile/password",
                          json(
                              "currentPassword", current.getText().toString(), "newPassword", pass),
                          (data, error, status) -> {
                            v.setEnabled(true);
                            if (error != null) problem.setText(tr(error));
                            else {
                              dialog.dismiss();
                              clearSession();
                              auth();
                              text(R.id.authError, tr("Password changed. Please log in again"));
                            }
                          });
                    }));
    dialog.show();
  }

  private void manageCategories() {
    LinearLayout form = form(), list = form();
    TextView error = label(form, "");
    AlertDialog dialog =
        builder()
            .setTitle(tr("Categories"))
            .setView(scroll(form))
            .setNegativeButton(tr("Close"), null)
            .create();
    action(
        form,
        tr("Add category"),
        () -> {
          dialog.dismiss();
          category();
        });
    form.addView(list);
    for (int i = 0; i < categories.length(); i++) {
      JSONObject c = categories.optJSONObject(i);
      action(
          list,
          tr(c.optString("name")) + " · " + tr(c.optString("type")),
          () ->
              builder()
                  .setTitle(tr(c.optString("name")))
                  .setItems(
                      new String[] {tr("Rename"), tr("Delete")},
                      (d, index) -> {
                        if (index == 0) {
                          dialog.dismiss();
                          renameCategory(c);
                        } else
                          builder()
                              .setTitle(tr("Delete unused category?"))
                              .setMessage(
                                  tr(
                                      "Used categories cannot be deleted. Transactions and budgets"
                                          + " stay safe."))
                              .setNegativeButton(tr("Cancel"), null)
                              .setPositiveButton(
                                  tr("Delete"),
                                  (confirm, which) ->
                                      request(
                                          "DELETE",
                                          "/categories/" + c.optLong("id"),
                                          null,
                                          (result, problem, code) -> {
                                            if (problem != null) error.setText(tr(problem));
                                            else {
                                              if (filterCategory == c.optLong("id"))
                                                filterCategory = 0;
                                              dialog.dismiss();
                                              load();
                                            }
                                          }))
                              .show();
                      })
                  .show());
    }
    dialog.show();
  }

  private void renameCategory(JSONObject category) {
    LinearLayout form = form();
    EditText name =
        input(form, tr("Category name"), category.optString("name"), InputType.TYPE_CLASS_TEXT);
    TextView problem = label(form, "");
    AlertDialog dialog =
        builder()
            .setTitle(tr("Rename category"))
            .setView(scroll(form))
            .setPositiveButton(tr("Save"), null)
            .setNegativeButton(tr("Cancel"), null)
            .create();
    dialog.setOnShowListener(
        d ->
            dialog
                .getButton(-1)
                .setOnClickListener(
                    v -> {
                      String value = name.getText().toString().trim();
                      if (value.isEmpty() || value.length() > 60) {
                        problem.setText(tr("Name must contain 1–60 characters."));
                        return;
                      }
                      v.setEnabled(false);
                      request(
                          "PUT",
                          "/categories/" + category.optLong("id"),
                          json("name", value),
                          (result, error, code) -> {
                            v.setEnabled(true);
                            if (error != null) problem.setText(tr(error));
                            else {
                              dialog.dismiss();
                              load();
                            }
                          });
                    }));
    dialog.show();
  }

  private void loadBudgetPanel() {
    if (token.isEmpty() || isDestroyed() || isFinishing() || findViewById(R.id.budgetPanel) == null)
      return;
    request(
        "GET",
        "/budgets?month=" + YearMonth.now(),
        null,
        (data, error, status) -> {
          LinearLayout panel = findViewById(R.id.budgetPanel);
          panel.removeAllViews();
          if (error != null) {
            label(panel, tr(error));
            action(panel, tr("Retry"), () -> loadBudgetPanel());
            return;
          }
          JSONArray rows = (JSONArray) data;
          if (rows.length() == 0) {
            action(panel, tr("Set a monthly budget"), () -> budgets());
            return;
          }
          label(panel, tr("This month's budgets"));
          for (int i = 0; i < rows.length(); i++) budgetCard(panel, rows.optJSONObject(i), null);
        });
  }

  private void budgetCard(LinearLayout parent, JSONObject b, Runnable click) {
    LinearLayout card = form();
    card.setBackgroundResource(R.drawable.card);
    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
    lp.topMargin = dp(8);
    card.setLayoutParams(lp);
    label(card, tr(b.optString("category")) + " · " + money(b.optString("amount")));
    label(
            card,
            tr("Spent: ")
                + money(b.optString("spent"))
                + "  ·  "
                + tr("Remaining: ")
                + money(b.optString("remaining")))
        .setTextSize(13);
    ProgressBar progress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
    progress.setMax(100);
    double limit = b.optDouble("amount");
    progress.setProgress((int) Math.min(100, limit == 0 ? 0 : 100 * b.optDouble("spent") / limit));
    card.addView(progress, new LinearLayout.LayoutParams(-1, dp(12)));
    if (!b.optString("status").equals("OK")) {
      TextView warning =
          label(
              card,
              b.optString("status").equals("OVER")
                  ? tr("Budget limit reached")
                  : tr("80% of budget used"));
      warning.setTextColor(Color.parseColor("#B34F3B"));
      warning.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
    }
    if (click != null) {
      label(card, tr("Tap to edit or delete")).setTextSize(12);
      card.setOnClickListener(v -> click.run());
    }
    parent.addView(card);
  }

  private void budgets() {
    LinearLayout form = form();
    EditText month =
        input(
            form, tr("Month: YYYY-MM"), YearMonth.now().toString(), InputType.TYPE_CLASS_DATETIME);
    TextView error = label(form, "");
    LinearLayout list = form();
    AlertDialog dialog =
        builder()
            .setTitle(tr("Budgets"))
            .setView(scroll(form))
            .setNegativeButton(tr("Close"), null)
            .create();
    Runnable[] refresh = new Runnable[1];
    refresh[0] =
        () -> {
          String m = month.getText().toString();
          try {
            YearMonth.parse(m);
          } catch (Exception ex) {
            error.setText(tr("Use YYYY-MM, for example 2026-10."));
            return;
          }
          error.setText(tr("Loading…"));
          request(
              "GET",
              "/budgets?month=" + encode(m),
              null,
              (data, problem, status) -> {
                if (problem != null) {
                  error.setText(tr(problem));
                  return;
                }
                error.setText("");
                list.removeAllViews();
                JSONArray rows = (JSONArray) data;
                if (rows.length() == 0) label(list, tr("No budgets for this month."));
                for (int i = 0; i < rows.length(); i++) {
                  JSONObject b = rows.optJSONObject(i);
                  budgetCard(
                      list,
                      b,
                      () ->
                          builder()
                              .setTitle(tr("Budget"))
                              .setItems(
                                  new String[] {tr("Edit"), tr("Delete")},
                                  (d, index) -> {
                                    if (index == 0) editBudget(m, b, refresh[0]);
                                    else
                                      builder()
                                          .setTitle(tr("Delete budget?"))
                                          .setNegativeButton(tr("Cancel"), null)
                                          .setPositiveButton(
                                              tr("Delete"),
                                              (confirm, w) ->
                                                  request(
                                                      "DELETE",
                                                      "/budgets/" + b.optLong("id"),
                                                      null,
                                                      (result, issue, code) -> {
                                                        if (issue != null) error.setText(tr(issue));
                                                        else refresh[0].run();
                                                      }))
                                          .show();
                                  })
                              .show());
                }
              });
        };
    action(form, tr("View budgets"), refresh[0]);
    action(
        form,
        tr("Add budget"),
        () -> {
          try {
            YearMonth.parse(month.getText().toString());
            editBudget(month.getText().toString(), null, refresh[0]);
          } catch (Exception e) {
            error.setText(tr("Use YYYY-MM, for example 2026-10."));
          }
        });
    form.addView(list);
    dialog.setOnDismissListener(d -> loadBudgetPanel());
    dialog.show();
    refresh[0].run();
  }

  private void editBudget(String month, JSONObject existing, Runnable refresh) {
    LinearLayout form = form();
    label(form, month);
    List<String> names = new ArrayList<>(Arrays.asList(tr("Monthly budget")));
    List<Long> ids = new ArrayList<>(Arrays.asList(0L));
    for (int i = 0; i < categories.length(); i++) {
      JSONObject c = categories.optJSONObject(i);
      if (c.optString("type").equals("EXPENSE")) {
        names.add(tr(c.optString("name")));
        ids.add(c.optLong("id"));
      }
    }
    Spinner cat = spinner(form, names.toArray(new String[0]));
    if (existing != null) {
      cat.setSelection(Math.max(0, ids.indexOf(existing.optLong("categoryId"))));
      cat.setEnabled(false);
    }
    EditText amount =
        input(
            form,
            tr("Budget amount (BDT)"),
            existing == null ? "" : existing.optString("amount"),
            InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
    TextView error = label(form, "");
    AlertDialog dialog =
        builder()
            .setTitle(tr("Save budget"))
            .setView(scroll(form))
            .setPositiveButton(tr("Save"), null)
            .setNegativeButton(tr("Cancel"), null)
            .create();
    dialog.setOnShowListener(
        d ->
            dialog
                .getButton(-1)
                .setOnClickListener(
                    v -> {
                      try {
                        BigDecimal value = new BigDecimal(amount.getText().toString());
                        if (value.signum() <= 0
                            || value.scale() > 2
                            || value.compareTo(new BigDecimal("999999999999.99")) > 0)
                          throw new IllegalArgumentException();
                        v.setEnabled(false);
                        request(
                            "PUT",
                            "/budgets",
                            json(
                                "month",
                                month,
                                "categoryId",
                                ids.get(cat.getSelectedItemPosition()),
                                "amount",
                                value),
                            (result, problem, status) -> {
                              v.setEnabled(true);
                              if (problem != null) error.setText(tr(problem));
                              else {
                                dialog.dismiss();
                                refresh.run();
                                loadBudgetPanel();
                              }
                            });
                      } catch (Exception ex) {
                        error.setText(tr("Enter a positive amount with up to 2 decimals."));
                      }
                    }));
    dialog.show();
  }

  private void export() {
    request(
        "GET",
        "/export",
        null,
        (data, error, status) -> {
          if (error != null) {
            Toast.makeText(this, tr(error), Toast.LENGTH_LONG).show();
            return;
          }
          String csv = ((JSONObject) data).optString("csv");
          files.execute(
              () -> {
                try {
                  java.io.File target = new java.io.File(getCacheDir(), "ledger-export.csv");
                  try (java.io.FileOutputStream out = new java.io.FileOutputStream(target)) {
                    out.write(("\uFEFF" + csv).getBytes(java.nio.charset.StandardCharsets.UTF_8));
                  }
                  runOnUiThread(
                      () -> {
                        if (!isDestroyed()) {
                          android.content.Intent intent =
                              new android.content.Intent(
                                      android.content.Intent.ACTION_CREATE_DOCUMENT)
                                  .setType("text/csv")
                                  .addCategory(android.content.Intent.CATEGORY_OPENABLE)
                                  .putExtra(
                                      android.content.Intent.EXTRA_TITLE,
                                      "daily-hisab-" + LocalDate.now() + ".csv");
                          startActivityForResult(intent, 42);
                        }
                      });
                } catch (Exception ex) {
                  runOnUiThread(
                      () ->
                          Toast.makeText(
                                  this, tr("Export failed. Please retry."), Toast.LENGTH_LONG)
                              .show());
                }
              });
        });
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == 42 && resultCode == RESULT_OK && data != null && data.getData() != null) {
      android.net.Uri uri = data.getData();
      files.execute(
          () -> {
            try (java.io.InputStream in =
                    new java.io.FileInputStream(
                        new java.io.File(getCacheDir(), "ledger-export.csv"));
                java.io.OutputStream out = getContentResolver().openOutputStream(uri)) {
              if (out == null) throw new java.io.IOException();
              byte[] buffer = new byte[8192];
              int n;
              while ((n = in.read(buffer)) != -1) out.write(buffer, 0, n);
              runOnUiThread(() -> Toast.makeText(this, tr("CSV saved"), Toast.LENGTH_LONG).show());
            } catch (Exception ex) {
              runOnUiThread(
                  () ->
                      Toast.makeText(this, tr("Export failed. Please retry."), Toast.LENGTH_LONG)
                          .show());
            }
          });
    }
  }

  private void datePicker(EditText field) {
    field.setOnClickListener(
        v -> {
          LocalDate date;
          try {
            date = LocalDate.parse(field.getText().toString());
          } catch (Exception ex) {
            date = LocalDate.now();
          }
          new DatePickerDialog(
                  this,
                  (picker, y, m, d) -> field.setText(LocalDate.of(y, m + 1, d).toString()),
                  date.getYear(),
                  date.getMonthValue() - 1,
                  date.getDayOfMonth())
              .show();
        });
  }

  private ScrollView scroll(LinearLayout form) {
    ScrollView scroll = new ScrollView(this);
    scroll.addView(form);
    return scroll;
  }

  private LinearLayout form() {
    LinearLayout form = new LinearLayout(this);
    form.setOrientation(LinearLayout.VERTICAL);
    form.setPadding(dp(20), dp(12), dp(20), dp(12));
    return form;
  }

  private EditText input(LinearLayout parent, String hint, String value, int type) {
    EditText field = new EditText(this);
    field.setHint(tr(hint));
    field.setText(value);
    field.setInputType(type);
    field.setSingleLine(true);
    parent.addView(field, new LinearLayout.LayoutParams(-1, dp(56)));
    return field;
  }

  private Spinner spinner(LinearLayout parent, String[] options) {
    Spinner s = new Spinner(this);
    s.setAdapter(
        new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, options) {
          @Override
          public View getView(int position, View convert, ViewGroup parent) {
            TextView v = (TextView) super.getView(position, convert, parent);
            v.setText(tr(getItem(position)));
            return v;
          }

          @Override
          public View getDropDownView(int position, View convert, ViewGroup parent) {
            TextView v = (TextView) super.getDropDownView(position, convert, parent);
            v.setText(tr(getItem(position)));
            return v;
          }
        });
    parent.addView(s, new LinearLayout.LayoutParams(-1, dp(52)));
    return s;
  }

  private TextView label(LinearLayout parent, String text) {
    TextView view = new TextView(this);
    view.setText(tr(text));
    view.setTextColor(Color.parseColor("#405B50"));
    view.setTextSize(16);
    view.setPadding(0, dp(8), 0, dp(8));
    parent.addView(view);
    return view;
  }

  private int dp(int value) {
    return Math.round(value * getResources().getDisplayMetrics().density);
  }

  private String value(int id) {
    return ((EditText) findViewById(id)).getText().toString().trim();
  }

  private void text(int id, String text) {
    ((TextView) findViewById(id)).setText(tr(text));
  }

  private String money(String amount) {
    try {
      NumberFormat format = NumberFormat.getNumberInstance(Locale.US);
      format.setMinimumFractionDigits(2);
      format.setMaximumFractionDigits(2);
      return "৳ " + format.format(new BigDecimal(amount));
    } catch (Exception e) {
      return "৳ 0.00";
    }
  }

  private static String encode(String text) {
    try {
      return URLEncoder.encode(text, "UTF-8");
    } catch (java.io.UnsupportedEncodingException e) {
      throw new IllegalStateException(e);
    }
  }

  private static JSONObject json(Object... pairs) {
    JSONObject object = new JSONObject();
    try {
      for (int i = 0; i < pairs.length; i += 2) object.put((String) pairs[i], pairs[i + 1]);
    } catch (JSONException e) {
      throw new IllegalArgumentException(e);
    }
    return object;
  }

  @Override
  protected void onDestroy() {
    generation++;
    api.close();
    files.shutdown();
    super.onDestroy();
  }
}
