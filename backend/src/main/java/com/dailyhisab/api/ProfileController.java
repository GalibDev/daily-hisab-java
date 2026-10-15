package com.dailyhisab.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {
  private final JdbcTemplate db;
  private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

  public ProfileController(JdbcTemplate db) {
    this.db = db;
  }

  public record Name(@NotBlank @Size(max = 80) String name) {}

  public record Password(
      @NotNull @Size(min = 8, max = 64) String currentPassword,
      @NotNull @Size(min = 8, max = 64) String newPassword) {}

  @GetMapping
  public Map<String, Object> profile(@RequestAttribute long userId) {
    return db.queryForMap("SELECT name,email FROM users WHERE id=?", userId);
  }

  @PutMapping
  public Map<String, String> name(@RequestAttribute long userId, @Valid @RequestBody Name p) {
    db.update("UPDATE users SET name=? WHERE id=?", p.name().trim(), userId);
    return Map.of("name", p.name().trim());
  }

  @PostMapping("/password")
  @Transactional
  public Map<String, String> password(
      @RequestAttribute long userId, @Valid @RequestBody Password p) {
    String hash =
        db.queryForObject(
            "SELECT password_hash FROM users WHERE id=? FOR UPDATE", String.class, userId);
    if (!encoder.matches(p.currentPassword(), hash))
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current password is incorrect");
    if (p.newPassword().getBytes(StandardCharsets.UTF_8).length > 72)
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is too long");
    db.update(
        "UPDATE users SET password_hash=? WHERE id=?", encoder.encode(p.newPassword()), userId);
    db.update("DELETE FROM sessions WHERE user_id=?", userId);
    return Map.of("message", "Password changed. Please log in again");
  }
}
