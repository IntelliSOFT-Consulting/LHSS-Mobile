package com.intellisoft.lhss.patient_list

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.intellisoft.lhss.MainActivity
import com.intellisoft.lhss.R
import com.intellisoft.lhss.detail.PatientDetailActivity
import com.intellisoft.lhss.fhir.data.FormatterClass
import com.intellisoft.lhss.fhir.data.NavigationDetails

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
            val lhssFlow = FormatterClass().getSharedPref("lhssFlow", context)

            if (lhssFlow == null){
                val intent = Intent(context, PatientDetailActivity::class.java)
                intent.putExtra("patientId", id)
                context.startActivity(intent)
            }else{
                if (lhssFlow == "referralDetails"){
                    val encounterId = dbPatientList[pos].encounterId?.replace("Encounter/","")
                    if (encounterId != null) {
                        FormatterClass().saveSharedPref("encounterId", encounterId, context)
                    }

                    val intent = Intent(context, MainActivity::class.java)
                    intent.putExtra("functionToCall", NavigationDetails.REFERRAL_DETAIL_VIEW.name)
                    intent.putExtra("patientId", id)
                    context.startActivity(intent)
                }else{
                    val intent = Intent(context, PatientDetailActivity::class.java)
                    intent.putExtra("patientId", id)
                    context.startActivity(intent)
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

        val id = dbPatientList[position].id
        val name = dbPatientList[position].name
        val dob = dbPatientList[position].dob
        val idNumber = dbPatientList[position].dob
        val phoneNumber = dbPatientList[position].phone

        var age = ""
        if (dob != null){
            age = FormatterClass().getFormattedAge(dob.toString(), holder.viewPhoneNumber.context.resources)
        }

        holder.name.text = name
        holder.idNumber.text = id
        holder.tvPhoneNumber.text = phoneNumber.toString()

        holder.viewPhoneNumber.text = "Phone: "
        holder.viewId.text = "Cross Border ID: "
        holder.viewName.text = "Name: "

    }

    override fun getItemCount(): Int {
        return dbPatientList.size
    }

}