package com.dailyhisab.api;

import java.time.LocalDate;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/export")
public class ExportController {
  private final JdbcTemplate db;
  public ExportController(JdbcTemplate db){this.db=db;}
  /** Full ledger export, independent of the 500-row list limit. */
  @GetMapping public Map<String,Object> csv(@RequestAttribute long userId){
    StringBuilder csv=new StringBuilder("Date,Type,Category,Amount (BDT),Note\r\n");
    var rows=db.queryForList("SELECT t.occurred_on,c.type,c.name,t.amount,t.note FROM transactions t JOIN categories c ON c.id=t.category_id WHERE t.user_id=? ORDER BY t.occurred_on DESC,t.id DESC",userId);
    for(var r:rows)csv.append(cell(r.get("occurred_on").toString())).append(',').append(cell(r.get("type").toString())).append(',').append(cell(r.get("name").toString())).append(',').append(r.get("amount")).append(',').append(cell(r.get("note").toString())).append("\r\n");
    return Map.of("csv",csv.toString(),"count",rows.size());
  }
  static String cell(String text){
    String trimmed=text.stripLeading();
    if(!trimmed.isEmpty() && ("=+-@".indexOf(trimmed.charAt(0))>=0 || Character.isWhitespace(text.charAt(0))))text="'"+text;
    return "\""+text.replace("\"","\"\"")+"\"";
  }
}
