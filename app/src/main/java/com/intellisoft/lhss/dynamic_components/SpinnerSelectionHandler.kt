package com.intellisoft.lhss.dynamic_components

import android.view.View
import android.widget.AdapterView
import android.widget.Spinner

interface SpinnerSelectionHandler {
    fun handleSelection(spinner: Spinner, updateSelectedItem: (String) -> Unit)
}

class DefaultSpinnerSelectionHandler : SpinnerSelectionHandler {
    override fun handleSelection(spinner: Spinner, updateSelectedItem: (String) -> Unit) {
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedItem = parent.getItemAtPosition(position) as String
                updateSelectedItem(selectedItem)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Handle case where no item is selected
            }
        }
    }
}
