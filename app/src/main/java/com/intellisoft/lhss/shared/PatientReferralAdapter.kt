package com.intellisoft.lhss.shared

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.intellisoft.lhss.R

class PatientReferralAdapter(
    private val patientList: ArrayList<DbServiceRequest?>,
    private val onItemClick: (DbServiceRequest?) -> Unit // Lambda function to handle click
) : RecyclerView.Adapter<PatientReferralAdapter.PatientViewHolder>() {

    inner class PatientViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val referralReason: TextView = itemView.findViewById(R.id.tvReferralReason)
        val referralId: TextView = itemView.findViewById(R.id.tvReferralId)
        val referralDate: TextView = itemView.findViewById(R.id.tvReferralDate)
        val imgBtnStatus: ImageView = itemView.findViewById(R.id.imgBtnStatus)

        // Bind click listener to the itemView
        fun bind(serviceRequest: DbServiceRequest?) {
            itemView.setOnClickListener {
                onItemClick(serviceRequest)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatientViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.patient_referral_list_item, parent, false)
        return PatientViewHolder(view)
    }

    override fun onBindViewHolder(holder: PatientViewHolder, position: Int) {
        val serviceRequest = patientList[position]
        holder.referralReason.text = serviceRequest?.referralReason
        holder.referralId.text = serviceRequest?.id?.substring(0, 5)
        holder.referralDate.text = serviceRequest?.dateRecorded

        val status = serviceRequest?.status

        if (status == "ACTIVE")
            holder.imgBtnStatus.setImageResource(R.mipmap.ic_referral_completed)
        if (status == "COMPLETED")
            holder.imgBtnStatus.setImageResource(R.mipmap.ic_referral_active)

        // Call the bind function to set the click listener
        holder.bind(serviceRequest)
    }

    override fun getItemCount(): Int {
        return patientList.size
    }
}

