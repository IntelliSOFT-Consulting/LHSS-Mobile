package com.intellisoft.lhss25.clinical_info.viewmodel

import android.util.Log
import android.widget.Spinner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ClinicalInfoViewViewModel : ViewModel() {
    // LiveData to hold the spinner selection
    private val _selectedItem = MutableLiveData<String>()
    private val _rootViewSpinner = MutableLiveData<Spinner>()
    val selectedItem: LiveData<String> get() = _selectedItem
    val rootViewSpinner: LiveData<Spinner> get() = _rootViewSpinner

    // Method to update the selected item in LiveData
    fun updateSelectedItem(item: String, rootViewParent: Spinner) {
        Log.e("ClinicalInfoViewViewModel", "Selected item updated: $item")
        _selectedItem.value = item
        _rootViewSpinner.value = rootViewParent
    }
}