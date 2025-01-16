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
        val tvContactPerson: TextView = itemView.findViewById(R.id.tvContactPerson)
        val tvReportingDate: TextView = itemView.findViewById(R.id.tvReportingDate)
        val tvFilledOn: TextView = itemView.findViewById(R.id.tvFilledOn)

        val nameLabel: TextView = itemView.findViewById(R.id.nameLabel)
        val referralReportingDateLabel: TextView = itemView.findViewById(R.id.referralReportingDateLabel)
        val filledOnLabel: TextView = itemView.findViewById(R.id.filledOnLabel)


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.filled_forms_item, parent, false)
        return ParentViewHolder(view)
    }

    override fun onBindViewHolder(holder: ParentViewHolder, position: Int) {
        val parentItem = formList[position]

        val formatterClass = FormatterClass(context)

        val formName = formatterClass.getSharedPref("","FORM_NAME") ?: ""
        if (formName != "") {
            if (formName == "END_TREATMENT_FORM"){
                holder.nameLabel.text = "Contact Person"
                holder.referralReportingDateLabel.text = "Reporting Date"
                holder.filledOnLabel.text = "Filled On"
            }
            if (formName == "ACKNOWLEDGEMENT_FORM"){
                holder.nameLabel.text = "Referral Reason"
                holder.referralReportingDateLabel.text = "Referral Date"
                holder.filledOnLabel.text = "Filled On"
            }
        }


        holder.tvContactPerson.text = parentItem.contactPersonReferralReason
        holder.tvReportingDate.text = parentItem.reportingReferralDate
        holder.tvFilledOn.text = parentItem.filledOn

        holder.itemView.setOnClickListener {

            val id = parentItem.id
            if (formName == "ACKNOWLEDGEMENT_FORM"){
                formatterClass.saveSharedPref("","serviceRequestId", id)
                formatterClass.deleteSharedPref("","encounterId")
            }
            if (formName == "END_TREATMENT_FORM"){
                formatterClass.saveSharedPref("","encounterId", id)
                formatterClass.deleteSharedPref("","serviceRequestId")
            }

            findNavController(fragment).navigate(R.id.action_filledFormsListFragment_to_viewFormDetailsFragment)

        }

    }

    override fun getItemCount(): Int = formList.size
}