package com.intellisoft.lhss.utils

import com.google.android.material.textfield.TextInputEditText

class AppUtils {
     fun capitalizeFirstLetter(sentence: String): String {
        val words = sentence.split(" ")

        val capitalizedWords = words.map { it.capitalize() }

        return capitalizedWords.joinToString(" ")
    }
    fun disableEditing(editText: TextInputEditText) {
        editText.keyListener = null
        editText.isCursorVisible = false
        editText.isFocusable = false
//        editText.isEnabled = false
    }
}