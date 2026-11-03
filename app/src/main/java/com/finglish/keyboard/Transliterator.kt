package com.finglish.keyboard

object Transliterator {
 private val letters = mapOf('ا' to "a",'آ' to "â",'ب' to "b",'پ' to "p",'ت' to "t",'ث' to "s",'ج' to "j",'چ' to "ch",'ح' to "h",'خ' to "kh",'د' to "d",'ذ' to "z",'ر' to "r",'ز' to "z",'ژ' to "zh",'س' to "s",'ش' to "sh",'ص' to "s",'ض' to "z",'ط' to "t",'ظ' to "z",'ع' to "'",'غ' to "gh",'ف' to "f",'ق' to "gh",'ک' to "k",'گ' to "g",'ل' to "l",'م' to "m",'ن' to "n",'و' to "o",'ه' to "h",'ی' to "i",'ي' to "i",'ك' to "k",'ء' to "'",'ؤ' to "o",'ئ' to "i",'ة' to "e",'۰' to "0",'۱' to "1",'۲' to "2",'۳' to "3",'۴' to "4",'۵' to "5",'۶' to "6",'۷' to "7",'۸' to "8",'۹' to "9")
 fun toFinglish(text: String): String = buildString { text.forEach { append(letters[it] ?: it) } }
}
