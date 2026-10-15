package com.dailyhisab.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
public class LedgerController {
  private final JdbcTemplate db;

  public LedgerController(JdbcTemplate db) {
    this.db = db;
  }

  public record Category(
      @NotBlank @Size(max = 60) String name,
      @Pattern(regexp = "INCOME|EXPENSE") @NotNull String type) {}

  public record Entry(
      @NotNull @Positive Long categoryId,
      @NotNull @DecimalMin("0.01") @Digits(integer = 12, fraction = 2) BigDecimal amount,
      @NotNull LocalDate date,
      @NotNull @Size(max = 250) String note) {}

  public record Transaction(
      long id,
      long categoryId,
      String category,
      String type,
      BigDecimal amount,
      LocalDate date,
      String note) {}

  @GetMapping("/categories")
  public List<Map<String, Object>> categories(@RequestAttribute long userId) {
    return db.queryForList(
        "SELECT id,name,type FROM categories WHERE user_id=? ORDER BY type,name", userId);
  }

  @PostMapping("/categories")
  @ResponseStatus(HttpStatus.CREATED)
  public Map<String, String> category(
      @RequestAttribute long userId, @Valid @RequestBody Category c) {
    db.update(
        "INSERT INTO categories(user_id,name,type) VALUES (?,?,?)",
        userId,
        c.name().trim(),
        c.type());
    return Map.of("message", "Category added");
  }

  @GetMapping("/transactions")
  public List<Transaction> list(
      @RequestAttribute long userId,
      @RequestParam(required = false) LocalDate from,
      @RequestParam(required = false) LocalDate to,
      @RequestParam(required = false) String type,
      @RequestParam(required = false) Long categoryId,
      @RequestParam(required = false) @Size(max = 120) String q) {
    range(from, to);
    validType(type);
    StringBuilder sql =
        new StringBuilder(
            "SELECT t.id,t.category_id AS categoryId,c.name AS"
                + " category,c.type,t.amount,t.occurred_on AS date,t.note FROM transactions t JOIN"
                + " categories c ON c.id=t.category_id WHERE t.user_id=?");
    List<Object> args = new ArrayList<>();
    args.add(userId);
    if (from != null) {
      sql.append(" AND t.occurred_on>=?");
      args.add(from);
    }
    if (to != null) {
      sql.append(" AND t.occurred_on<=?");
      args.add(to);
    }
    if (type != null) {
      sql.append(" AND c.type=?");
      args.add(type);
    }
    if (categoryId != null) {
      sql.append(" AND c.id=?");
      args.add(categoryId);
    }
    if (q != null && !q.isBlank()) {
      if(q.length()>120) throw new IllegalArgumentException();
      String term="%"+q.trim().toLowerCase(Locale.ROOT).replace("!","!!").replace("%","!%").replace("_","!_")+"%";
      sql.append(" AND (LOWER(t.note) LIKE ? ESCAPE '!' OR LOWER(c.name) LIKE ? ESCAPE '!')");
      args.add(term); args.add(term);
    }
    sql.append(" ORDER BY t.occurred_on DESC,t.id DESC LIMIT 500");
    return db.query(
        sql.toString(),
        (rs, index) ->
            new Transaction(
                rs.getLong("id"),
                rs.getLong("categoryId"),
                rs.getString("category"),
                rs.getString("type"),
                rs.getBigDecimal("amount"),
                rs.getDate("date").toLocalDate(),
                rs.getString("note")),
        args.toArray());
  }

  @PostMapping("/transactions")
  @ResponseStatus(HttpStatus.CREATED)
  @Transactional
  public Map<String, String> create(@RequestAttribute long userId, @Valid @RequestBody Entry e) {
    ownCategory(userId, e.categoryId());
    db.update(
        "INSERT INTO transactions(user_id,category_id,amount,occurred_on,note) VALUES (?,?,?,?,?)",
        userId,
        e.categoryId(),
        e.amount(),
        e.date(),
        e.note().trim());
    return Map.of("message", "Transaction added");
  }

  @PutMapping("/transactions/{id}")
  @Transactional
  public Map<String, String> update(
      @RequestAttribute long userId, @PathVariable long id, @Valid @RequestBody Entry e) {
    ownCategory(userId, e.categoryId());
    if (db.update(
            "UPDATE transactions SET category_id=?,amount=?,occurred_on=?,note=? WHERE id=? AND"
                + " user_id=?",
            e.categoryId(),
            e.amount(),
            e.date(),
            e.note().trim(),
            id,
            userId)
        == 0) missing();
    return Map.of("message", "Transaction updated");
  }

  @DeleteMapping("/transactions/{id}")
  public Map<String, String> delete(@RequestAttribute long userId, @PathVariable long id) {
    if (db.update("DELETE FROM transactions WHERE id=? AND user_id=?", id, userId) == 0) missing();
    return Map.of("message", "Transaction deleted");
  }

  @GetMapping("/summary")
  public Map<String, Object> summary(
      @RequestAttribute long userId,
      @RequestParam(required = false) LocalDate from,
      @RequestParam(required = false) LocalDate to) {
    range(from, to);
    String filter = " WHERE t.user_id=?";
    List<Object> args = new ArrayList<>();
    args.add(userId);
    if (from != null) {
      filter += " AND t.occurred_on>=?";
      args.add(from);
    }
    if (to != null) {
      filter += " AND t.occurred_on<=?";
      args.add(to);
    }
    var totals =
        db.queryForMap(
            "SELECT COALESCE(SUM(CASE WHEN c.type='INCOME' THEN t.amount ELSE 0 END),0) AS"
                + " income,COALESCE(SUM(CASE WHEN c.type='EXPENSE' THEN t.amount ELSE 0 END),0) AS"
                + " expense FROM transactions t JOIN categories c ON c.id=t.category_id"
                + filter,
            args.toArray());
    BigDecimal income = new BigDecimal(totals.get("income").toString()),
        expense = new BigDecimal(totals.get("expense").toString());
    return Map.of("income", income, "expense", expense, "balance", income.subtract(expense));
  }

  @GetMapping("/reports/monthly")
  public Map<String, Object> report(@RequestAttribute long userId, @RequestParam String month) {
    YearMonth m = YearMonth.parse(month);
    var breakdown =
        db.queryForList(
            "SELECT c.name AS category,c.type,SUM(t.amount) AS total FROM transactions t JOIN"
                + " categories c ON c.id=t.category_id WHERE t.user_id=? AND t.occurred_on>=? AND"
                + " t.occurred_on<=? GROUP BY c.id,c.name,c.type ORDER BY c.type,total DESC",
            userId,
            m.atDay(1),
            m.atEndOfMonth());
    return Map.of(
        "month",
        month,
        "summary",
        summary(userId, m.atDay(1), m.atEndOfMonth()),
        "categories",
        breakdown);
  }

  private void ownCategory(long user, long category) {
    if (db.queryForObject(
            "SELECT COUNT(*) FROM categories WHERE id=? AND user_id=?",
            Integer.class,
            category,
            user)
        == 0) missing();
  }

  private void range(LocalDate from, LocalDate to) {
    if (from != null && to != null && from.isAfter(to)) throw new IllegalArgumentException();
  }

  private void validType(String type) {
    if (type != null && !List.of("INCOME", "EXPENSE").contains(type))
      throw new IllegalArgumentException();
  }

  private void missing() {
    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Entry or category not found");
  }
}
