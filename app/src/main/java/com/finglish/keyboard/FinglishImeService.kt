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
    private var finglishMode = false
    private var capitalizeNextFinglish = true
    private var symbols = false
    private var clipboardPage = false
    private var toolsPage = false
    private lateinit var toolsArea: LinearLayout
    private lateinit var root: LinearLayout
    private lateinit var keysArea: LinearLayout
    private lateinit var clipboardArea: LinearLayout
    private lateinit var finglishPreview: TextView
    private val history = mutableListOf<String>()
    private val bg = Color.rgb(27, 26, 30)
    private val keyBg = Color.rgb(49, 47, 51)
    private val pink = Color.rgb(255, 174, 184)
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
        toolbar.addView(tool("☷", "کلیپ‌بورد") { clipboardPage = !clipboardPage; toolsPage = false; renderContent() })
        toolbar.addView(tool(if (english) "فارسی" else "EN", "تغییر زبان کیبورد") {
            english = !english; symbols = false; finglishMode = false; toolsPage = false; renderContent()
        })
        toolbar.addView(tool("⚙", "تنظیمات و ابزارها") { toolsPage = !toolsPage; renderContent() })
        root.addView(toolbar, LinearLayout.LayoutParams(-1, dp(38)))
        toolsArea = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER; visibility = View.GONE }
        toolsArea.addView(tool("☷", "کلیپ‌بورد") { clipboardPage = !clipboardPage; toolsPage = false; renderContent() })
        toolsArea.addView(tool(if (english) "فارسی" else "EN", "تغییر زبان") { english = !english; symbols = false; toolsPage = false; renderContent() })
        toolsArea.addView(tool("⌫", "پاک کردن") { deleteOne() })
        toolsArea.addView(tool("↵", "اینتر") { sendEnter() })
        root.addView(toolsArea, LinearLayout.LayoutParams(-1, dp(42)))
        val previewBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            background = rounded(keyBg, 9)
            setPadding(dp(9), 0, dp(9), 0)
        }
        previewBar.addView(TextView(this).apply {
            text = "فینگلیش:"
            textSize = 12f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(pink)
        })
        finglishPreview = TextView(this).apply {
            text = " "
            textSize = 18f
            setTextColor(white)
            gravity = Gravity.CENTER_VERTICAL
            maxLines = 1
            ellipsize = android.text.TextUtils.TruncateAt.END
            setPadding(dp(8), 0, 0, 0)
        }
        previewBar.addView(finglishPreview, LinearLayout.LayoutParams(0, -1, 1f))
        previewBar.visibility = View.GONE
        root.addView(previewBar, LinearLayout.LayoutParams(-1, dp(34)).apply { setMargins(0, dp(3), 0, dp(3)) })
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
        updateFinglishPreview()
        rememberClipboard()
    }

    private fun updateFinglishPreview() {
        if (!::finglishPreview.isInitialized) return
        finglishPreview.text = composing
        (finglishPreview.parent as? View)?.visibility = if (finglishMode && !clipboardPage) View.VISIBLE else View.GONE
    }

    private fun renderContent() {
        if (!::root.isInitialized) return
        clipboardArea.removeAllViews()
        keysArea.removeAllViews()
        clipboardArea.visibility = if (clipboardPage || toolsPage) View.VISIBLE else View.GONE
        keysArea.visibility = if (clipboardPage || toolsPage) View.GONE else View.VISIBLE
        toolsArea.visibility = if (toolsPage && !clipboardPage) View.VISIBLE else View.GONE
        if (clipboardPage) renderClipboard() else if (toolsPage) renderToolsSettings() else renderKeys()
        updateFinglishPreview()
    }

    private fun renderToolsSettings() {
        clipboardArea.addView(TextView(this).apply {
            text = "تنظیمات و ابزارها"; textSize = 17f; setTextColor(pink)
            gravity = Gravity.CENTER; typeface = Typeface.DEFAULT_BOLD; setPadding(0, dp(8), 0, dp(8))
        })
        clipboardArea.addView(TextView(this).apply {
            text = "چهار ابزار در نوار بالای تنظیمات قرار دارند."
            textSize = 14f; setTextColor(white); gravity = Gravity.CENTER
            setPadding(dp(10), dp(12), dp(10), dp(12))
        })
        clipboardArea.addView(tool("بازگشت به صفحه‌کلید", "بستن تنظیمات") { toolsPage = false; renderContent() },
            LinearLayout.LayoutParams(-1, dp(42)))
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
            listOf("!", "?", ",", ";", "'", "_", ".", ":")
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
                    textSize = if (label.length > 1) 16f else 22f
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
        bottom.addView(actionKey("؟", 1.1f) { commitText("؟") })
        bottom.addView(actionKey(if (finglishMode) "فینگلیش ✓" else "فینگلیش", 1.2f) {
            if (finglishMode) {
                if (composing.isNotEmpty()) currentInputConnection?.commitText(Transliterator.toFinglish(composing), 1)
                composing = ""
                currentInputConnection?.finishComposingText()
                finglishMode = false
            } else {
                finglishMode = true
                capitalizeNextFinglish = true
                english = false
                symbols = false
            }
            renderContent()
        })
        bottom.addView(actionKey("۱۲۳", .8f, pink) { symbols = !symbols; renderContent() })
        bottom.addView(actionKey("فاصله", 3.2f) { commitText(" ") })
        bottom.addView(actionKey("،", .8f) { commitText("،") })
        bottom.addView(actionKey("↵", 1f, pink) { sendEnter() })
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
        val ic = currentInputConnection ?: return
        if (!finglishMode) {
            ic.commitText(value, 1)
            composing += value
            updateFinglishPreview()
            return
        }

        if (value == " " || value == "\n" || value in listOf(".", ",", ":", "!", "?", "،", "؛", "؟")) {
            if (composing.isNotEmpty()) {
                ic.commitText(Transliterator.toFinglish(composing), 1)
                composing = ""
                updateFinglishPreview()
            }
            if (value in listOf(".", ",", ":")) {
                if (ic.getTextBeforeCursor(1, 0)?.toString() == " ") ic.deleteSurroundingText(1, 0)
                ic.commitText(value + " ", 1)
                capitalizeNextFinglish = value == "." || value == ":"
            } else {
                ic.commitText(value, 1)
                if (value == "\n") capitalizeNextFinglish = true
            }
            return
        }

        val startsNewWord = composing.isEmpty()
        composing += value
        var converted = Transliterator.toFinglish(composing)
        if (startsNewWord && capitalizeNextFinglish) {
            val firstLetter = converted.indexOfFirst { it.isLetter() }
            if (firstLetter >= 0) {
                converted = converted.replaceRange(firstLetter, firstLetter + 1, converted[firstLetter].uppercaseChar().toString())
                capitalizeNextFinglish = false
            }
        }
        ic.setComposingText(converted, 1)
        updateFinglishPreview()
    }

    private fun deleteOne() {
        val ic = currentInputConnection ?: return
        if (finglishMode && composing.isNotEmpty()) {
            composing = composing.dropLast(1)
            if (composing.isEmpty()) ic.setComposingText("", 1)
            else ic.setComposingText(Transliterator.toFinglish(composing), 1)
            updateFinglishPreview()
        } else {
            ic.deleteSurroundingText(1, 0)
            if (composing.isNotEmpty()) composing = composing.dropLast(1)
            updateFinglishPreview()
        }
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

    private fun actionKey(label: String, weight: Float, color: Int? = null, action: () -> Unit) =
        TextView(this).apply {
            text = label
            textSize = 13f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(if (label.startsWith("فینگلیش")) bg else white)
            gravity = Gravity.CENTER
            background = rounded(color ?: if (label.startsWith("فینگلیش")) pink else keyBg, 9)
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
