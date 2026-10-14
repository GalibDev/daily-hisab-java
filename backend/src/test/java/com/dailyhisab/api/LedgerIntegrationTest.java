package com.dailyhisab.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(
    properties = {
      "spring.datasource.url=${TEST_DB_URL:jdbc:h2:mem:hisab;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE}",
      "spring.datasource.username=${TEST_DB_USER:sa}",
      "spring.datasource.password=${TEST_DB_PASSWORD:}"
    })
@AutoConfigureMockMvc
class LedgerIntegrationTest {
  @Autowired MockMvc mvc;
  @Autowired ObjectMapper json;
  @Autowired JdbcTemplate db;
  String alice, bob;
  long income, expense;

  @BeforeEach
  void setup() throws Exception {
    db.update("DELETE FROM transactions");
    db.update("DELETE FROM sessions");
    db.update("DELETE FROM categories");
    db.update("DELETE FROM users");
    alice = register("alice@test.com");
    bob = register("bob@test.com");
    income = categoryId(alice, "Salary");
    expense = categoryId(alice, "Food");
  }

  String register(String email) throws Exception {
    String response =
        mvc.perform(
                post("/api/auth/register")
                    .contentType("application/json")
                    .content(
                        "{\"name\":\"Student\",\"email\":\""
                            + email
                            + "\",\"password\":\"password123\"}"))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
    return json.readTree(response).get("token").asText();
  }

  long categoryId(String token, String name) throws Exception {
    JsonNode rows =
        json.readTree(
            mvc.perform(get("/api/categories").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString());
    for (JsonNode row : rows)
      if (row.get("name").asText().equals(name)) return row.get("id").asLong();
    throw new AssertionError("Category missing");
  }

  String entry(long category, String amount, String date) {
    return "{\"categoryId\":"
        + category
        + ",\"amount\":"
        + amount
        + ",\"date\":\""
        + date
        + "\",\"note\":\"Test\"}";
  }

  void add(long category, String amount, String date) throws Exception {
    mvc.perform(
            post("/api/transactions")
                .header("Authorization", "Bearer " + alice)
                .contentType("application/json")
                .content(entry(category, amount, date)))
        .andExpect(status().isCreated());
  }

  @Test
  void exactTotalsFiltersMonthlyAndCrud() throws Exception {
    add(income, "1000.10", "2026-10-01");
    add(expense, "100.05", "2026-10-02");
    add(expense, "10.00", "2026-09-30");
    mvc.perform(get("/api/summary").header("Authorization", "Bearer " + alice))
        .andExpect(jsonPath("$.balance").value(890.05));
    mvc.perform(
            get("/api/reports/monthly")
                .param("month", "2026-10")
                .header("Authorization", "Bearer " + alice))
        .andExpect(jsonPath("$.summary.balance").value(900.05))
        .andExpect(jsonPath("$.categories.length()").value(2));
    String rows =
        mvc.perform(
                get("/api/transactions")
                    .param("from", "2026-10-01")
                    .param("to", "2026-10-31")
                    .param("type", "EXPENSE")
                    .param("categoryId", Long.toString(expense))
                    .header("Authorization", "Bearer " + alice))
            .andExpect(jsonPath("$.length()").value(1))
            .andReturn()
            .getResponse()
            .getContentAsString();
    long id = json.readTree(rows).get(0).get("id").asLong();
    assertEquals(expense, json.readTree(rows).get(0).get("categoryId").asLong());
    mvc.perform(
            put("/api/transactions/" + id)
                .header("Authorization", "Bearer " + alice)
                .contentType("application/json")
                .content(entry(expense, "200.00", "2026-10-02")))
        .andExpect(status().isOk());
    mvc.perform(delete("/api/transactions/" + id).header("Authorization", "Bearer " + alice))
        .andExpect(status().isOk());
    mvc.perform(get("/api/summary").header("Authorization", "Bearer " + alice))
        .andExpect(jsonPath("$.balance").value(990.10));
  }

  @Test
  void otherUserCannotReadEditDeleteOrUseCategories() throws Exception {
    add(income, "100", "2026-10-01");
    long id = db.queryForObject("SELECT id FROM transactions", Long.class);
    mvc.perform(get("/api/transactions").header("Authorization", "Bearer " + bob))
        .andExpect(jsonPath("$.length()").value(0));
    mvc.perform(get("/api/summary").header("Authorization", "Bearer " + bob))
        .andExpect(jsonPath("$.balance").value(0));
    mvc.perform(delete("/api/transactions/" + id).header("Authorization", "Bearer " + bob))
        .andExpect(status().isNotFound());
    mvc.perform(
            put("/api/transactions/" + id)
                .header("Authorization", "Bearer " + bob)
                .contentType("application/json")
                .content(entry(categoryId(bob, "Food"), "1", "2026-10-01")))
        .andExpect(status().isNotFound());
    mvc.perform(
            post("/api/transactions")
                .header("Authorization", "Bearer " + bob)
                .contentType("application/json")
                .content(entry(income, "1", "2026-10-01")))
        .andExpect(status().isNotFound());
  }

  @Test
  void authLifecycleAndPasswordStorage() throws Exception {
    assertTrue(
        db.queryForObject(
                "SELECT password_hash FROM users WHERE email='alice@test.com'", String.class)
            .startsWith("$2a$"));
    mvc.perform(get("/api/categories")).andExpect(status().isUnauthorized());
    mvc.perform(
            post("/api/auth/login")
                .contentType("application/json")
                .content("{\"email\":\"ALICE@test.com\",\"password\":\"password123\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").isNotEmpty());
    mvc.perform(
            post("/api/auth/login")
                .contentType("application/json")
                .content("{\"email\":\"alice@test.com\",\"password\":\"wrongpass\"}"))
        .andExpect(status().isUnauthorized());
    mvc.perform(post("/api/auth/logout").header("Authorization", "Bearer " + alice))
        .andExpect(status().isOk());
    mvc.perform(get("/api/categories").header("Authorization", "Bearer " + alice))
        .andExpect(status().isUnauthorized());
    db.update("UPDATE sessions SET expires_at='2020-01-01 00:00:00'");
    mvc.perform(get("/api/categories").header("Authorization", "Bearer " + bob))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void invalidAmountsDatesAndDuplicateCategoriesAreRejected() throws Exception {
    for (String amount : new String[] {"0", "-1", "1.001", "1000000000000"})
      mvc.perform(
              post("/api/transactions")
                  .header("Authorization", "Bearer " + alice)
                  .contentType("application/json")
                  .content(entry(expense, amount, "2026-10-01")))
          .andExpect(status().isBadRequest());
    mvc.perform(
            get("/api/transactions")
                .param("from", "2026-10-10")
                .param("to", "2026-10-01")
                .header("Authorization", "Bearer " + alice))
        .andExpect(status().isBadRequest());
    mvc.perform(
            get("/api/reports/monthly")
                .param("month", "bad")
                .header("Authorization", "Bearer " + alice))
        .andExpect(status().isBadRequest());
    mvc.perform(
            post("/api/categories")
                .header("Authorization", "Bearer " + alice)
                .contentType("application/json")
                .content("{\"name\":\"Tuition\",\"type\":\"INCOME\"}"))
        .andExpect(status().isCreated());
    mvc.perform(
            post("/api/categories")
                .header("Authorization", "Bearer " + alice)
                .contentType("application/json")
                .content("{\"name\":\"Tuition\",\"type\":\"INCOME\"}"))
        .andExpect(status().isConflict());
  }
}
