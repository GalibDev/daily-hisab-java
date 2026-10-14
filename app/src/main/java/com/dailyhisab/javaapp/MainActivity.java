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

  @Override
  public void onCreate(Bundle state) {
    super.onCreate(state);
    prefs = getSharedPreferences("session", MODE_PRIVATE);
    base = prefs.getString("server", "http://10.0.2.2:8080");
    token = prefs.getString("token", "");
    if (token.isEmpty()) auth();
    else home();
  }

  private void screen(int layout) {
    generation++;
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
            text(R.id.authError, "Session expired. Please log in.");
            return;
          }
          cb.done(data, error, status);
        });
  }

  private void auth() {
    screen(R.layout.auth);
    findViewById(R.id.name).setVisibility(register ? View.VISIBLE : View.GONE);
    ((Button) findViewById(R.id.submit)).setText(register ? "Create account" : "Log in");
    ((Button) findViewById(R.id.switchAuth))
        .setText(register ? "Already registered? Log in" : "New here? Create an account");
    findViewById(R.id.switchAuth)
        .setOnClickListener(
            v -> {
              register = !register;
              auth();
            });
    findViewById(R.id.server).setOnClickListener(v -> server());
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
              text(R.id.authError, "Connecting…");
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
            "API server URL",
            base,
            InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
    label(
        form,
        "Emulator: http://10.0.2.2:8080\n"
            + "Phone: use your computer's LAN IP. Release builds require HTTPS.");
    AlertDialog dialog =
        new AlertDialog.Builder(this)
            .setTitle("Server settings")
            .setView(form)
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel", null)
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
                        field.setError("Use http(s)://host:port without a path");
                      }
                    }));
    dialog.show();
  }

  private void home() {
    screen(R.layout.home);
    text(R.id.greeting, "Hello, " + prefs.getString("name", "friend"));
    findViewById(R.id.add).setEnabled(false);
    findViewById(R.id.categories).setEnabled(false);
    findViewById(R.id.add).setOnClickListener(v -> entry(null));
    findViewById(R.id.categories).setOnClickListener(v -> category());
    findViewById(R.id.report).setOnClickListener(v -> monthly());
    findViewById(R.id.filter).setOnClickListener(v -> filters());
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
  }

  private void load() {
    text(R.id.homeError, "Loading…");
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
                    "Income  "
                        + money(s.optString("income"))
                        + "\nExpenses  "
                        + money(s.optString("expense")));
                loadEntries();
              });
        });
  }

  private void loadEntries() {
    String query = "?";
    if (!from.isEmpty()) query += "from=" + encode(from) + "&";
    if (!to.isEmpty()) query += "to=" + encode(to) + "&";
    if (!filterType.isEmpty()) query += "type=" + filterType + "&";
    if (filterCategory != 0) query += "categoryId=" + filterCategory;
    text(
        R.id.filterLabel,
        (from.isEmpty() ? "Any start" : from)
            + " → "
            + (to.isEmpty() ? "Any end" : to)
            + " · "
            + (filterType.isEmpty() ? "All types" : filterType)
            + (filterCategory == 0 ? " · All categories" : " · Selected category"));
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
            label(list, "No transactions yet. Add one or change your filters.");
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
                    row.optString("category")
                        + "   "
                        + (income ? "+" : "−")
                        + money(row.optString("amount")));
            heading.setTextSize(18);
            heading.setTextColor(Color.parseColor(income ? "#137C66" : "#B34F3B"));
            label(
                card,
                row.optString("date")
                    + (row.optString("note").isEmpty() ? "" : "\n" + row.optString("note")));
            label(card, "Tap to edit or delete").setTextSize(12);
            card.setContentDescription(
                "Transaction " + heading.getText() + ". Tap to edit or delete");
            card.setOnClickListener(
                v ->
                    new AlertDialog.Builder(this)
                        .setTitle("Transaction")
                        .setItems(
                            new String[] {"Edit", "Delete"},
                            (d, index) -> {
                              if (index == 0) entry(row);
                              else
                                new AlertDialog.Builder(this)
                                    .setTitle("Delete transaction?")
                                    .setMessage("This removes the entry from your balance.")
                                    .setNegativeButton("Cancel", null)
                                    .setPositiveButton(
                                        "Delete",
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
                "Showing the latest 500 matches. Narrow the date filter to see older entries.");
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
              names.add(c.optString("name"));
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
            "Amount (BDT)",
            existing == null ? "" : existing.optString("amount"),
            InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
    EditText date =
        input(
            form,
            "Date: YYYY-MM-DD",
            existing == null ? LocalDate.now().toString() : existing.optString("date"),
            InputType.TYPE_CLASS_DATETIME);
    datePicker(date);
    EditText note =
        input(
            form,
            "Note (optional)",
            existing == null ? "" : existing.optString("note"),
            InputType.TYPE_CLASS_TEXT);
    note.setFilters(
        new android.text.InputFilter[] {new android.text.InputFilter.LengthFilter(250)});
    TextView error = label(form, "");
    AlertDialog dialog =
        new AlertDialog.Builder(this)
            .setTitle(existing == null ? "Add transaction" : "Edit transaction")
            .setView(form)
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel", null)
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
                              if (problem != null) error.setText(problem);
                              else {
                                dialog.dismiss();
                                load();
                              }
                            });
                      } catch (Exception ex) {
                        error.setText(
                            "Enter a positive amount with up to 2 decimals and a valid date.");
                      }
                    }));
    dialog.show();
  }

  private void category() {
    LinearLayout form = form();
    EditText name = input(form, "Category name", "", InputType.TYPE_CLASS_TEXT);
    Spinner type = spinner(form, new String[] {"EXPENSE", "INCOME"});
    TextView error = label(form, "");
    AlertDialog dialog =
        new AlertDialog.Builder(this)
            .setTitle("Add category")
            .setView(form)
            .setPositiveButton("Add", null)
            .setNegativeButton("Cancel", null)
            .create();
    dialog.setOnShowListener(
        d ->
            dialog
                .getButton(-1)
                .setOnClickListener(
                    v -> {
                      String text = name.getText().toString().trim();
                      if (text.isEmpty() || text.length() > 60) {
                        error.setText("Name must contain 1–60 characters.");
                        return;
                      }
                      v.setEnabled(false);
                      request(
                          "POST",
                          "/categories",
                          json("name", text, "type", type.getSelectedItem()),
                          (data, problem, code) -> {
                            v.setEnabled(true);
                            if (problem != null) error.setText(problem);
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
        input(form, "From: YYYY-MM-DD (optional)", from, InputType.TYPE_CLASS_DATETIME);
    datePicker(start);
    EditText end = input(form, "To: YYYY-MM-DD (optional)", to, InputType.TYPE_CLASS_DATETIME);
    datePicker(end);
    Spinner type = spinner(form, new String[] {"All types", "INCOME", "EXPENSE"});
    type.setSelection(filterType.equals("INCOME") ? 1 : filterType.equals("EXPENSE") ? 2 : 0);
    List<String> names = new ArrayList<>(Arrays.asList("All categories"));
    List<Long> ids = new ArrayList<>(Arrays.asList(0L));
    for (int i = 0; i < categories.length(); i++) {
      JSONObject c = categories.optJSONObject(i);
      names.add(c.optString("name") + " · " + c.optString("type"));
      ids.add(c.optLong("id"));
    }
    Spinner category = spinner(form, names.toArray(new String[0]));
    category.setSelection(Math.max(0, ids.indexOf(filterCategory)));
    TextView error = label(form, "");
    AlertDialog dialog =
        new AlertDialog.Builder(this)
            .setTitle("Filter transactions")
            .setView(form)
            .setPositiveButton("Apply", null)
            .setNeutralButton(
                "Reset",
                (d, w) -> {
                  from = "";
                  to = "";
                  filterType = "";
                  filterCategory = 0;
                  load();
                })
            .setNegativeButton("Cancel", null)
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
                        error.setText("Use YYYY-MM-DD. Start must be before end.");
                      }
                    }));
    dialog.show();
  }

  private void monthly() {
    LinearLayout form = form();
    EditText month =
        input(form, "Month: YYYY-MM", YearMonth.now().toString(), InputType.TYPE_CLASS_DATETIME);
    TextView result = label(form, "");
    AlertDialog dialog =
        new AlertDialog.Builder(this)
            .setTitle("Monthly report")
            .setView(form)
            .setPositiveButton("View report", null)
            .setNegativeButton("Close", null)
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
                        result.setText("Use YYYY-MM, for example 2026-10.");
                        return;
                      }
                      v.setEnabled(false);
                      result.setText("Loading…");
                      request(
                          "GET",
                          "/reports/monthly?month=" + encode(month.getText().toString()),
                          null,
                          (data, error, status) -> {
                            v.setEnabled(true);
                            if (error != null) {
                              result.setText(error);
                              return;
                            }
                            JSONObject report = (JSONObject) data,
                                summary = report.optJSONObject("summary");
                            StringBuilder text =
                                new StringBuilder(
                                    "Income: "
                                        + money(summary.optString("income"))
                                        + "\nExpenses: "
                                        + money(summary.optString("expense"))
                                        + "\nNet: "
                                        + money(summary.optString("balance"))
                                        + "\n\nCategory totals\n");
                            JSONArray rows = report.optJSONArray("categories");
                            if (rows.length() == 0) text.append("No transactions for this month.");
                            for (int i = 0; i < rows.length(); i++) {
                              JSONObject c = rows.optJSONObject(i);
                              text.append(c.optString("category"))
                                  .append(" · ")
                                  .append(c.optString("type"))
                                  .append(": ")
                                  .append(money(c.optString("total")))
                                  .append("\n");
                            }
                            result.setText(text);
                          });
                    }));
    dialog.show();
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

  private LinearLayout form() {
    LinearLayout form = new LinearLayout(this);
    form.setOrientation(LinearLayout.VERTICAL);
    form.setPadding(dp(20), dp(12), dp(20), dp(12));
    return form;
  }

  private EditText input(LinearLayout parent, String hint, String value, int type) {
    EditText field = new EditText(this);
    field.setHint(hint);
    field.setText(value);
    field.setInputType(type);
    field.setSingleLine(true);
    parent.addView(field, new LinearLayout.LayoutParams(-1, dp(56)));
    return field;
  }

  private Spinner spinner(LinearLayout parent, String[] options) {
    Spinner s = new Spinner(this);
    s.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, options));
    parent.addView(s, new LinearLayout.LayoutParams(-1, dp(52)));
    return s;
  }

  private TextView label(LinearLayout parent, String text) {
    TextView view = new TextView(this);
    view.setText(text);
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
    ((TextView) findViewById(id)).setText(text);
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
    super.onDestroy();
  }
}
