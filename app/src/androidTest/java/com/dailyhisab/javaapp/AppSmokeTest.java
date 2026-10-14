package com.dailyhisab.javaapp;

import static org.junit.Assert.*;

import android.content.Intent;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.*;
import java.io.File;
import org.junit.Test;

/** Run against a local backend at the emulator default server URL. */
public class AppSmokeTest {
  private final UiDevice device =
      UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());

  private UiObject2 waitFor(BySelector selector) {
    UiObject2 object = device.wait(Until.findObject(selector), 20000);
    assertNotNull("Missing UI: " + selector, object);
    return object;
  }

  private BySelector id(String name) {
    return By.res("com.galibdev.dailyhisab.java", name);
  }

  @Test
  public void registrationAddEntryAndMonthlyReport() throws Exception {
    Configurator.getInstance().setWaitForIdleTimeout(500);
    var context = InstrumentationRegistry.getInstrumentation().getTargetContext();
    context.getSharedPreferences("session", 0).edit().clear().commit();
    Intent launch = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
    launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    context.startActivity(launch);
    waitFor(id("switchAuth")).click();
    waitFor(id("name")).setText("University Demo");
    waitFor(id("email")).setText("demo" + System.currentTimeMillis() + "@example.com");
    waitFor(id("password")).setText("TestPassword123");
    waitFor(id("submit")).click();
    assertTrue(device.wait(Until.hasObject(id("greeting")), 20000));
    waitFor(id("add").enabled(true));
    waitFor(id("add")).click();
    UiObject2 amount = waitFor(By.clazz("android.widget.EditText"));
    amount.setText("250.50");
    waitFor(By.text("SAVE")).click();
    assertTrue(device.wait(Until.hasObject(By.text("৳ -250.50")), 20000));
    File dir = context.getExternalFilesDir(null);
    device.takeScreenshot(new File(dir, "dashboard.png"));
    waitFor(id("report")).click();
    waitFor(By.text("VIEW REPORT")).click();
    assertNotNull(device.wait(Until.findObject(By.textContains("Expenses: ৳ 250.50")), 20000));
    device.takeScreenshot(new File(dir, "monthly-report.png"));
  }
}
