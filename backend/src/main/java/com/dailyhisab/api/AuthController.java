package com.dailyhisab.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final JdbcTemplate db;
  private final BCryptPasswordEncoder passwords = new BCryptPasswordEncoder();

  public AuthController(JdbcTemplate db) {
    this.db = db;
  }

  public record Register(
      @NotBlank @Size(max = 80) String name,
      @NotBlank @Email @Size(max = 190) String email,
      @NotNull @Size(min = 8, max = 64) String password) {}

  public record Login(
      @NotBlank @Email String email, @NotNull @Size(min = 8, max = 64) String password) {}

  @PostMapping("/register")
  @ResponseStatus(HttpStatus.CREATED)
  @Transactional
  public Map<String, Object> register(@Valid @RequestBody Register r) {
    if (r.password().getBytes(StandardCharsets.UTF_8).length > 72)
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is too long");
    db.update(
        "INSERT INTO users(name,email,password_hash) VALUES (?,?,?)",
        r.name().trim(),
        email(r.email()),
        passwords.encode(r.password()));
    long id = db.queryForObject("SELECT id FROM users WHERE email=?", Long.class, email(r.email()));
    for (String name : List.of("Food", "Transport", "Shopping", "Bills", "Other"))
      db.update("INSERT INTO categories(user_id,name,type) VALUES (?,?,'EXPENSE')", id, name);
    for (String name : List.of("Salary", "Gift", "Other"))
      db.update("INSERT INTO categories(user_id,name,type) VALUES (?,?,'INCOME')", id, name);
    return session(id, r.name().trim());
  }

  @PostMapping("/login")
  @Transactional
  public Map<String, Object> login(@Valid @RequestBody Login r) {
    var users = db.queryForList("SELECT * FROM users WHERE email=?", email(r.email()));
    if (users.isEmpty()
        || !passwords.matches(r.password(), (String) users.get(0).get("password_hash")))
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email or password is incorrect");
    var u = users.get(0);
    return session(((Number) u.get("id")).longValue(), (String) u.get("name"));
  }

  @PostMapping("/logout")
  public Map<String, String> logout(HttpServletRequest request) {
    db.update("DELETE FROM sessions WHERE token_hash=?", hash(bearer(request)));
    return Map.of("message", "Logged out");
  }

  private Map<String, Object> session(long id, String name) {
    db.update("DELETE FROM sessions WHERE expires_at < ?", Timestamp.from(Instant.now()));
    byte[] bytes = new byte[32];
    new SecureRandom().nextBytes(bytes);
    String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    db.update(
        "INSERT INTO sessions(token_hash,user_id,expires_at) VALUES (?,?,?)",
        hash(token),
        id,
        Timestamp.from(Instant.now().plusSeconds(7 * 86400)));
    return Map.of("token", token, "name", name, "userId", id);
  }

  static String email(String email) {
    return email.trim().toLowerCase(Locale.ROOT);
  }

  static String bearer(HttpServletRequest request) {
    String h = request.getHeader("Authorization");
    if (h == null || !h.startsWith("Bearer "))
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please log in");
    return h.substring(7);
  }

  static String hash(String text) {
    try {
      return HexFormat.of()
          .formatHex(
              MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException ex) {
      throw new IllegalStateException(ex);
    }
  }
}
