package com.intellisoft.lhss.registration.viewmodel

import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.intellisoft.lhss.shared.DbFormData

class DemographicsViewModel : ViewModel() {

    // LiveData that holds the latest DbFormData
    private val _formDataLiveData = MutableLiveData<DbFormData>()
    val formDataLiveData: LiveData<DbFormData> = _formDataLiveData

    // This will be used to keep track of form data
    private val formDataList = ArrayList<DbFormData>()

    // Extracts the form data and updates LiveData whenever a spinner changes
    fun extractFormData(rootLayout: LinearLayout) {
        // Traverse through all child views of rootLayout
        for (i in 0 until rootLayout.childCount) {
            when (val childView = rootLayout.getChildAt(i)) {

                is RadioGroup -> {
                    // Get the selected RadioButton ID
                    val selectedRadioButtonId = childView.checkedRadioButtonId
                    if (selectedRadioButtonId != -1) {
                        // Find the selected RadioButton using the selected ID
                        val selectedRadioButton = childView.findViewById<RadioButton>(selectedRadioButtonId)
                        val selectedText = selectedRadioButton.text.toString()

                        val tag = childView.tag
                        if (tag != null && selectedText.isNotEmpty()) {
                            val formData = DbFormData(tag.toString(), selectedText)

                            // Update the formDataList with the new entry
                            formDataList.add(formData)

                            // Set the latest selected item as LiveData
                            _formDataLiveData.value = formData
                        }
                    }
                }


//                is Spinner -> {
//                    // Add an event listener for when the spinner value is changed
//                    childView.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//                        override fun onItemSelected(
//                            parent: AdapterView<*>,
//                            view: View?,
//                            position: Int,
//                            id: Long
//                        ) {
//                            // Get the updated text when a new item is selected
//                            val updatedText = parent.getItemAtPosition(position)
//
//                            val tag = childView.tag
//                            if (tag != null && updatedText != null) {
//                                val formData = DbFormData(tag.toString(), updatedText.toString())
//
//                                // Update the formDataList with the new entry
//                                formDataList.add(formData)
//
//                                // Set the latest selected item as LiveData
//                                _formDataLiveData.value = formData
//                            }
//                        }
//
//                        override fun onNothingSelected(parent: AdapterView<*>) {
//                            // Handle case where no item is selected, if necessary
//                        }
//                    }
//                }
            }
        }
    }

}