package com.intellisoft.lhss25.dynamic_components

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatRadioButton
import com.intellisoft.lhss25.R

class MandatoryEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.editTextStyle
) : AppCompatEditText(context, attrs, defStyleAttr) {

    var isMandatory: Boolean = false

    init {
        // If you want to read the attribute from XML, you can do it here
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.MandatoryWidget, 0, 0)
            isMandatory = typedArray.getBoolean(R.styleable.MandatoryWidget_isMandatory, false)
            typedArray.recycle()
        }
    }

    // Optionally, add any additional behavior based on the isMandatory property
}

class MandatorySpinner @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : androidx.appcompat.widget.AppCompatSpinner(context, attrs, defStyleAttr) {

    var isMandatory: Boolean = false

    init {
        // Optional: Read custom XML attributes for isMandatory if necessary
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.MandatoryWidget, 0, 0)
            isMandatory = typedArray.getBoolean(R.styleable.MandatoryWidget_isMandatory, false)
            typedArray.recycle()
        }
    }

    // You can also add validation logic if needed
}

class MandatoryRadioButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.radioButtonStyle
) : AppCompatRadioButton(context, attrs, defStyleAttr) {

    var isMandatory: Boolean = false

    init {
        // Optional: Read custom XML attributes for isMandatory
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.MandatoryWidget, 0, 0)
            isMandatory = typedArray.getBoolean(R.styleable.MandatoryWidget_isMandatory, false)
            typedArray.recycle()
        }
    }
}

class MandatoryRadioGroup @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    var isMandatory: Boolean = false
    private var radioGroup: RadioGroup

    init {
        orientation = HORIZONTAL

        // Initialize the RadioGroup and add it to this custom view
        radioGroup = RadioGroup(context)
        this.addView(radioGroup)

        // Read custom XML attributes for isMandatory
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.MandatoryWidget, 0, 0)
            isMandatory = typedArray.getBoolean(R.styleable.MandatoryWidget_isMandatory, false)
            typedArray.recycle()
        }
    }

    // Function to add RadioButtons
    fun addRadioButton(radioButton: RadioButton) {
        radioGroup.addView(radioButton)
    }

    // Validation function to check if a RadioButton is selected
    fun isValid(): Boolean {
        return radioGroup.checkedRadioButtonId != -1
    }

    fun checkedRadioButtonId():Int{
        return radioGroup.checkedRadioButtonId
    }

    // Function to get the selected RadioButton's text
    fun getSelectedRadioButtonText(): String? {
        val selectedId = radioGroup.checkedRadioButtonId
        if (selectedId != -1) {
            val selectedRadioButton = radioGroup.findViewById<RadioButton>(selectedId)
            return selectedRadioButton?.text?.toString()
        }
        return null // No selection
    }
}