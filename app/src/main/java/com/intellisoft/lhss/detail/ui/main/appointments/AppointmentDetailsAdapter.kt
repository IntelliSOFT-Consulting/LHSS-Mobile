package com.intellisoft.lhss.detail.ui.main.appointments

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.intellisoft.lhss.MainActivity
import com.intellisoft.lhss.R
import com.intellisoft.lhss.fhir.data.DbAppointmentDetails
import com.intellisoft.lhss.fhir.data.FormatterClass
import com.intellisoft.lhss.fhir.data.NavigationDetails

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