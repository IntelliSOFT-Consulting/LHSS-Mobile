package com.intellisoft.lhss25.dynamic_components

import android.view.View
import android.widget.TextView

// Interface for customizing TextView (label)
interface LabelCustomizer {
    fun applyCustomization(textView: TextView)
}

// Interface for creating input fields (SRP)
interface FieldCreator {
    fun createField(
        label: String,
        isMandatory: Boolean = false,
        inputType: Int?,
        isEnable: Boolean = true,
        isPastDate: Boolean = true,
        startDate: String? = null,
        endDate: String? = null,
        hint: String? = null,
    ): View
}

