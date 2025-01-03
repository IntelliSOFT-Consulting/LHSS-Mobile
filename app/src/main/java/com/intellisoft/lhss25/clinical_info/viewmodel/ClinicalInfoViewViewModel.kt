package com.intellisoft.lhss25.clinical_info.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ClinicalInfoViewViewModel : ViewModel() {
    // LiveData to hold the spinner selection
    private val _selectedItem = MutableLiveData<String>()
    val selectedItem: LiveData<String> get() = _selectedItem

    // Method to update the selected item in LiveData
    fun updateSelectedItem(item: String) {
        Log.e("ClinicalInfoViewViewModel", "Selected item updated: $item")
        _selectedItem.value = item
    }
}