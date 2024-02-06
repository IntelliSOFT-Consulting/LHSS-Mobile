package com.intellisoft.chanjoke.detail.ui.main.appointments

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.intellisoft.chanjoke.MainActivity
import com.intellisoft.chanjoke.R
import com.intellisoft.chanjoke.fhir.data.DbAppointmentData
import com.intellisoft.chanjoke.fhir.data.DbAppointmentDetails
import com.intellisoft.chanjoke.fhir.data.FormatterClass
import com.intellisoft.chanjoke.fhir.data.NavigationDetails
import com.intellisoft.chanjoke.vaccine.stock_management.VaccineStockManagement
import org.hl7.fhir.r4.model.Appointment
import org.hl7.fhir.r4.model.Appointment.AppointmentStatus
import org.hl7.fhir.r4.model.codesystems.ImmunizationRecommendationStatus
import java.time.LocalDate

class AppointmentDetailsAdapter(
    private var entryList: ArrayList<DbAppointmentDetails>,
    private val context: Context
) : RecyclerView.Adapter<AppointmentDetailsAdapter.Pager2ViewHolder>() {

    inner class Pager2ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        val tvVaccineName: TextView = itemView.findViewById(R.id.tvVaccineName)
        val tvAdministerVaccine: TextView = itemView.findViewById(R.id.tvAdministerVaccine)


        init {
            itemView.setOnClickListener(this)

        }

        override fun onClick(p0: View) {

            val formatterClass = FormatterClass()
            val pos = adapterPosition
            val id = entryList[pos].appointmentId
            val patientId = FormatterClass().getSharedPref("patientId", context)

            val targetDisease = entryList[pos].targetDisease
            val administeredProduct = entryList[pos].vaccineName


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

            //Send to contraindications
            val intent = Intent(context, MainActivity::class.java)
            intent.putExtra("functionToCall", NavigationDetails.ADMINISTER_VACCINE.name)
            intent.putExtra("patientId", patientId)
            context.startActivity(intent)


        }


    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): Pager2ViewHolder {
        return Pager2ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.appointment_vaccine,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: Pager2ViewHolder, position: Int) {

        val title = entryList[position].vaccineName

        holder.tvVaccineName.text = title


    }

    override fun getItemCount(): Int {
        return entryList.size
    }

}