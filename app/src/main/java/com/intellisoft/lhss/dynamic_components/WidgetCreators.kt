package com.intellisoft.lhss.dynamic_components

import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Color
import android.text.InputType
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.intellisoft.lhss.R
import java.util.Calendar

/**
 * This contains all the dynamic Widgets
 */

// Concrete implementation for label customization (OCP)
class DefaultLabelCustomizer : LabelCustomizer {
    override fun applyCustomization(
        textView: TextView
    ) {
        // Ensure that the TextView has layoutParams
        val layoutParams = textView.layoutParams ?: ViewGroup.MarginLayoutParams(
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        ) .apply {
            setMargins(8,8,8,0)
        }

        textView.layoutParams = layoutParams
        textView.textSize = 18f

        // Set text color and padding
        textView.setTextColor(Color.BLACK)
        textView.setPadding(16, 16, 16, 16)
    }
}

// Concrete implementation for creating an EditText field
class EditTextFieldCreator(
    private val context: Context
) : FieldCreator {
    override fun createField(
        label: String,
        isMandatory: Boolean,
        inputType: Int?,
        isEnable: Boolean,
        isPastDate: Boolean
    ): View {
        // LinearLayout to hold both Country Code Picker and EditText
        val containerLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(8, 5, 8, 12)
            }

        }

        // Country Code Picker (CCP) if input type is phone number
        val countryCodePicker = com.hbb20.CountryCodePicker(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 16, 0)
            }

            // Set default country code (e.g., Kenya)
            setDefaultCountryUsingNameCode("ke")  // Set using country code, like "KE" for Kenya
            // Or alternatively, you can set by phone code:
             setCountryForPhoneCode(254) // Set using phone code for Kenya
        }

        // EditText field
        val editText = MandatoryEditText(context).apply {
            this.hint = label
            this.inputType = inputType ?: InputType.TYPE_CLASS_TEXT
            this.background = ContextCompat.getDrawable(context, R.drawable.rounded_edittext) // Set rounded border
            this.tag = label
            this.isMandatory = isMandatory
            this.isEnabled = isEnable
            this.layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                weight = 1f // Let EditText take up remaining space after Country Code Picker
            }
            this.setPadding(32, 16, 20, 16)
        }

        // Add country code picker only if the input type is for a phone number
        if (inputType == InputType.TYPE_CLASS_PHONE) {
            containerLayout.addView(countryCodePicker)
        }

        // Add the EditText to the container
        containerLayout.addView(editText)

        return containerLayout
    }
}


// Concrete implementation for creating a Spinner field (LSP)
class SpinnerFieldCreator(
    private val options: List<String>,
    private val context: Context) : FieldCreator {
    override fun createField(
        label: String,
        isMandatory: Boolean,
        inputType: Int?,
        isEnabled: Boolean,
        isPastDate: Boolean
    ): View {

        val spinner = Spinner(context)
        spinner.tag = label
        val adapter = ArrayAdapter(context, R.layout.simple_spinner_item, options)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        return spinner
    }
}

// Concrete implementation for creating a Radio button field (LSP)
class RadioButtonFieldCreator(
    private val optionList: List<String>,
    private val context: Context,
    private val isHorizontal: Boolean = true // Default is vertical
) : FieldCreator {
    override fun createField(
        label: String,
        isMandatory1: Boolean,
        inputType: Int?,
        isEnabled: Boolean,
        isPastDate: Boolean
    ): View {
        // Create a RadioGroup
        val radioGroup = MandatoryRadioGroup(context).apply {
            // Set orientation to horizontal
            orientation = RadioGroup.HORIZONTAL
            tag = label
            isMandatory = isMandatory1
        }

        // Dynamically add RadioButtons to the RadioGroup
        optionList.forEach { option ->
            val radioButton = RadioButton(context).apply {
                text = option
                tag = label
            }
            radioGroup.addRadioButton(radioButton)
//            radioGroup.addView(radioButton)
        }

        // Return the RadioGroup as the view
        return radioGroup
    }
}



//Concrete implementation for creating a Datepicker
class DatePickerFieldCreator(private val context: Context) : FieldCreator {

    override fun createField(
        label: String,
        isMandatory: Boolean,
        inputType: Int?,
        isEnabled: Boolean,
        isPastDate: Boolean
    ): View {

        // Create an EditText field to display the selected date
        val dateEditText = EditText(context).apply {
            isFocusable = false // Prevent typing
            background = ContextCompat.getDrawable(context, R.drawable.rounded_edittext)
            hint = if (isMandatory) "$label *" else label

            // Set drawable to the right (use 0 for other positions if no drawable is needed)
            val rightIcon = ContextCompat.getDrawable(context, R.drawable.ic_action_date) // Your drawable resource
            setCompoundDrawablesWithIntrinsicBounds(null, null, rightIcon, null)

            setOnClickListener {
                // Show DatePickerDialog on click
                showDatePickerDialog(this, isPastDate)
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(8,5,8,12)
            }
            this.setPadding(32,16,20,16)
        }

        return dateEditText
    }

    // Function to show DatePickerDialog
    private fun showDatePickerDialog(editText: EditText, isPast: Boolean = true) {
        // Get the current date
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        // Create DatePickerDialog
        val datePickerDialog = DatePickerDialog(context, { _, selectedYear, selectedMonth, selectedDay ->
            // Format and set the selected date to EditText
            val selectedDate = "${selectedDay.toString().padStart(2, '0')}/" +
                    "${(selectedMonth + 1).toString().padStart(2, '0')}/$selectedYear"
            editText.setText(selectedDate)
        }, year, month, day)

        // Block off future dates if isPast is true
        if (isPast) {
            datePickerDialog.datePicker.maxDate = calendar.timeInMillis
        }else{
            // Block off past dates if isPast is false
            datePickerDialog.datePicker.minDate = 0
        }

        // Show the dialog
        datePickerDialog.show()
    }
}

class CheckboxFieldCreator(
    private val context: Context
) : FieldCreator {
    override fun createField(
        label: String,
        isMandatory: Boolean,
        inputType: Int?,
        isEnabled: Boolean,
        isPastDate: Boolean
    ): View {

        // Create a LinearLayout to hold the checkbox and label
        val linearLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // Create the CheckBox
        val checkBox = CheckBox(context).apply {
            isChecked = false
            tag = label
        }

        // Create the label TextView
        val textView = TextView(context).apply {
            text = label
            setPadding(8, 0, 0, 0) // Padding to space out the label from the checkbox
        }

        // Add the CheckBox and the label to the layout
        linearLayout.addView(checkBox)
        linearLayout.addView(textView)

        return linearLayout
    }
}

