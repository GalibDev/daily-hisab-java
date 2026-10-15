package com.dailyhisab.javaapp;

import android.content.Context;
import android.graphics.*;
import android.view.View;
import java.util.*;
import org.json.*;

/** Native Canvas chart; exact totals remain available as text below the chart. */
public class ReportChart extends View {
  private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
  private final boolean bn;
  private double income, expense;
  private final List<Double> slices = new ArrayList<>();
  private final int[] colors = {
    0xff147D72, 0xff557BD7, 0xffE7A648, 0xffAD78BE, 0xffDE7466, 0xff56A4B8
  };
  private boolean ready;

  public ReportChart(Context context, boolean bn) {
    super(context);
    this.bn = bn;
    setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
  }

  public void setReport(JSONObject report) {
    JSONObject summary = report.optJSONObject("summary");
    income = summary.optDouble("income");
    expense = summary.optDouble("expense");
    slices.clear();
    JSONArray categories = report.optJSONArray("categories");
    for (int i = 0; i < categories.length(); i++) {
      JSONObject c = categories.optJSONObject(i);
      if (c.optString("type").equals("EXPENSE")) slices.add(c.optDouble("total"));
    }
    ready = true;
    setContentDescription(
        (bn ? "আয় " : "Income ") + income + (bn ? ", ব্যয় " : ", expense ") + expense);
    invalidate();
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    float d = getResources().getDisplayMetrics().density, w = getWidth(), top = 32 * d;
    paint.setTextSize(14 * d);
    paint.setColor(0xff405B50);
    if (!ready) {
      canvas.drawText(
          bn ? "রিপোর্ট দেখতে মাস বাছুন" : "Choose a month to view the chart", 8 * d, top, paint);
      return;
    }
    double max = Math.max(income, expense);
    canvas.drawText(bn ? "আয় ও ব্যয়" : "Income and expenses", 8 * d, top, paint);
    String[] labels = {bn ? "আয়" : "Income", bn ? "ব্যয়" : "Expenses"};
    double[] values = {income, expense};
    for (int i = 0; i < 2; i++) {
      float y = (55 + 44 * i) * d;
      paint.setColor(0xff405B50);
      canvas.drawText(labels[i], 8 * d, y + 18 * d, paint);
      float left = 85 * d, right = w - 12 * d;
      paint.setColor(0xffE5EBF0);
      canvas.drawRoundRect(left, y, right, y + 24 * d, 8 * d, 8 * d, paint);
      paint.setColor(i == 0 ? 0xff147D72 : 0xffDE7466);
      float width = max == 0 ? 0 : (float) (values[i] / max) * (right - left);
      if (width > 0) canvas.drawRoundRect(left, y, left + width, y + 24 * d, 8 * d, 8 * d, paint);
    }
    paint.setColor(0xff405B50);
    canvas.drawText(bn ? "খাত অনুযায়ী ব্যয়" : "Expense share by category", 8 * d, 154 * d, paint);
    float center = w / 2, r = 57 * d;
    RectF bounds = new RectF(center - r, 170 * d, center + r, 284 * d);
    float start = -90;
    if (expense == 0) {
      paint.setColor(0xffE5EBF0);
      canvas.drawOval(bounds, paint);
    } else
      for (int i = 0; i < slices.size(); i++) {
        float angle = (float) (slices.get(i) / expense * 360);
        paint.setColor(colors[i % colors.length]);
        canvas.drawArc(bounds, start, angle, true, paint);
        start += angle;
      }
    paint.setColor(0xffF4F7F6);
    canvas.drawCircle(center, 227 * d, 34 * d, paint);
    paint.setColor(0xff405B50);
    paint.setTextAlign(Paint.Align.CENTER);
    paint.setTextSize(12 * d);
    canvas.drawText(
        expense == 0 ? (bn ? "তথ্য নেই" : "No expenses") : (bn ? "মোট ব্যয়" : "Expenses"),
        center,
        231 * d,
        paint);
    paint.setTextAlign(Paint.Align.LEFT);
  }
}
