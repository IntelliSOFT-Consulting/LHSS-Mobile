package com.intellisoft.lhss25.dynamic_components

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.DatePicker
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import com.google.gson.Gson
import com.intellisoft.lhss25.shared.DbFormData
import com.intellisoft.lhss25.shared.DbField
import com.intellisoft.lhss25.shared.DbWidgets
import com.intellisoft.lhss25.shared.FormData
import com.intellisoft.lhss25.shared.FormatterClass

object FormUtils {

    fun populateView(
        dbFieldList: ArrayList<DbField>,
        rootLayout: LinearLayout,
        fieldManager: FieldManager,
        context: Context
    ) {

        dbFieldList.forEach { field ->
            val widgetType = field.widgets
            val optionList = field.optionList
            val label = field.label
            val isMandatory = field.isMandatory
            val isEnabled = field.isEnabled
            val inputType = field.inputType
            val isPastDate = field.isPastDate
            val startDate = field.startDate
            val endDate = field.endDate
            val hintValue = field.hint

            // Look for an existing view with the same label as the tag in the root layout
            var existingView: View? = null
            for (i in 0 until rootLayout.childCount) {
                val child = rootLayout.getChildAt(i)
                if (child.tag == label) {
                    existingView = child
                    break
                }
            }

            // Update the existing view or create a new one
            if (existingView != null) {
                // Update the existing widget
                when (existingView) {
                    is EditText -> {
                        EditTextFieldCreator(context)
                    }
                    is Spinner -> {
                        if (widgetType == DbWidgets.SPINNER.name) {
                            SpinnerFieldCreator(
                                optionList ,
                                context
                            )
                        }
                    }
                    is RadioButton -> {
                        RadioButtonFieldCreator(
                            optionList,
                            context,
                            isHorizontal = true)
                    }
                    is DatePicker -> {
                        DatePickerFieldCreator(
                            context
                        )
                    }
                    is CheckBox -> {
                        CheckboxFieldCreator(
                            context
                        )
                    }
                }
            } else {
                // Create a new widget if it doesn't exist
                // Add the label as a TextView
                fieldManager.addTextView(label, isMandatory, rootLayout)

                val newField = when (widgetType) {
                    DbWidgets.EDIT_TEXT.name -> {
                        EditTextFieldCreator(context)
                    }
                    DbWidgets.SPINNER.name -> {
                        SpinnerFieldCreator(
                            optionList ,
                            context
                        )
                    }
                    DbWidgets.RADIO_BUTTON.name -> {
                        RadioButtonFieldCreator(
                            optionList ,
                            context,
                            isHorizontal = true
                        )
                    }
                    DbWidgets.DATE_PICKER.name -> {
                        DatePickerFieldCreator(
                            context
                        )
                    }
                    DbWidgets.CHECK_BOX.name -> {
                        CheckboxFieldCreator(
                            context
                        )
                    }

                    else -> {
                        null
                    }
                }

                // Add the new widget to the layout
                if (newField != null) {
                    fieldManager.addField(
                        newField,
                        label,
                        isMandatory,
                        rootLayout,
                        inputType,
                        isEnabled,
                        isPastDate,
                        startDate,
                        endDate,
                        hintValue
                    )
                }
            }
        }
    }

    fun removeWidgetByTag(parentLayout: LinearLayout, tag: String) {
        // Loop through the children of the parent layout
        for (i in 0 until parentLayout.childCount) {
            val childView = parentLayout.getChildAt(i)
            // Check if the child's tag matches the tag to be removed
            if (childView.tag == tag) {
                // Remove the view from the parent layout
                parentLayout.removeView(childView)
                break // Exit the loop once the view is removed
            }
        }
    }

