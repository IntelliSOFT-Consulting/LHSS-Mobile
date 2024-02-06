package com.intellisoft.chanjoke.detail.ui.main

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.intellisoft.chanjoke.MainActivity
import com.intellisoft.chanjoke.R
import com.intellisoft.chanjoke.fhir.data.DbAppointmentDetails
import com.intellisoft.chanjoke.fhir.data.FormatterClass
import com.intellisoft.chanjoke.fhir.data.NavigationDetails
import com.intellisoft.chanjoke.vaccine.stock_management.VaccineStockManagement
import org.hl7.fhir.r4.model.codesystems.ImmunizationRecommendationStatus
import java.time.LocalDate

class RecommendationAdapter(
    private var entryList: ArrayList<DbAppointmentDetails>,
    private val context: Context
) : RecyclerView.Adapter<RecommendationAdapter.Pager2ViewHolder>() {

    inner class Pager2ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        val tvAppointment: TextView = itemView.findViewById(R.id.tvAppointment)
        val tvDateScheduled: TextView = itemView.findViewById(R.id.tvDateScheduled)
        val tvDoseNumber: TextView = itemView.findViewById(R.id.tvDoseNumber)
        val btnAdministerVaccine: TextView = itemView.findViewById(R.id.btnAdministerVaccine)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)

        val chipAppointment: Chip = itemView.findViewById(R.id.chipAppointment)

        init {
            itemView.setOnClickListener(this)
            btnAdministerVaccine.setOnClickListener {
                val pos = adapterPosition
                val formatterClass = FormatterClass()
                val patientId = FormatterClass().getSharedPref("patientId", context)
                val targetDisease = entryList[pos].targetDisease
                val administeredProduct = entryList[pos].vaccineName
                val appointmentStatus = entryList[pos].appointmentStatus.trim()
                val appointmentId = entryList[pos].appointmentId.trim()
                val dateScheduled = entryList[pos].dateScheduled

                val daysTo = formatterClass.daysBetweenTodayAndGivenDate(dateScheduled)
                if (daysTo != null) {
                    if (daysTo < 14){
                        formatterClass.saveSharedPref(
                            "questionnaireJson",
                            "contraindications.json",
                            context)

                        formatterClass.saveSharedPref(
                            "vaccinationFlow",
                            "createVaccineDetails",
                            context
                        )


                        formatterClass.saveSharedPref(
                            "vaccinationTargetDisease",
                            targetDisease,
                            context
                        )
                        formatterClass.saveSharedPref(
                            "administeredProduct",
                            administeredProduct,
                            context
                        )

                        formatterClass.deleteSharedPref("title", context)
//                if (appointmentStatus == "Contraindicated" && appointmentId != ""){
//                    formatterClass.saveSharedPref(
//                        "isContraindicated",
//                        appointmentId,
//                        context
//                    )
//                }


                        //Send to contraindications
                        val intent = Intent(context, MainActivity::class.java)
                        intent.putExtra("functionToCall", NavigationDetails.ADMINISTER_VACCINE.name)
                        intent.putExtra("patientId", patientId)
                        context.startActivity(intent)
                    }else{
                        Toast.makeText(context, "Choose a date within the next 14 days from today.", Toast.LENGTH_SHORT).show()
                    }
                }else{
                    Toast.makeText(context, "There was an issue try again", Toast.LENGTH_SHORT).show()
                }


            }

        }

        override fun onClick(p0: View) {

            val pos = adapterPosition

        }


    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): Pager2ViewHolder {
        return Pager2ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.recommendation,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: Pager2ViewHolder, position: Int) {

        val targetDisease = entryList[position].targetDisease
        val vaccineName = entryList[position].vaccineName
        val dateScheduled = entryList[position].dateScheduled
        val doseNumber = entryList[position].doseNumber
        val dbAppointmentStatus = entryList[position].appointmentStatus
        var appointmentStatus = dbAppointmentStatus

        val dobFormat = FormatterClass().convertDateFormat(dateScheduled)

        holder.tvAppointment.text = vaccineName
        holder.tvDoseNumber.text = doseNumber

        //Check if dateScheduled is past
        if (dobFormat != null){
            holder.tvDateScheduled.text = dobFormat

            val dobDate = FormatterClass().convertStringToDate(dobFormat, "MMM d yyyy")
            if (dobDate != null) {
                val targetDate = FormatterClass().convertDateToLocalDate(dobDate)
                val currentDate = LocalDate.now()

                // Check if the target date is in the past
                if (targetDate.isBefore(currentDate)) {
                    if (!dbAppointmentStatus.equals(ImmunizationRecommendationStatus.COMPLETE.name, ignoreCase = true)){
                        appointmentStatus = ImmunizationRecommendationStatus.OVERDUE.name
                    }
                }
            }
        }


        holder.tvStatus.text = appointmentStatus


        if (appointmentStatus.equals(ImmunizationRecommendationStatus.DUE.name, ignoreCase = true)){
            holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary))
        }else if (appointmentStatus.equals(ImmunizationRecommendationStatus.CONTRAINDICATED.name, ignoreCase = true)){
            holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.darker_gray))
        }else if (appointmentStatus.equals(ImmunizationRecommendationStatus.COMPLETE.name, ignoreCase = true)){
            holder.btnAdministerVaccine.isVisible = false
            holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.green))
        }else{
            holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.red))
        }


    }

    override fun getItemCount(): Int {
        return entryList.size
    }

}