package com.example.keyboardbazifinglish
import android.inputmethodservice.InputMethodService
import android.content.Context
import android.content.ClipboardManager
import android.view.View
import android.widget.*
import android.graphics.Color
import android.view.inputmethod.EditorInfo
class GameKeyboardService:InputMethodService(){ var finglish=false; lateinit var panel:LinearLayout
 override fun onCreateInputView():View { panel=LinearLayout(this).apply{orientation=1;setPadding(5,5,5,5);setBackgroundColor(Color.rgb(25,25,32))}
 val bar=LinearLayout(this)
 bar.addView(btn("فینگلیش: خاموش"){finglish=!finglish;(it as Button).text=if(finglish)"فینگلیش: روشن" else "فینگلیش: خاموش"}) ,LinearLayout.LayoutParams(0,48,1f))
 bar.addView(btn("کلیپ‌بورد / چسباندن"){val c=getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager; val t=c.primaryClip?.getItemAt(0)?.coerceToText(this)?.toString().orEmpty();currentInputConnection?.commitText(t,1)},LinearLayout.LayoutParams(0,48,1f))
 panel.addView(bar)
 listOf("ض ص ث ق ف غ ع ه خ ح ج","ش س ی ب ل ا ت ن م ک گ","ظ ط ز ر ذ د پ و چ").forEach{r->val row=LinearLayout(this);r.split(" ").forEach{ch->row.addView(btn(ch){currentInputConnection?.commitText(if(finglish)Transliterator.convert(ch) else ch,1)},LinearLayout.LayoutParams(0,48,1f))};panel.addView(row)}
 val bottom=LinearLayout(this);bottom.addView(btn("?123"){ });bottom.addView(btn("فاصله"){currentInputConnection?.commitText(" ",1)},LinearLayout.LayoutParams(0,48,2f));bottom.addView(btn("⌫"){currentInputConnection?.deleteSurroundingText(1,0)});bottom.addView(btn("↵"){val a=currentInputEditorInfo?.imeOptions?.and(EditorInfo.IME_MASK_ACTION)?:0;if(a!=0)currentInputConnection?.performEditorAction(a) else currentInputConnection?.commitText("\n",1)});panel.addView(bottom);return panel }
 private fun btn(s:String,f:(Button)->Unit)=Button(this).apply{text=s;textSize=16f;isAllCaps=false;setTextColor(Color.WHITE);setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(55,50,65)));setOnClickListener{f(this)}}
}