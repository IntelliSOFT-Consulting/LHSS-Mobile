package com.intellisoft.lhss.shared

import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

class MainActivityViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel(){
    // LiveData to hold the selected radio button's value
    private val _radioSelectedOption = MutableLiveData<DbFormData>()
    val radioSelectedOption: LiveData<DbFormData> get() = _radioSelectedOption


    private val _missingFields = MutableLiveData<List<DbFormData>>()
    val missingFields: LiveData<List<DbFormData>> = _missingFields

    private val _formData = MutableLiveData<Map<String, String>>()
    val formData: LiveData<Map<String, String>> get() = _formData

    fun updateFormData(addedField: ArrayList<DbFormData>) {

        addedField.forEach {
            val tag = it.tag
            val text = it.text

            val currentData = _formData.value?.toMutableMap() ?: mutableMapOf()
            currentData[tag] = text
            _formData.value = currentData
        }

    }

    fun updateMissingFields(missingFields: List<DbFormData>) {
        _missingFields.value = missingFields
    }

    // Method to update LiveData when a new option is selected
    fun updateSelectedOption(option: DbFormData) {
        _radioSelectedOption.value = option
    }

    // Function to extract form data, including radio button selections
    fun extractFormData(rootLayout: LinearLayout) {
        // Traverse through all child views of rootLayout
        for (i in 0 until rootLayout.childCount) {
            when (val childView = rootLayout.getChildAt(i)) {
                is RadioGroup -> {
                    // Set a listener to detect changes in RadioGroup selections
                    childView.setOnCheckedChangeListener { group, checkedId ->
                        val selectedRadioButton = group.findViewById<RadioButton>(checkedId)
                        val selectedText = selectedRadioButton.text.toString()
                        val tag = childView.tag

                        if (tag != null && selectedText.isNotEmpty()) {
                            val formData = DbFormData(tag.toString(), selectedText)
                            // Update LiveData with the new radio button selection
                            updateSelectedOption(formData)
                        }
                    }
                }

            }
        }
    }


}