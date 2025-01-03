package com.intellisoft.lhss25.clinical_info.shared

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
import com.intellisoft.lhss25.shared.FormatterClass


class ClinicalEncounterAdapter(
    private val context: Context,
    private val fragment: Fragment,
    private val clinicalInfo: String?,
    private val parentItemList: ArrayList<DbEncounter>
) : RecyclerView.Adapter<ClinicalEncounterAdapter.ParentViewHolder>() {

    inner class ParentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val tvFilledBy: TextView = itemView.findViewById(R.id.tvFilledBy)
        val chipContainer: Chip = itemView.findViewById(R.id.chipContainer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.section_item, parent, false)
        return ParentViewHolder(view)
    }

    override fun onBindViewHolder(holder: ParentViewHolder, position: Int) {
        val parentItem = parentItemList[position]

        val formatterClass = FormatterClass(context)
        val facilityName = formatterClass.getSharedPref("","facilityName") ?: ""

        parentItem.date.let { holder.tvDate.text = it }
        parentItem.filledBy.let { holder.tvFilledBy.text = "Filled By: $facilityName" }

        //Add an icon to the chip based on the clinical info
        holder.chipContainer.setChipIconResource(R.drawable.ic_action_view)


        //Create a setOnClickListener for the chip
        holder.chipContainer.setOnClickListener {
            val id = parentItem.id.replace("Encounter/","")
            FormatterClass(context).saveSharedPref("","encounterId", id)
            findNavController(fragment).navigate(R.id.action_clinicalInfoEncountersFragment_to_clinicalViewEncountersFragment)
        }

        // Add any additional data to the chip if needed
        // Example: holder.chipContainer.text = parentItem.additionalData

    }

    override fun getItemCount(): Int = parentItemList.size
}