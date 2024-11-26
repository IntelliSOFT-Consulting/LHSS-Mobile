package com.intellisoft.lhss.clinical_info.shared

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.intellisoft.lhss.R
import com.intellisoft.lhss.shared.DbCarePlan
import com.intellisoft.lhss.shared.DbNavigationDetails
import com.intellisoft.lhss.shared.FormatterClass


class PatientFileAdapter(
    private val context: Context,
    private val fragment: Fragment,
    private val dbCarePlanList: ArrayList<DbCarePlan>
) : RecyclerView.Adapter<PatientFileAdapter.ParentViewHolder>() {

    inner class ParentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvFileName: TextView = itemView.findViewById(R.id.tvFileName)
        val tvCreated: TextView = itemView.findViewById(R.id.tvCreated)
        val chipStatus: Chip = itemView.findViewById(R.id.chipStatus)
        val linear: LinearLayout = itemView.findViewById(R.id.linear)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.patient_file_card_details, parent, false)
        return ParentViewHolder(view)
    }

    override fun onBindViewHolder(holder: ParentViewHolder, position: Int) {
        val parentItem = dbCarePlanList[position]

        val currentNo = "Patient File ${position + 1}"
        val status = parentItem.status
        val dateCreated = "Created ${parentItem.dateCreated}"

        parentItem.fileNumber.let { holder.tvFileName.text = currentNo }
        parentItem.dateCreated.let { holder.tvCreated.text = dateCreated }

        var statusValue = ""
        if (status == "ACTIVE") {
            statusValue = "Ongoing"
            holder.chipStatus.setChipBackgroundColorResource(R.color.white)
            holder.chipStatus.setTextColor(context.resources.getColor(R.color.colorPrimary))
            //Change the linear background color and text color based on the status
            holder.linear.setBackgroundResource(R.drawable.patient_file_border_ongoing)
            holder.tvFileName.setTextColor(context.resources.getColor(R.color.white))
            holder.tvCreated.setTextColor(context.resources.getColor(R.color.white))
        }
        if (status == "COMPLETED") {
            statusValue = "Closed"
            holder.chipStatus.setChipBackgroundColorResource(R.color.red)
            holder.chipStatus.setTextColor(context.resources.getColor(R.color.white))
            //Change the linear background color and text color based on the status
            holder.linear.setBackgroundResource(R.drawable.patient_file_border)
            holder.tvFileName.setTextColor(context.resources.getColor(R.color.black))
            holder.tvCreated.setTextColor(context.resources.getColor(R.color.black))
        }


        holder.chipStatus.text = statusValue

        //Add a setOnClickListener to navigate to the corresponding section when the chip is clicked
        holder.linear.setOnClickListener {
            val carePlanId = parentItem.id.replace("CarePlan/", "")
            FormatterClass(context).saveSharedPref(DbNavigationDetails.CARE_PLAN.name, "carePlanId", carePlanId)
            findNavController(fragment).navigate(R.id.action_patientFileFragment_to_clinicalInfoSectionsFragment)
        }

        // Add any additional data to the chip if needed
        // Example: holder.chipContainer.text = parentItem.additionalData

    }

    override fun getItemCount(): Int = dbCarePlanList.size
}