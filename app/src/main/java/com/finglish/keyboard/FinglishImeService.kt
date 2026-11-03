package com.finglish.keyboard

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.inputmethodservice.InputMethodService
import android.view.Gravity
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class FinglishImeService : InputMethodService() {
    private var composing = ""
    private var english = false
    private var symbols = false
    private lateinit var root: LinearLayout
    private lateinit var preview: TextView
    private lateinit var translated: TextView
    private val bg = Color.rgb(22, 27, 38)
    private val keyBg = Color.rgb(48, 57, 73)
    private val accent = Color.rgb(65, 190, 255)
    private val textColor = Color.WHITE

    override fun onCreateInputView(): View {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(5), dp(6), dp(5), dp(7))
            setBackgroundColor(bg)
        }
        val toolbar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        toolbar.addView(tool("🎮", "بازی") { symbols = false; renderKeys() })
        toolbar.addView(tool(if (english) "فا" else "EN", "زبان") { english = !english; composing = ""; updatePreview(); renderKeys() })
        toolbar.addView(tool("⌫", "پاک‌کردن") { deleteOne() })
        toolbar.addView(tool("↵", "ارسال") { sendEnter() })
        toolbar.addView(tool("فینگلیش", "تبدیل") { applyTranslation() })
        root.addView(toolbar, LinearLayout.LayoutParams(-1, dp(42)))

        preview = TextView(this).apply {
            text = "کیبورد بازی • برای چت داخل بازی"
            setTextColor(Color.LTGRAY)
            textSize = 12f
            gravity = Gravity.CENTER
            setPadding(2, dp(3), 2, dp(3))
        }
        root.addView(preview, LinearLayout.LayoutParams(-1, dp(24)))
        translated = TextView(this).apply {
            text = "متن فینگلیش اینجا نمایش داده می‌شود"
            setTextColor(accent)
            textSize = 14f
            gravity = Gravity.CENTER
            setPadding(2, dp(2), 2, dp(5))
        }
        root.addView(translated, LinearLayout.LayoutParams(-1, dp(28)))
        renderKeys()
        return root
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        composing = ""
        if (::preview.isInitialized) updatePreview()
    }

    private fun renderKeys() {
        if (!::root.isInitialized) return
        while (root.childCount > 3) root.removeViewAt(3)
        val rows = if (symbols) {
            listOf(listOf("1","2","3","4","5","6","7","8","9","0"),
                listOf("@","#","$","%","&","-","+","(",")","/"),
                listOf("!","?",":",";","'","\"","_",".",","))
        } else if (english) {
            listOf(listOf("q","w","e","r","t","y","u","i","o","p"),
                listOf("a","s","d","f","g","h","j","k","l"),
                listOf("⇧","z","x","c","v","b","n","m","⌫"))
        } else {
            listOf(listOf("ض","ص","ث","ق","ف","غ","ع","ه","خ","ح","ج"),
                listOf("ش","س","ی","ب","ل","ا","ت","ن","م","ک","گ"),
                listOf("ظ","ط","ز","ر","ذ","د","پ","و","چ","⌫"))
        }
        rows.forEach { labels ->
            val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER }
            labels.forEach { label ->
                val b = Button(this).apply {
                    text = label
                    isAllCaps = false
                    textSize = if (label.length > 1) 14f else 20f
                    setTextColor(textColor)
                    background = rounded(if (label == "⌫") Color.rgb(210,75,91) else keyBg, 8)
                    setPadding(0, 0, 0, 0)
                    minWidth = 0; minimumWidth = 0; minHeight = 0; minimumHeight = 0
                    setOnClickListener { onKey(label) }
                }
                val lp = LinearLayout.LayoutParams(0, dp(47), 1f).apply { setMargins(dp(2), dp(3), dp(2), dp(3)) }
                row.addView(b, lp)
            }
            root.addView(row, LinearLayout.LayoutParams(-1, dp(53)))
        }
        val bottom = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER }
        bottom.addView(actionKey(if (symbols) "ABC" else "?123", 1.15f) { symbols = !symbols; renderKeys() })
        bottom.addView(actionKey("🌐", .8f) { english = !english; symbols = false; renderKeys() })
        bottom.addView(actionKey("فاصله", 3.4f) { commitText(" ") })
        bottom.addView(actionKey("،", .8f) { commitText("،") })
        bottom.addView(actionKey("↵", 1.1f) { sendEnter() })
        root.addView(bottom, LinearLayout.LayoutParams(-1, dp(51)))
    }

    private fun onKey(key: String) {
        when (key) {
            "⌫" -> deleteOne()
            "⇧" -> { english = true; renderKeys() }
            else -> commitText(key)
        }
    }

    private fun commitText(value: String) {
        val ic = currentInputConnection ?: return
        ic.commitText(value, 1)
        composing += value
        updatePreview()
    }

    private fun deleteOne() {
        val ic = currentInputConnection ?: return
        if (composing.isNotEmpty()) composing = composing.dropLast(1)
        ic.deleteSurroundingText(1, 0)
        updatePreview()
    }

    private fun sendEnter() {
        val ic = currentInputConnection ?: return
        val action = currentInputEditorInfo?.imeOptions?.and(EditorInfo.IME_MASK_ACTION)
        if (action != null && action != EditorInfo.IME_ACTION_NONE && action != EditorInfo.IME_ACTION_UNSPECIFIED) {
            ic.performEditorAction(action)
        } else {
            ic.commitText("\n", 1)
        }
    }

    private fun applyTranslation() {
        if (composing.isBlank()) return
        val result = Transliterator.toFinglish(composing)
        currentInputConnection?.commitText(result, 1)
        composing = ""
        updatePreview()
    }

    private fun updatePreview() {
        if (!::preview.isInitialized) return
        preview.text = if (composing.isBlank()) "کیبورد بازی • آماده چت" else composing
        translated.text = if (composing.isBlank()) "فارسی و انگلیسی • مناسب چت بازی‌ها"
            else Transliterator.toFinglish(composing)
    }

    private fun tool(label: String, description: String, action: () -> Unit) = TextView(this).apply {
        text = label
        contentDescription = description
        textSize = if (label.length > 4) 12f else 17f
        typeface = Typeface.DEFAULT_BOLD
        setTextColor(accent)
        gravity = Gravity.CENTER
        background = rounded(Color.rgb(35, 44, 60), 9)
        setOnClickListener { action() }
        layoutParams = LinearLayout.LayoutParams(0, dp(36), 1f).apply { setMargins(dp(2), 0, dp(2), 0) }
    }

    private fun actionKey(label: String, weight: Float, action: () -> Unit) = TextView(this).apply {
        text = label
        textSize = 14f
        typeface = Typeface.DEFAULT_BOLD
        setTextColor(textColor)
        gravity = Gravity.CENTER
        background = rounded(if (label == "↵") accent else keyBg, 8)
        setOnClickListener { action() }
        layoutParams = LinearLayout.LayoutParams(0, dp(43), weight).apply { setMargins(dp(2), dp(3), dp(2), dp(2)) }
    }

    private fun rounded(color: Int, radius: Int) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = dp(radius).toFloat()
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
}
