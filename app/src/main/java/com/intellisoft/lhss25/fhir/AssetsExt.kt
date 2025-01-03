package com.intellisoft.lhss25.fhir


import android.content.Context

fun Context.readFileFromAssets(fileName: String): String =
    assets.open(fileName).bufferedReader().use { it.readText() }