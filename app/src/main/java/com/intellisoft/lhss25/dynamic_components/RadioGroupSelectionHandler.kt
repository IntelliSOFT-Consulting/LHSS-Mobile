package com.intellisoft.lhss25.dynamic_components

interface RadioGroupSelectionHandler {
    fun handleSelection(radioGroup: MandatoryRadioGroup, updateSelectedItem: (String) -> Unit)
}

class DefaultRadioGroupSelectionHandler : RadioGroupSelectionHandler {
    override fun handleSelection(radioGroup: MandatoryRadioGroup, updateSelectedItem: (String) -> Unit) {
        // Set a listener on the RadioGroup to handle selection changes

        val selectedText = radioGroup.getSelectedRadioButtonText()
        if (selectedText != null) {
            updateSelectedItem(selectedText)
        }

    }
}

