package com.dailyhisab.api;

import jakarta.servlet.http.*;
import java.sql.Timestamp;
import java.time.Instant;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class WebConfig implements WebMvcConfigurer {
  private final JdbcTemplate db;

  public WebConfig(JdbcTemplate db) {
    this.db = db;
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry
        .addInterceptor(
            new HandlerInterceptor() {
              @Override
              public boolean preHandle(
                  HttpServletRequest req, HttpServletResponse res, Object handler) {
                var ids =
                    db.queryForList(
                        "SELECT user_id FROM sessions WHERE token_hash=? AND expires_at>?",
                        Long.class,
                        AuthController.hash(AuthController.bearer(req)),
                        Timestamp.from(Instant.now()));
                if (ids.isEmpty())
                  throw new ResponseStatusException(
                      HttpStatus.UNAUTHORIZED, "Session expired. Please log in");
                req.setAttribute("userId", ids.get(0));
                return true;
              }
            })
        .addPathPatterns("/api/**")
        .excludePathPatterns("/api/auth/register", "/api/auth/login");
  }
}
