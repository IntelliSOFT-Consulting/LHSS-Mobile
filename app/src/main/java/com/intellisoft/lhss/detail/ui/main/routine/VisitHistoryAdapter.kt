package com.intellisoft.lhss.detail.ui.main.routine

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.intellisoft.lhss.MainActivity
import com.intellisoft.lhss.R
import com.intellisoft.lhss.detail.PatientDetailActivity
import com.intellisoft.lhss.detail.ui.main.appointments.AppointmentDetails
import com.intellisoft.lhss.fhir.data.DbEncounterDetails
import com.intellisoft.lhss.fhir.data.DbObservation
import com.intellisoft.lhss.fhir.data.DbVaccineStockDetails
import com.intellisoft.lhss.fhir.data.FormatterClass
import com.intellisoft.lhss.fhir.data.NavigationDetails
import org.hl7.fhir.r4.model.Encounter

class VisitHistoryAdapter(private var encounterList: ArrayList<DbEncounterDetails>,
                          private val context: Context
) : RecyclerView.Adapter<VisitHistoryAdapter.Pager2ViewHolder>() {

    inner class Pager2ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        val tvHospital: TextView = itemView.findViewById(R.id.tvHospital)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val linear: LinearLayout = itemView.findViewById(R.id.linear)

        init {
            itemView.setOnClickListener(this)

        }

        override fun onClick(p0: View) {

            val pos = adapterPosition
            val encounterId = encounterList[pos].id
            val workflowName = encounterList[pos].type
            val status = encounterList[position].status
            if (status != null){
                if (status == "INPROGRESS"){
                    FormatterClass().saveSharedPref("workflowName",workflowName.toString(), context)
                }
            }
            FormatterClass().saveSharedPref("encounterId",encounterId.toString(), context)
            val patientId = FormatterClass().getSharedPref("patientId", context)

            val intent = Intent(context, PatientDetailActivity::class.java)
            intent.putExtra("functionToCall", NavigationDetails.DETAIL_VIEW.name)
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
                R.layout.visit_history,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: Pager2ViewHolder, position: Int) {

        var name = ""
        val destination = encounterList[position].destination
        val origin = encounterList[position].origin
        name = if (destination != ""){
            destination
        }else{
            origin
        }

        val date = encounterList[position].date
        val status = encounterList[position].status

        if (status != null){
            if (status == "INPROGRESS"){
                holder.linear.setBackgroundColor(context.resources.getColor(R.color.amber))
            }
        }

        if (date != ""){
            val dateStr = FormatterClass().convertDateFormat(date)
            holder.tvDate.text = dateStr
        }

        holder.tvHospital.text = name


    }

    override fun getItemCount(): Int {
        return encounterList.size
    }

}