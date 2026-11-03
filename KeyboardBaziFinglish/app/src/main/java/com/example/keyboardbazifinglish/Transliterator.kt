package com.example.keyboardbazifinglish
object Transliterator {
 private val map = mapOf('ا' to "a",'آ' to "aa",'ب' to "b",'پ' to "p",'ت' to "t",'ث' to "s",'ج' to "j",'چ' to "ch",'ح' to "h",'خ' to "kh",'د' to "d",'ذ' to "z",'ر' to "r",'ز' to "z",'ژ' to "zh",'س' to "s",'ش' to "sh",'ص' to "s",'ض' to "z",'ط' to "t",'ظ' to "z",'ع' to "'",'غ' to "gh",'ف' to "f",'ق' to "gh",'ک' to "k",'گ' to "g",'ل' to "l",'م' to "m",'ن' to "n",'و' to "v",'ه' to "h",'ی' to "i")
 fun convert(s:String)=buildString { s.forEach { append(map[it] ?: it.toString()) } }
}