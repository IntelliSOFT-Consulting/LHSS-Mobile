package com.intellisoft.lhss25.shared

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.intellisoft.lhss25.R

class PatientAdapter(
    private val patientList: List<DbPatientItem>,
    private val onItemClick: (DbPatientItem) -> Unit // Lambda function to handle click
) : RecyclerView.Adapter<PatientAdapter.PatientViewHolder>() {

    inner class PatientViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameValue: TextView = itemView.findViewById(R.id.nameValue)
        val crossBorderIdValue: TextView = itemView.findViewById(R.id.crossBorderIdValue)
        val dobValue: TextView = itemView.findViewById(R.id.dobValue)
        val dateCreated: TextView = itemView.findViewById(R.id.dateCreated)

        // Bind click listener to the itemView
        fun bind(patient: DbPatientItem) {
            itemView.setOnClickListener {
                onItemClick(patient)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatientViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.patient_list_item, parent, false)
        return PatientViewHolder(view)
    }

    override fun onBindViewHolder(holder: PatientViewHolder, position: Int) {
        val patient = patientList[position]
        holder.nameValue.text = patient.name
        holder.crossBorderIdValue.text = patient.crossBorderId
        holder.dobValue.text = patient.dob
        holder.dateCreated.text = patient.dateCreated

        // Call the bind function to set the click listener
        holder.bind(patient)
    }

    override fun getItemCount(): Int {
        return patientList.size
    }
}

