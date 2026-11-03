package com.example.keyboardbazifinglish
import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.provider.Settings
import android.content.Context
import android.view.inputmethod.InputMethodManager
import android.widget.*
class MainActivity:Activity(){ override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState)
 val l=LinearLayout(this).apply{orientation=1;setPadding(24,40,24,24)}
 l.addView(TextView(this).apply{text="کیبورد بازی فینگلیش\nابتدا کیبورد را فعال و سپس انتخاب کن.";textSize=22f})
 l.addView(Button(this).apply{text="فعال‌سازی کیبورد";setOnClickListener{startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))}})
 l.addView(Button(this).apply{text="انتخاب کیبورد";setOnClickListener{(getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).showInputMethodPicker()}})
 setContentView(l)
}}