    fun extractAllFormData(rootLayout: LinearLayout):
            Pair<ArrayList<DbFormData>, ArrayList<DbFormData>> {

        val missingFields = ArrayList<DbFormData>()
        val addedFields = ArrayList<DbFormData>()

        // Traverse through all child views of rootLayout
        for (i in 0 until rootLayout.childCount) {
            val childView = rootLayout.getChildAt(i)

            if (childView.visibility == View.VISIBLE) { // Only process visible views

                when (childView) {

                    is MandatoryRadioGroup -> {

                        val selectedText = childView.getSelectedRadioButtonText()
                        val isMandatory = childView.isMandatory
                        val tag = childView.tag?.toString() ?: ""

                        if (isMandatory){
                            if (tag.isNotEmpty() && selectedText != null){
                                val formData = DbFormData(tag, selectedText)
                                addedFields.add(formData)
                            }else{
                                // Add missing mandatory fields if text is empty
                                val tagText = if (tag=="DOB_SELECTION") "Date of Birth" else tag
                                missingFields.add(DbFormData(tagText, ""))
                            }
                        }else{
                            if (tag.isNotEmpty() && selectedText != null) {
                                val formData = DbFormData(tag, selectedText)
                                addedFields.add(formData)
                            }
                        }
                    }

                    is MandatoryEditText -> {
                        val tag = childView.tag?.toString() ?: ""
                        val text = childView.text.toString()
                        val isMandatory = childView.isMandatory

                        if (isMandatory){
                            if (tag.isNotEmpty() && text.isNotEmpty()){

                                val formData = DbFormData(tag, text)
                                addedFields.add(formData)
                            }else{
                                // Add missing mandatory fields if text is empty
                                missingFields.add(DbFormData(tag, ""))
                            }
                        }else{
                            if (tag.isNotEmpty() && text.isNotEmpty()) {
                                val formData = DbFormData(tag, text)
                                addedFields.add(formData)
                            }
                        }

                    }

                    is Spinner -> {
                        val selectedText = childView.selectedItem.toString()
                        val tag = childView.tag?.toString() ?: ""

                        if (tag.isNotEmpty() && selectedText.isNotEmpty()) {
                            val formData = DbFormData(tag, selectedText)
                            addedFields.add(formData)
                        } else if (tag.isNotEmpty()) {
                            // Add missing mandatory fields if text is empty
                            missingFields.add(DbFormData(tag, ""))
                        }
                    }

                    is CheckBox -> {
                        val tag = childView.tag?.toString() ?: ""
                        val isChecked = childView.isChecked

                        if (tag.isNotEmpty()) {
                            val formData = DbFormData(tag, if (isChecked) "Checked" else "Unchecked")
                            addedFields.add(formData)
                        }
                    }

                    is TextView -> {
                        val text = childView.text.toString()
                        val tag = childView.tag?.toString()?: ""

                        if (tag.isNotEmpty() && text.isNotEmpty()) {
                            val formData = DbFormData(tag, text)
                            addedFields.add(formData)
                        }
                    }

                    // New case for extracting phone number with country code
                    is LinearLayout -> {
                        // Initialize variables for CountryCodePicker and EditText
                        var countryCodePicker: com.hbb20.CountryCodePicker? = null
                        var editText: MandatoryEditText? = null

                        // Loop through the children of this LinearLayout to find CountryCodePicker and EditText
                        for (j in 0 until childView.childCount) {
                            val innerChild = childView.getChildAt(j)


                            // Check the type of each inner child
                            when (innerChild) {
                                is com.hbb20.CountryCodePicker -> {
                                    countryCodePicker = innerChild
                                }
                                is MandatoryEditText -> {
                                    editText = innerChild
                                }
                            }
                        }

                        // Process phone number fields with CountryCodePicker and EditText
                        if (countryCodePicker != null && editText != null) {
                            val countryCode = countryCodePicker.selectedCountryCodeWithPlus // Get the country code with the plus sign
                            val phoneNumber = editText.text.toString() // Get the entered phone number
                            val tag = editText.tag?.toString() ?: "" // Get the tag for identifying the field

                            // Check if the field is mandatory and has a tag containing "Telephone"
                            if (editText.isMandatory) {
                                if (tag.isNotEmpty() && phoneNumber.isNotEmpty() && tag.contains("Telephone", true)) {
                                    // Combine country code and phone number
                                    val fullPhoneNumber = "$countryCode$phoneNumber"
                                    val formData = DbFormData(tag, fullPhoneNumber)
                                    addedFields.add(formData)
                                } else {
                                    // Add missing mandatory field if phone number is empty
                                    missingFields.add(DbFormData(tag, ""))
                                }
                            } else {
                                // For non-mandatory fields, check if tag and phone number are not empty
                                if (tag.isNotEmpty() && phoneNumber.isNotEmpty() && tag.contains("Telephone", true)) {
                                    // Combine country code and phone number
                                    val fullPhoneNumber = "$countryCode$phoneNumber"
                                    val formData = DbFormData(tag, fullPhoneNumber)
                                    addedFields.add(formData)
                                }
                            }
                        }else if (editText != null) {
                            val tag = editText.tag?.toString() ?: ""
                            val text = editText.text.toString()

                            // Check if the field is mandatory
                            if (editText.isMandatory) {
                                if (tag.isNotEmpty() && text.isNotEmpty()) {
                                    val formData = DbFormData(tag, text)
                                    addedFields.add(formData)
                                } else {
                                    // Add missing mandatory fields if text is empty
                                    missingFields.add(DbFormData(tag, ""))
                                }
                            } else {
                                if (tag.isNotEmpty() && text.isNotEmpty()) {
                                    val formData = DbFormData(tag, text)
                                    addedFields.add(formData)
                                }
                            }
                        }
                    }

                    // Add more cases as needed based on widget types
                }
            }
        }

        return Pair(ArrayList(addedFields), ArrayList(missingFields))


    }



