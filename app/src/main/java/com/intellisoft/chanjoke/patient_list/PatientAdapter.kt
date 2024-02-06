package com.intellisoft.chanjoke.patient_list

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.intellisoft.chanjoke.R
import com.intellisoft.chanjoke.detail.PatientDetailActivity
import com.intellisoft.chanjoke.fhir.data.FormatterClass

class PatientAdapter(
    private var dbPatientList: ArrayList<PatientListViewModel.PatientItem>,
    private val context: Context
) : RecyclerView.Adapter<PatientAdapter.Pager2ViewHolder>() {

    inner class Pager2ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        val name: TextView = itemView.findViewById(R.id.name)
        val idNumber: TextView = itemView.findViewById(R.id.idNumber)
        val tvPhoneNumber: TextView = itemView.findViewById(R.id.tvPhoneNumber)
        val btnView: Button = itemView.findViewById(R.id.btnView)

        val viewPhoneNumber: TextView = itemView.findViewById(R.id.viewPhoneNumber)
        val viewId: TextView = itemView.findViewById(R.id.viewId)
        val viewName: TextView = itemView.findViewById(R.id.viewName)


        init {
            btnView.setOnClickListener(this)

        }

        override fun onClick(p0: View) {

            val pos = adapterPosition
            val id = dbPatientList[pos].resourceId

            FormatterClass().saveSharedPref("patientId", id, context)
            val selectedVaccinationVenue = FormatterClass().getSharedPref("selectedVaccinationVenue", context)
            val isSelectedVaccinationVenue = FormatterClass().getSharedPref("isSelectedVaccinationVenue", context)

            if (isSelectedVaccinationVenue == null){
                val intent = Intent(context, PatientDetailActivity::class.java)
                intent.putExtra("patientId", id)
                context.startActivity(intent)
            }else{
                if (selectedVaccinationVenue != null){
                    val intent = Intent(context, PatientDetailActivity::class.java)
                    intent.putExtra("patientId", id)
                    context.startActivity(intent)
                }else{
                    Toast.makeText(context, "Please select a vaccination venue", Toast.LENGTH_SHORT).show()
                }
            }



        }


    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): Pager2ViewHolder {
        return Pager2ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.patient_list_item_view,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: Pager2ViewHolder, position: Int) {

        val name = dbPatientList[position].name
        val age = dbPatientList[position].dob
        val idNumber = dbPatientList[position].identification
        val phoneNumber = dbPatientList[position].phone

        holder.name.text = name
        holder.idNumber.text = idNumber
        holder.tvPhoneNumber.text = phoneNumber

        holder.viewPhoneNumber.text = "Phone Number"
        holder.viewId.text = "Identification No"
        holder.viewName.text = "Name"

    }

    override fun getItemCount(): Int {
        return dbPatientList.size
    }

}