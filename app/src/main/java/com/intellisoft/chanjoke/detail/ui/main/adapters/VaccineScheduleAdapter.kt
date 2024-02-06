package com.intellisoft.chanjoke.detail.ui.main.adapters

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.CheckBox
import android.widget.TextView
import com.intellisoft.chanjoke.R
import com.intellisoft.chanjoke.vaccine.validations.BasicVaccine

class VaccineScheduleAdapter(
    private val context: Context,
    private val expandableListTitle: List<String>,
    private val expandableListDetail: HashMap<String, List<BasicVaccine>>,
    private val tvAdministerVaccine: TextView
) : BaseExpandableListAdapter() {

    // Maintain a map to store the checked state of each checkbox
    private val checkedStates = HashMap<Pair<Int, Int>, Boolean>()

    init {
        updateAdministerVaccineText()
    }

    override fun getChild(listPosition: Int, expandedListPosition: Int): Any {
        return expandableListDetail[expandableListTitle[listPosition]]!![expandedListPosition]
    }

    override fun getChildId(listPosition: Int, expandedListPosition: Int): Long {
        return expandedListPosition.toLong()
    }

    // Modify getChildView to handle checkbox state
    override fun getChildView(
        listPosition: Int,
        expandedListPosition: Int,
        isLastChild: Boolean,
        convertView: View?,
        parent: ViewGroup?
    ): View {
        var convertView = convertView
        val expandedListText = getChild(listPosition, expandedListPosition) as BasicVaccine
        if (convertView == null) {
            val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = layoutInflater.inflate(R.layout.vaccination_schedule_vaccines, null)
        }
        val expandedListTextView = convertView!!.findViewById<TextView>(R.id.tvVaccineName)
        val checkBox = convertView.findViewById<CheckBox>(R.id.checkbox)

        expandedListTextView.text = expandedListText.vaccineName

        // Set checkbox state based on stored checked state
        val key = Pair(listPosition, expandedListPosition)
        checkBox.isChecked = checkedStates[key] ?: false

        // Update checked state when checkbox state changes
        checkBox.setOnCheckedChangeListener { _, isChecked ->
            checkedStates[key] = isChecked
            updateAdministerVaccineText()
        }

        return convertView
    }

    // Method to update the tvAdministerVaccine TextView with the number of selected checkboxes
    private fun updateAdministerVaccineText() {
        val selectedCount = checkedStates.count { it.value }
        val value = "Administer Vaccine ($selectedCount)"
        tvAdministerVaccine.text = value
    }

    // Method to get the checked states
    // Method to get the list of selected BasicVaccine items
    fun getCheckedStates(): List<BasicVaccine> {
        val selectedVaccines = mutableListOf<BasicVaccine>()
        for ((positionPair, isChecked) in checkedStates) {
            if (isChecked) {
                val (groupPosition, childPosition) = positionPair
                val vaccine = expandableListDetail[expandableListTitle[groupPosition]]?.get(childPosition)
                vaccine?.let {
                    selectedVaccines.add(it)
                }
            }
        }
        return selectedVaccines
    }

    override fun getChildrenCount(listPosition: Int): Int {
        return expandableListDetail[expandableListTitle[listPosition]]!!.size
    }

    override fun getGroup(listPosition: Int): Any {
        return expandableListTitle[listPosition]
    }

    override fun getGroupCount(): Int {
        return expandableListTitle.size
    }

    override fun getGroupId(listPosition: Int): Long {
        return listPosition.toLong()
    }

    override fun getGroupView(
        listPosition: Int,
        isExpanded: Boolean,
        convertView: View?,
        parent: ViewGroup?
    ): View {
        var convertView = convertView
        val listTitle = getGroup(listPosition) as String
        if (convertView == null) {
            val layoutInflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = layoutInflater.inflate(R.layout.vaccination_schedule, null)
        }
        val listTitleTextView = convertView!!.findViewById<TextView>(R.id.tvScheduleTime)
        listTitleTextView.setTypeface(null, Typeface.BOLD)
        val weekNo: String = if (listTitle == "0"){
            "At Birth"
        }else{
            "$listTitle weeks"
        }
        listTitleTextView.text = weekNo
        return convertView
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun isChildSelectable(listPosition: Int, expandedListPosition: Int): Boolean {
        return true
    }
}
