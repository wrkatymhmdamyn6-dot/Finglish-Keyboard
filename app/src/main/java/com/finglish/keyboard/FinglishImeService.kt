package com.finglish.keyboard

import android.inputmethodservice.InputMethodService
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.view.inputmethod.InputConnection
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class FinglishImeService : InputMethodService() {
 private var composing = ""
 private lateinit var translation: TextView
 private lateinit var preview: TextView
 override fun onCreateInputView(): View {
  val root = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(4,6,4,8); setBackgroundColor(Color.rgb(232,235,242)) }
  preview = TextView(this).apply { text="فارسی  ⇢  Finglish"; textSize=13f; gravity=Gravity.CENTER; setTextColor(Color.DKGRAY); setPadding(4,5,4,5) }
  translation = TextView(this).apply { text="ترجمه اینجا نمایش داده می‌شود"; textSize=18f; gravity=Gravity.CENTER; setTextColor(Color.rgb(35,75,160)); typeface=Typeface.DEFAULT_BOLD; setPadding(8,10,8,10); setBackgroundColor(Color.WHITE); setOnClickListener { applyTranslation() } }
  root.addView(preview); root.addView(translation)
  listOf("ض ص ث ق ف غ ع ه خ ح ج چ","ش س ی ب ل ا ت ن م ک گ","ظ ط ز ر ذ د پ و").forEach { row ->
   val line=LinearLayout(this).apply { orientation=LinearLayout.HORIZONTAL; gravity=Gravity.CENTER }
   row.split(" ").forEach { line.addView(makeKey(it)) }; root.addView(line)
  }
  val actions=LinearLayout(this).apply { orientation=LinearLayout.HORIZONTAL; gravity=Gravity.CENTER }
  listOf("؟","،","⌫","فاصله","↵").forEach { actions.addView(makeKey(it)) }; root.addView(actions)
  return root
 }
 private fun makeKey(label:String): Button = Button(this).apply {
  text=label; isAllCaps=false; textSize=if(label=="فاصله") 12f else 17f; setTextColor(Color.BLACK); setPadding(0,0,0,0); minWidth=0; minimumWidth=0; minHeight=0; minimumHeight=0
  layoutParams=LinearLayout.LayoutParams(0,dp(48),1f).apply { setMargins(dp(2),dp(3),dp(2),dp(3)) }; setOnClickListener { handleKey(label) }
 }
 private fun handleKey(key:String) { val ic:InputConnection=currentInputConnection ?: return
  when(key) {
   "⌫" -> { if(composing.isNotEmpty()) { composing=composing.dropLast(1); if(composing.isEmpty()) ic.commitText("",1) else ic.setComposingText(composing,1) } else ic.deleteSurroundingText(1,0); updateSuggestion() }
   "فاصله" -> { ic.commitText(" ",1); composing=""; updateSuggestion() }
   "↵" -> { ic.commitText("\n",1); composing=""; updateSuggestion() }
   else -> { composing+=key; ic.setComposingText(composing,1); updateSuggestion() }
  }
 }
 private fun updateSuggestion() { translation.text=if(composing.isEmpty()) "ترجمه اینجا نمایش داده می‌شود" else Transliterator.toFinglish(composing); preview.text=if(composing.isEmpty()) "فارسی  ⇢  Finglish" else composing }
 private fun applyTranslation() { if(composing.isEmpty()) return; currentInputConnection?.commitText(Transliterator.toFinglish(composing),1); composing=""; updateSuggestion() }
 private fun dp(value:Int):Int=(value*resources.displayMetrics.density).toInt()
}
