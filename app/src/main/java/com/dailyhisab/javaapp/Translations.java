package com.dailyhisab.javaapp;

import java.util.HashMap;
import java.util.Map;

/** UI-only translations; API enum values remain unchanged. */
final class Translations {
  private static final Map<String, String> BN = new HashMap<>();

  static {
    BN.put("DAILY HISAB", "ডেইলি হিসাব");
    BN.put("Your money.\\nA clearer picture.", "আপনার টাকা।\\nহিসাব আরও সহজ।");
    BN.put("Your money.\nA clearer picture.", "আপনার টাকা।\nহিসাব আরও সহজ।");
    BN.put("Track everyday income and expenses in taka.", "প্রতিদিনের আয় ও ব্যয়ের হিসাব রাখুন।");
    BN.put("Your name", "আপনার নাম");
    BN.put("Email address", "ইমেইল ঠিকানা");
    BN.put("Password (at least 8 characters)", "পাসওয়ার্ড (কমপক্ষে ৮ অক্ষর)");
    BN.put("Log in", "লগ ইন");
    BN.put("Create account", "অ্যাকাউন্ট তৈরি করুন");
    BN.put("New here? Create an account", "নতুন? অ্যাকাউন্ট তৈরি করুন");
    BN.put("Already registered? Log in", "অ্যাকাউন্ট আছে? লগ ইন করুন");
    BN.put("Server settings", "সার্ভার সেটিংস");
    BN.put("Your everyday money, all in one place.", "প্রতিদিনের হিসাব, এক জায়গায়।");
    BN.put("AVAILABLE BALANCE · ALL TIME", "বর্তমান ব্যালেন্স · সব সময়");
    BN.put("Loading totals…", "মোট হিসাব আসছে…");
    BN.put("+ Add transaction", "+ লেনদেন যোগ করুন");
    BN.put("Categories", "খাতসমূহ");
    BN.put("Monthly report", "মাসিক রিপোর্ট");
    BN.put("Transactions", "লেনদেন");
    BN.put("All dates · All types · All categories", "সব তারিখ · সব ধরন · সব খাত");
    BN.put("Filter / Refresh", "ফিল্টার / রিফ্রেশ");
    BN.put("Log out", "লগ আউট");
    BN.put("Home", "হোম");
    BN.put("Budgets", "বাজেট");
    BN.put("Settings", "সেটিংস");
    BN.put("Search", "খুঁজুন");
    BN.put("Search note or category", "নোট বা খাত দিয়ে খুঁজুন");
    BN.put("Language", "ভাষা");
    BN.put("Profile", "প্রোফাইল");
    BN.put("Change password", "পাসওয়ার্ড পরিবর্তন");
    BN.put("Export full ledger (CSV)", "সব হিসাব CSV হিসেবে সংরক্ষণ");
    BN.put("Save", "সংরক্ষণ");
    BN.put("Cancel", "বাতিল");
    BN.put("Close", "বন্ধ");
    BN.put("Edit", "সম্পাদনা");
    BN.put("Delete", "মুছুন");
    BN.put("Rename", "নাম পরিবর্তন");
    BN.put("Add", "যোগ করুন");
    BN.put("Apply", "প্রয়োগ");
    BN.put("Reset", "রিসেট");
    BN.put("Retry", "আবার চেষ্টা করুন");
    BN.put("Hello, ", "স্বাগতম, ");
    BN.put("Income  ", "আয়  ");
    BN.put("Expenses  ", "ব্যয়  ");
    BN.put("Income: ", "আয়: ");
    BN.put("Expenses: ", "ব্যয়: ");
    BN.put("Net: ", "নিট: ");
    BN.put("\nExpenses: ", "\nব্যয়: ");
    BN.put("\nNet: ", "\nনিট: ");
    BN.put("\n\nCategory totals\n", "\n\nখাত অনুযায়ী মোট\n");
    BN.put("Any start", "শুরুর তারিখ নেই");
    BN.put("Any end", "শেষ তারিখ নেই");
    BN.put("All types", "সব ধরন");
    BN.put("All categories", "সব খাত");
    BN.put(" · All categories", " · সব খাত");
    BN.put(" · Selected category", " · নির্বাচিত খাত");
    BN.put("Tap to edit or delete", "সম্পাদনা বা মুছতে চাপুন");
    BN.put("Transaction", "লেনদেন");
    BN.put("Transaction ", "লেনদেন ");
    BN.put(". Tap to edit or delete", ". সম্পাদনা বা মুছতে চাপুন");
    BN.put("Delete transaction?", "লেনদেন মুছবেন?");
    BN.put("This removes the entry from your balance.", "এই লেনদেন মুছে ব্যালেন্স আপডেট হবে।");
    BN.put(
        "No transactions yet. Add one or change your filters.",
        "কোনো লেনদেন নেই। নতুন লেনদেন যোগ করুন বা ফিল্টার বদলান।");
    BN.put(
        "Showing the latest 500 matches. Narrow the date filter to see older entries.",
        "সর্বশেষ ৫০০টি ফলাফল দেখানো হচ্ছে। পুরোনো হিসাব দেখতে তারিখ ফিল্টার ব্যবহার করুন।");
    BN.put("Add transaction", "লেনদেন যোগ করুন");
    BN.put("Edit transaction", "লেনদেন সম্পাদনা");
    BN.put("Amount (BDT)", "টাকার পরিমাণ");
    BN.put("Date: YYYY-MM-DD", "তারিখ: YYYY-MM-DD");
    BN.put("Note (optional)", "নোট (ঐচ্ছিক)");
    BN.put("Category name", "খাতের নাম");
    BN.put("Add category", "খাত যোগ করুন");
    BN.put("Rename category", "খাতের নাম পরিবর্তন");
    BN.put("From: YYYY-MM-DD (optional)", "শুরু: YYYY-MM-DD (ঐচ্ছিক)");
    BN.put("To: YYYY-MM-DD (optional)", "শেষ: YYYY-MM-DD (ঐচ্ছিক)");
    BN.put("Filter transactions", "লেনদেন ফিল্টার");
    BN.put("Month: YYYY-MM", "মাস: YYYY-MM");
    BN.put("View report", "রিপোর্ট দেখুন");
    BN.put("No transactions for this month.", "এই মাসে কোনো লেনদেন নেই।");
    BN.put("Name must contain 1–60 characters.", "নাম ১–৬০ অক্ষরের হতে হবে।");
    BN.put("Name must contain 1–80 characters.", "নাম ১–৮০ অক্ষরের হতে হবে।");
    BN.put(
        "Enter a positive amount with up to 2 decimals and a valid date.",
        "সঠিক তারিখ ও দশমিকের পরে সর্বোচ্চ ২ ঘরসহ শূন্যের বেশি টাকা লিখুন।");
    BN.put(
        "Enter a positive amount with up to 2 decimals.",
        "দশমিকের পরে সর্বোচ্চ ২ ঘরসহ শূন্যের বেশি টাকা লিখুন।");
    BN.put(
        "Use YYYY-MM-DD. Start must be before end.",
        "YYYY-MM-DD ব্যবহার করুন। শুরুর তারিখ শেষের আগে হতে হবে।");
    BN.put("Use YYYY-MM, for example 2026-10.", "YYYY-MM ব্যবহার করুন, যেমন 2026-10।");
    BN.put("Connecting…", "সংযোগ হচ্ছে…");
    BN.put("Loading…", "লোড হচ্ছে…");
    BN.put("Session expired. Please log in.", "লগইনের সময় শেষ। আবার লগ ইন করুন।");
    BN.put("Current password", "বর্তমান পাসওয়ার্ড");
    BN.put("New password (8+ characters)", "নতুন পাসওয়ার্ড (৮+ অক্ষর)");
    BN.put("Confirm new password", "নতুন পাসওয়ার্ড আবার লিখুন");
    BN.put(
        "Changing your password logs out all devices.",
        "পাসওয়ার্ড বদলালে সব ডিভাইস থেকে লগ আউট হবে।");
    BN.put(
        "Use matching passwords of 8–64 characters.", "দুইবার একই ৮–৬৪ অক্ষরের পাসওয়ার্ড লিখুন।");
    BN.put("Password changed. Please log in again", "পাসওয়ার্ড বদলেছে। আবার লগ ইন করুন।");
    BN.put("Current password is incorrect", "বর্তমান পাসওয়ার্ড সঠিক নয়");
    BN.put("Password is too long", "পাসওয়ার্ড বেশি লম্বা");
    BN.put("Delete unused category?", "অব্যবহৃত খাত মুছবেন?");
    BN.put(
        "Used categories cannot be deleted. Transactions and budgets stay safe.",
        "ব্যবহৃত খাত মোছা যাবে না। লেনদেন ও বাজেট অক্ষত থাকবে।");
    BN.put(
        "Category is used by transactions or budgets. Rename it or remove those references first",
        "এই খাত লেনদেন বা বাজেটে ব্যবহৃত। নাম বদলান বা আগে সংশ্লিষ্ট হিসাব সরান।");
    BN.put("Monthly budget", "মাসিক বাজেট");
    BN.put("Budget", "বাজেট");
    BN.put("This month's budgets", "এই মাসের বাজেট");
    BN.put("Set a monthly budget", "মাসিক বাজেট ঠিক করুন");
    BN.put("Spent: ", "ব্যয়: ");
    BN.put("Remaining: ", "বাকি: ");
    BN.put("Budget limit reached", "বাজেটের সীমায় পৌঁছেছে");
    BN.put("80% of budget used", "বাজেটের ৮০% ব্যবহার হয়েছে");
    BN.put("No budgets for this month.", "এই মাসে কোনো বাজেট নেই।");
    BN.put("View budgets", "বাজেট দেখুন");
    BN.put("Add budget", "বাজেট যোগ করুন");
    BN.put("Delete budget?", "বাজেট মুছবেন?");
    BN.put("Budget amount (BDT)", "বাজেটের পরিমাণ (টাকা)");
    BN.put("Save budget", "বাজেট সংরক্ষণ");
    BN.put("CSV saved", "CSV সংরক্ষণ হয়েছে");
    BN.put("Export failed. Please retry.", "এক্সপোর্ট হয়নি। আবার চেষ্টা করুন।");
    BN.put("Food", "খাবার");
    BN.put("Transport", "যাতায়াত");
    BN.put("Shopping", "কেনাকাটা");
    BN.put("Bills", "বিল");
    BN.put("Other", "অন্যান্য");
    BN.put("Salary", "বেতন");
    BN.put("Gift", "উপহার");
    BN.put("INCOME", "আয়");
    BN.put("EXPENSE", "ব্যয়");
    BN.put(
        "Cannot reach server. Check the server URL, connection and backend.",
        "সার্ভারে সংযোগ হচ্ছে না। সার্ভারের ঠিকানা, ইন্টারনেট ও ব্যাকএন্ড দেখুন।");
    BN.put("Email or password is incorrect", "ইমেইল বা পাসওয়ার্ড সঠিক নয়");
    BN.put(
        "Email or category already exists, or this category is in use",
        "এই ইমেইল/খাত আগে থেকেই আছে বা খাতটি ব্যবহৃত।");
    BN.put(
        "Check the fields, amount and date format (YYYY-MM-DD)",
        "তথ্য, টাকার পরিমাণ ও তারিখের ফরম্যাট (YYYY-MM-DD) দেখুন।");
    BN.put("Please log in", "লগ ইন করুন");
    BN.put("Session expired. Please log in", "লগইনের সময় শেষ। আবার লগ ইন করুন");
    BN.put("Item not found", "তথ্য পাওয়া যায়নি");
    BN.put("Entry or category not found", "লেনদেন বা খাত পাওয়া যায়নি");
    BN.put("Budgets require an expense category", "বাজেটের জন্য ব্যয়ের খাত বাছুন");
    BN.put("API server URL", "API সার্ভারের ঠিকানা");
    BN.put("Use http(s)://host:port without a path", "পাথ ছাড়া http(s)://host:port লিখুন");
    BN.put(
        "Emulator: http://10.0.2.2:8080\n"
            + "Phone: use your computer's LAN IP. Release builds require HTTPS.",
        "ইমুলেটর: http://10.0.2.2:8080\n"
            + "ফোনে কম্পিউটারের LAN IP ব্যবহার করুন। রিলিজ অ্যাপে HTTPS দরকার।");
    BN.put(
        "Enter a valid email, password of 8+ characters, and your name when registering.",
        "সঠিক ইমেইল, ৮+ অক্ষরের পাসওয়ার্ড ও নিবন্ধনের সময় আপনার নাম লিখুন।");
  }

  static String bn(String text) {
    String value = BN.get(text);
    return value == null ? text : value;
  }
}
