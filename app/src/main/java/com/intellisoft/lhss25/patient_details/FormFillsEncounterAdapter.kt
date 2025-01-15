package com.intellisoft.lhss25.patient_details

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.intellisoft.lhss25.R
import com.intellisoft.lhss25.shared.DbEncounter
import com.intellisoft.lhss25.shared.DbFormsData
import com.intellisoft.lhss25.shared.FormatterClass


class FormFillsEncounterAdapter(
    private val context: Context,
    private val fragment: Fragment,
    private val formList: ArrayList<DbFormsData>
) : RecyclerView.Adapter<FormFillsEncounterAdapter.ParentViewHolder>() {

    inner class ParentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val contactPerson: TextView = itemView.findViewById(R.id.contactPerson)
        val tvReportingDate: TextView = itemView.findViewById(R.id.tvReportingDate)
        val tvFilledOn: TextView = itemView.findViewById(R.id.tvFilledOn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.filled_forms_item, parent, false)
        return ParentViewHolder(view)
    }

    override fun onBindViewHolder(holder: ParentViewHolder, position: Int) {
        val parentItem = formList[position]

        val formatterClass = FormatterClass(context)

        holder.contactPerson.text = parentItem.contactPerson
        holder.tvReportingDate.text = parentItem.reportingDate
        holder.tvFilledOn.text = parentItem.filledOn

        holder.itemView.setOnClickListener {

            val id = parentItem.encounterId.replace("Encounter/","")
            FormatterClass(context).saveSharedPref("","encounterId", id)
            findNavController(fragment).navigate(R.id.action_patientCardFragment_to_filledFormsListFragment)

        }

    }

    override fun getItemCount(): Int = formList.size
}