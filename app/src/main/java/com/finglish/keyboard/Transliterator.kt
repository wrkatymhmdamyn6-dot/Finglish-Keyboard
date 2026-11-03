package com.finglish.keyboard

object Transliterator {
    private val words = mapOf(
        "سلام" to "salaam",
        "سلامی" to "salami",
        "خوبی" to "khubi",
        "خوبم" to "khubam",
        "خوب" to "khub",
        "چطوری" to "chetori",
        "چطوره" to "chetore",
        "ممنون" to "mamnoon",
        "مرسی" to "merci",
        "خداحافظ" to "khodahafez",
        "خدا" to "khoda",
        "عزیزم" to "azizam",
        "دوست" to "doost",
        "دوستت" to "dooset",
        "دارم" to "daram",
        "داری" to "dari",
        "دارید" to "darid",
        "هستم" to "hastam",
        "هستی" to "hasti",
        "هست" to "hast",
        "نیستم" to "nistam",
        "بله" to "bale",
        "آره" to "are",
        "اره" to "are",
        "نه" to "na",
        "چی" to "chi",
        "چرا" to "chera",
        "کجا" to "koja",
        "کی" to "key",
        "امروز" to "emrooz",
        "فردا" to "farda",
        "شب" to "shab",
        "روز" to "rooz",
        "بازی" to "bazi",
        "بیا" to "bia",
        "بریم" to "berim",
        "برو" to "boro",
        "ببین" to "bebin",
        "لطفا" to "lotfan",
        "لطفاً" to "lotfan",
        "عالی" to "aali",
        "خیلی" to "kheyli",
        "قربونت" to "ghorboonet"
    )

    private val letters = mapOf(
        'ا' to "a", 'آ' to "aa", 'ب' to "b", 'پ' to "p", 'ت' to "t",
        'ث' to "s", 'ج' to "j", 'چ' to "ch", 'ح' to "h", 'خ' to "kh",
        'د' to "d", 'ذ' to "z", 'ر' to "r", 'ز' to "z", 'ژ' to "zh",
        'س' to "s", 'ش' to "sh", 'ص' to "s", 'ض' to "z", 'ط' to "t",
        'ظ' to "z", 'ع' to "'", 'غ' to "gh", 'ف' to "f", 'ق' to "gh",
        'ک' to "k", 'گ' to "g", 'ل' to "l", 'م' to "m", 'ن' to "n",
        'و' to "o", 'ه' to "h", 'ی' to "i", 'ئ' to "y", 'ء' to "'",
        'ة' to "e", 'ؤ' to "o"
    )

    fun toFinglish(text: String): String {
        val normalized = text.replace('ي', 'ی').replace('ك', 'ک')
        val tokenRegex = Regex("[\\u0600-\\u06FF]+|[^\\u0600-\\u06FF]+")
        return tokenRegex.findAll(normalized).joinToString("") { match ->
            val token = match.value
            if (token.firstOrNull()?.let { it in '\u0600'..'\u06FF' } != true) token
            else words[token] ?: buildString {
                token.forEach { ch -> append(letters[ch] ?: ch.toString()) }
            }
        }
    }
}
