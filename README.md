# Finglish Keyboard

A native Android Persian keyboard with a live Persian-to-Finglish transliteration bar.

## Features
- Android Input Method Service (IME), not just an in-app text field
- Persian character rows
- Live transliteration preview while composing
- Tap the suggestion to replace composing Persian text with Finglish
- Backspace, space, and enter keys

## Build
Open this repository in Android Studio (Hedgehog or newer) with JDK 17 and Android SDK 35. Let Gradle sync, then build the app configuration.

## Install and enable
1. Install the generated debug APK on an Android device.
2. Open Finglish Keyboard.
3. Tap Activate keyboard and enable Finglish Keyboard in Android system settings.
4. Return and tap Select keyboard, then choose Finglish Keyboard.
5. Open any text field and switch to this keyboard.

## Transliteration note
The first version uses a simple character-based transliteration map. Persian short vowels are not written in ordinary text, so transliteration can be ambiguous. This is a practical starter, not a language-model-powered dictionary.

## Project files
- FinglishImeService.kt: Android IME and keyboard UI
- Transliterator.kt: Persian-to-Latin mapping
- MainActivity.kt: activation and keyboard picker instructions
