package com.finglish.keyboard

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.inputmethodservice.InputMethodService
import android.view.Gravity
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class FinglishImeService : InputMethodService() {
    private var composing = ""
    private var english = false
    private var symbols = false
    private var clipboardPage = false
    private lateinit var root: LinearLayout
    private lateinit var keysArea: LinearLayout
    private lateinit var clipboardArea: LinearLayout
    private val history = mutableListOf<String>()
    private val bg = Color.rgb(25, 25, 31)
    private val keyBg = Color.rgb(52, 49, 59)
    private val pink = Color.rgb(244, 105, 157)
    private val white = Color.WHITE
    private val clipboard by lazy { getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager }
    private val clipListener = ClipboardManager.OnPrimaryClipChangedListener { rememberClipboard() }

    override fun onCreate() {
        super.onCreate()
        loadHistory()
        clipboard.addPrimaryClipChangedListener(clipListener)
    }

    override fun onDestroy() {
        clipboard.removePrimaryClipChangedListener(clipListener)
        super.onDestroy()
    }

    override fun onCreateInputView(): View {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(6), dp(7), dp(6), dp(7))
            setBackgroundColor(bg)
        }
        val toolbar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        toolbar.addView(tool("☷", "کلیپ‌بورد") { clipboardPage = !clipboardPage; renderContent() })
        toolbar.addView(tool(if (english) "فارسی" else "EN", "تغییر زبان") {
            english = !english; symbols = false; renderContent()
        })
        toolbar.addView(tool("⌫", "پاک کردن") { deleteOne() })
        toolbar.addView(tool("↵", "اینتر") { sendEnter() })
        root.addView(toolbar, LinearLayout.LayoutParams(-1, dp(42)))
        clipboardArea = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        keysArea = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        root.addView(clipboardArea, LinearLayout.LayoutParams(-1, 0, 1f))
        root.addView(keysArea, LinearLayout.LayoutParams(-1, 0, 1f))
        renderContent()
        return root
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        composing = ""
        rememberClipboard()
    }

    private fun renderContent() {
        if (!::root.isInitialized) return
        clipboardArea.removeAllViews()
        keysArea.removeAllViews()
        clipboardArea.visibility = if (clipboardPage) View.VISIBLE else View.GONE
        keysArea.visibility = if (clipboardPage) View.GONE else View.VISIBLE
        if (clipboardPage) renderClipboard() else renderKeys()
    }

    private fun renderClipboard() {
        val heading = TextView(this).apply {
            text = "متن‌های کپی‌شده"
            textSize = 15f
            setTextColor(pink)
            gravity = Gravity.CENTER
            setPadding(0, dp(5), 0, dp(8))
            typeface = Typeface.DEFAULT_BOLD
        }
        clipboardArea.addView(heading)
        val scroll = ScrollView(this)
        val list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        if (history.isEmpty()) {
            list.addView(TextView(this).apply {
                text = "هنوز متنی کپی نشده؛ یک متن کپی کن و دوباره این بخش را باز کن."
                setTextColor(white)
                gravity = Gravity.CENTER
                textSize = 13f
                setPadding(dp(12), dp(18), dp(12), dp(18))
            })
        } else {
            history.toList().forEach { item ->
                val b = Button(this).apply {
                    text = item
                    textSize = 13f
                    isAllCaps = false
                    gravity = Gravity.CENTER_VERTICAL
                    setTextColor(white)
                    background = rounded(keyBg, 12)
                    setPadding(dp(12), dp(8), dp(12), dp(8))
                    setOnClickListener {
                        currentInputConnection?.commitText(item, 1)
                        clipboardPage = false
                        renderContent()
                    }
                }
                list.addView(b, LinearLayout.LayoutParams(-1, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                    setMargins(dp(3), dp(3), dp(3), dp(3))
                })
            }
        }
        scroll.addView(list)
        clipboardArea.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        val actions = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        actions.addView(tool("بازگشت", "بازگشت به صفحه کلید") {
            clipboardPage = false; renderContent()
        })
        actions.addView(tool("پاک‌کردن فهرست", "پاک کردن تاریخچه") {
            history.clear(); saveHistory(); renderContent()
        })
        clipboardArea.addView(actions, LinearLayout.LayoutParams(-1, dp(42)))
    }

    private fun rememberClipboard() {
        try {
            val clip = clipboard.primaryClip ?: return
            if (clip.itemCount == 0) return
            val text = clip.getItemAt(0).coerceToText(this)?.toString()?.trim().orEmpty()
            if (text.isBlank()) return
            history.remove(text)
            history.add(0, text.take(2000))
            while (history.size > 20) history.removeAt(history.lastIndex)
            saveHistory()
            if (::root.isInitialized && clipboardPage) renderContent()
        } catch (_: Exception) { }
    }

    private fun loadHistory() {
        val saved = getSharedPreferences("clipboard_history", Context.MODE_PRIVATE)
            .getString("items", "") ?: ""
        history.addAll(saved.split("\n---CLIP---\n").filter { it.isNotBlank() }.take(20))
    }

    private fun saveHistory() {
        getSharedPreferences("clipboard_history", Context.MODE_PRIVATE).edit()
            .putString("items", history.joinToString("\n---CLIP---\n")).apply()
    }

    private fun renderKeys() {
        val rows = if (symbols) listOf(
            listOf("1","2","3","4","5","6","7","8","9","0"),
            listOf("@","#","$","%","&","-","+","(",")","/"),
            listOf("!","?","،","؛","'","\\"","_",".","،")
        ) else if (english) listOf(
            listOf("q","w","e","r","t","y","u","i","o","p"),
            listOf("a","s","d","f","g","h","j","k","l"),
            listOf("⇧","z","x","c","v","b","n","m","⌫")
        ) else listOf(
            listOf("ض","ص","ث","ق","ف","غ","ع","ه","خ","ح","ج"),
            listOf("ش","س","ی","ب","ل","ا","ت","ن","م","ک","گ"),
            listOf("ظ","ط","ز","ر","ذ","د","پ","و","چ","⌫")
        )
        rows.forEach { labels ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
            }
            labels.forEach { label ->
                val b = Button(this).apply {
                    text = label
                    isAllCaps = false
                    textSize = if (label.length > 1) 14f else 19f
                    setTextColor(white)
                    background = rounded(if (label == "⌫") pink else keyBg, 9)
                    setPadding(0, 0, 0, 0)
                    setOnClickListener { onKey(label) }
                }
                row.addView(b, LinearLayout.LayoutParams(0, dp(48), 1f).apply {
                    setMargins(dp(2), dp(3), dp(2), dp(3))
                })
            }
            keysArea.addView(row)
        }
        val bottom = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        bottom.addView(actionKey(if (symbols) "ABC" else "?123", 1.1f) {
            symbols = !symbols; renderContent()
        })
        bottom.addView(actionKey("فینگلیش", 1.2f) {
            english = !english; symbols = false; renderContent()
        })
        bottom.addView(actionKey("🌐", .8f) { requestShowSelf(0) })
        bottom.addView(actionKey("فاصله", 3.2f) { commitText(" ") })
        bottom.addView(actionKey("،", .8f) { commitText("،") })
        bottom.addView(actionKey("↵", 1f) { sendEnter() })
        keysArea.addView(bottom)
    }

    private fun onKey(key: String) {
        when (key) {
            "⌫" -> deleteOne()
            "⇧" -> { }
            else -> commitText(key)
        }
    }

    private fun commitText(value: String) {
        currentInputConnection?.commitText(value, 1)
        composing += value
    }

    private fun deleteOne() {
        currentInputConnection?.deleteSurroundingText(1, 0)
        if (composing.isNotEmpty()) composing = composing.dropLast(1)
    }

    private fun sendEnter() {
        val ic = currentInputConnection ?: return
        val action = currentInputEditorInfo?.imeOptions?.and(EditorInfo.IME_MASK_ACTION)
        if (action != null && action != EditorInfo.IME_ACTION_NONE &&
            action != EditorInfo.IME_ACTION_UNSPECIFIED) ic.performEditorAction(action)
        else ic.commitText("\n", 1)
    }

    private fun tool(label: String, description: String, action: () -> Unit) =
        TextView(this).apply {
            text = label
            contentDescription = description
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(pink)
            gravity = Gravity.CENTER
            background = rounded(keyBg, 10)
            setOnClickListener { action() }
            layoutParams = LinearLayout.LayoutParams(0, dp(38), 1f).apply {
                setMargins(dp(2), dp(1), dp(2), dp(1))
            }
        }

    private fun actionKey(label: String, weight: Float, action: () -> Unit) =
        TextView(this).apply {
            text = label
            textSize = 13f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(if (label == "فینگلیش") bg else white)
            gravity = Gravity.CENTER
            background = rounded(if (label == "فینگلیش") pink else keyBg, 9)
            setOnClickListener { action() }
            layoutParams = LinearLayout.LayoutParams(0, dp(43), weight).apply {
                setMargins(dp(2), dp(2), dp(2), dp(2))
            }
        }

    private fun rounded(color: Int, radius: Int) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = dp(radius).toFloat()
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
}
