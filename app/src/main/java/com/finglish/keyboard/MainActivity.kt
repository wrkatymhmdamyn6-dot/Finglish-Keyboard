package com.finglish.keyboard

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import android.content.Context
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
 override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState)
  val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(28,36,28,28) }
  root.addView(TextView(this).apply { text = "کیبورد فینگلیش\n\nکیبورد فارسی با تبدیل زنده به فینگلیش"; textSize = 22f })
  root.addView(Button(this).apply { text = "۱. فعال‌سازی کیبورد"; setOnClickListener { startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)) } })
  root.addView(Button(this).apply { text = "۲. انتخاب کیبورد"; setOnClickListener { (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).showInputMethodPicker() } })
  root.addView(TextView(this).apply { text = "\nکیبورد را فعال و انتخاب کن. هنگام تایپ فارسی، ترجمه در نوار بالای کلیدها نمایش داده می‌شود. با لمس ترجمه، متن جایگزین می‌شود."; textSize = 16f })
  setContentView(root)
 }
}