    fun populateFormData(
        formDataList: ArrayList<FormData>,
        parentLayout: LinearLayout,
        context: Context
    ) {

        val formatterClass = FormatterClass(context)


        formDataList.forEach { formData ->
            formData.formDataList.forEach { dbFormData ->
                // Find the widget by its tag in the parent layout
                val view = parentLayout.findViewWithTag<View>(dbFormData.tag)




                // Check for the type of view and set the appropriate text/value
                when (view) {
                    is EditText -> {
                        //Set Edittext bold
                        val text = dbFormData.text

                        val pairNull = formatterClass.parsePhoneNumber(text)
                        if (pairNull == null){
                            view.setText(text)
                        }else{
                            view.setText(pairNull.second)
                        }

                    }
                    is Spinner -> {
                        // Assuming your spinner has an ArrayAdapter with strings, find the correct position
                        val adapter = view.adapter as ArrayAdapter<String>
                        val position = adapter.getPosition(dbFormData.text)
                        if (position >= 0) {
                            view.setSelection(position)
                        }
                    }
                    is RadioGroup -> {
                        // Assuming the tag refers to the RadioGroup, and dbFormData.text refers to the text of a RadioButton
                        for (i in 0 until view.childCount) {
                            val radioButton = view.getChildAt(i) as RadioButton
                            if (radioButton.text == dbFormData.text) {
                                radioButton.isChecked = true
                                break
                            }
                        }
                    }




                    // Add handling for other types of widgets here as needed
                    else -> {
                        // Optionally handle other widget types
                    }
                }
            }
        }
    }

    fun loadFormData(
        context: Context,
        rootLayout: LinearLayout,
        navigationDetails:String,
        fragmentClass:String
    ) {

        val formatterClass = FormatterClass(context)

        val gson = Gson()
        val formDataList = ArrayList<FormData>()

        val savedJson = formatterClass.getSharedPref(
            navigationDetails,
            fragmentClass
        )
        if (savedJson != null){
            val formDataFromJson = gson.fromJson(savedJson, FormData::class.java)
            if (formDataFromJson != null){
                formDataList.addAll(listOf(formDataFromJson))
                if (formDataList.isNotEmpty()){
                    populateFormData(formDataList, rootLayout, context)
                }
            }
        }

    }



}