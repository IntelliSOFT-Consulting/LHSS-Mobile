package com.intellisoft.chanjoke.detail.ui.main.routine

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.intellisoft.chanjoke.R
import com.intellisoft.chanjoke.fhir.data.DbVaccinationSchedule

class ScheduleAdapter(
    private var entryList: ArrayList<DbVaccinationSchedule>,
    private val context: Context
) : RecyclerView.Adapter<ScheduleAdapter.Pager2ViewHolder>() {

    inner class Pager2ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        val tvScheduleTime: TextView = itemView.findViewById(R.id.tvScheduleTime)
        val imageViewSchedule: ImageView = itemView.findViewById(R.id.imageViewSchedule)
        val recyclerView: RecyclerView = itemView.findViewById(R.id.recyclerView)

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
                R.layout.vaccination_schedule,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: Pager2ViewHolder, position: Int) {

        val scheduleTime = entryList[position].scheduleTime
        val scheduleStatus = entryList[position].scheduleStatus
        val scheduleVaccinationList = entryList[position].scheduleVaccinationList

        val vaccinesScheduleAdapter = VaccinesScheduleAdapter(scheduleVaccinationList, context)
        holder.recyclerView.adapter = vaccinesScheduleAdapter

        holder.tvScheduleTime.text = scheduleTime





    }

    override fun getItemCount(): Int {
        return entryList.size
    }

}