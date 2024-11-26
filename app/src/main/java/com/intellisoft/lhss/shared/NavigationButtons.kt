package com.intellisoft.lhss.shared

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import com.intellisoft.lhss.R

// NavigationButtons.kt
class NavigationButtons @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val backButton: Button
    private val nextButton: Button

    init {
        LayoutInflater.from(context)
            .inflate(
                R.layout.layout_navigation_buttons,
                this, true)

        backButton = findViewById(R.id.backButton)
        nextButton = findViewById(R.id.nextButton)
    }

    fun setBackButtonClickListener(listener: OnClickListener) {
        backButton.setOnClickListener(listener)
    }

    fun setNextButtonClickListener(listener: OnClickListener) {
        nextButton.setOnClickListener(listener)
    }

    fun setNextButtonText(text: String) {
        nextButton.text = text
    }
}
