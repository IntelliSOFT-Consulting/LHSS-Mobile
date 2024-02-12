package com.intellisoft.lhss.utils

class AppUtils {
     fun capitalizeFirstLetter(sentence: String): String {
        val words = sentence.split(" ")

        val capitalizedWords = words.map { it.capitalize() }

        return capitalizedWords.joinToString(" ")
    }
}