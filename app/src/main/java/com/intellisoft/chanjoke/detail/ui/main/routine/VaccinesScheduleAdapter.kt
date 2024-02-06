package com.intellisoft.chanjoke.detail.ui.main.routine

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.intellisoft.chanjoke.R
import com.intellisoft.chanjoke.fhir.data.DbScheduleVaccination
import com.intellisoft.chanjoke.fhir.data.DbVaccinationSchedule

class VaccinesScheduleAdapter(
    private var entryList: ArrayList<DbScheduleVaccination>,
    private val context: Context
) : RecyclerView.Adapter<VaccinesScheduleAdapter.Pager2ViewHolder>() {

    inner class Pager2ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        val tvVaccineName: TextView = itemView.findViewById(R.id.tvVaccineName)
        val tvVaccineDate: TextView = itemView.findViewById(R.id.tvVaccineDate)
        val tvScheduleStatus: TextView = itemView.findViewById(R.id.tvScheduleStatus)


        init {
            itemView.setOnClickListener(this)

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
                R.layout.vaccination_schedule_vaccines,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: Pager2ViewHolder, position: Int) {

        val vaccineName = entryList[position].vaccineName
        val vaccineStatus = entryList[position].vaccineStatus
        val vaccineDate = entryList[position].vaccineDate

        holder.tvVaccineName.text = vaccineName
        holder.tvVaccineDate.text = vaccineDate
        holder.tvScheduleStatus.text = vaccineStatus




    }

    override fun getItemCount(): Int {
        return entryList.size
    }

}