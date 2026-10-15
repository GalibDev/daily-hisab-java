package com.dailyhisab.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
public class PlanningController {
  private final JdbcTemplate db;

  public PlanningController(JdbcTemplate db) {
    this.db = db;
  }

  public record Rename(@NotBlank @Size(max = 60) String name) {}

  public record Budget(
      @NotBlank String month,
      @NotNull @PositiveOrZero Long categoryId,
      @NotNull @DecimalMin("0.01") @Digits(integer = 12, fraction = 2) BigDecimal amount) {}

  @PutMapping("/categories/{id}")
  @Transactional
  public Map<String, String> rename(
      @RequestAttribute long userId, @PathVariable long id, @Valid @RequestBody Rename r) {
    category(userId, id);
    db.update("UPDATE categories SET name=? WHERE id=? AND user_id=?", r.name().trim(), id, userId);
    return Map.of("message", "Category renamed");
  }

  @DeleteMapping("/categories/{id}")
  @Transactional
  public Map<String, String> deleteCategory(@RequestAttribute long userId, @PathVariable long id) {
    category(userId, id);
    int used =
        db.queryForObject(
                "SELECT COUNT(*) FROM transactions WHERE user_id=? AND category_id=?",
                Integer.class,
                userId,
                id)
            + db.queryForObject(
                "SELECT COUNT(*) FROM budgets WHERE user_id=? AND category_key=?",
                Integer.class,
                userId,
                id);
    if (used > 0)
      throw new ResponseStatusException(
          HttpStatus.CONFLICT,
          "Category is used by transactions or budgets. Rename it or remove those references"
              + " first");
    db.update("DELETE FROM categories WHERE id=? AND user_id=?", id, userId);
    return Map.of("message", "Category deleted");
  }

  @PutMapping("/budgets")
  @Transactional
  public Map<String, String> saveBudget(
      @RequestAttribute long userId, @Valid @RequestBody Budget b) {
    YearMonth.parse(b.month());
    db.queryForObject("SELECT id FROM users WHERE id=? FOR UPDATE", Long.class, userId);
    if (b.categoryId() != 0) {
      String type = category(userId, b.categoryId());
      if (!type.equals("EXPENSE"))
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST, "Budgets require an expense category");
    }
    int changed =
        db.update(
            "UPDATE budgets SET amount=? WHERE user_id=? AND month_key=? AND category_key=?",
            b.amount(),
            userId,
            b.month(),
            b.categoryId());
    if (changed == 0)
      db.update(
          "INSERT INTO budgets(user_id,month_key,category_key,amount) VALUES (?,?,?,?)",
          userId,
          b.month(),
          b.categoryId(),
          b.amount());
    return Map.of("message", "Budget saved");
  }

  @GetMapping("/budgets")
  public List<Map<String, Object>> budgets(
      @RequestAttribute long userId, @RequestParam String month) {
    YearMonth m = YearMonth.parse(month);
    var rows =
        db.queryForList(
            "SELECT b.id,b.category_key,b.amount,c.name FROM budgets b LEFT JOIN categories c ON"
                + " c.id=b.category_key AND c.user_id=b.user_id WHERE b.user_id=? AND b.month_key=?"
                + " ORDER BY b.category_key",
            userId,
            month);
    List<Map<String, Object>> result = new ArrayList<>();
    for (var row : rows) {
      long category = ((Number) row.get("category_key")).longValue();
      BigDecimal limit = new BigDecimal(row.get("amount").toString());
      BigDecimal spent =
          db.queryForObject(
              "SELECT COALESCE(SUM(t.amount),0) FROM transactions t JOIN categories c ON"
                  + " c.id=t.category_id WHERE t.user_id=? AND c.type='EXPENSE' AND"
                  + " t.occurred_on>=? AND t.occurred_on<=? AND (?=0 OR t.category_id=?)",
              BigDecimal.class,
              userId,
              m.atDay(1),
              m.atEndOfMonth(),
              category,
              category);
      String status =
          spent.compareTo(limit) >= 0
              ? "OVER"
              : spent.compareTo(limit.multiply(new BigDecimal("0.8"))) >= 0 ? "NEAR" : "OK";
      result.add(
          Map.of(
              "id",
              row.get("id"),
              "categoryId",
              category,
              "category",
              category == 0 ? "Monthly budget" : row.get("name"),
              "amount",
              limit,
              "spent",
              spent,
              "remaining",
              limit.subtract(spent),
              "status",
              status));
    }
    return result;
  }

  @DeleteMapping("/budgets/{id}")
  public Map<String, String> deleteBudget(@RequestAttribute long userId, @PathVariable long id) {
    if (db.update("DELETE FROM budgets WHERE id=? AND user_id=?", id, userId) == 0) missing();
    return Map.of("message", "Budget deleted");
  }

  private String category(long user, long id) {
    var types =
        db.queryForList(
            "SELECT type FROM categories WHERE user_id=? AND id=? FOR UPDATE",
            String.class,
            user,
            id);
    if (types.isEmpty()) missing();
    return types.get(0);
  }

  private void missing() {
    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found");
  }
}
