package com.intellisoft.lhss25.dynamic_components

import android.content.Context
import android.widget.LinearLayout
import android.widget.TextView


class FieldManager(
    private val labelCustomizer: LabelCustomizer,
    private val context: Context) {

    fun addTextView(
        label: String,
        isMandatory: Boolean,
        parentLayout: LinearLayout) {
        val labelText = if (isMandatory) "$label *" else label
        val textView = TextView(context).apply {
            text = labelText
        }
        labelCustomizer.applyCustomization(textView)
        parentLayout.addView(textView)
    }

    fun addField(
        fieldCreator: FieldCreator,
        label: String,
        isMandatory: Boolean,
        parentLayout: LinearLayout,
        inputType: Int?,
        isEnabled: Boolean = true,
        isPastDate: Boolean = true,
        startDate: String? = null,
        endDate: String? = null,
        hintValue: String? = null
        ) {

        val field = fieldCreator.createField(
            label,
            isMandatory,
            inputType,
            isEnabled,
            isPastDate,
            startDate,
            endDate,
            hintValue
        )
        parentLayout.addView(field)
    }

}

