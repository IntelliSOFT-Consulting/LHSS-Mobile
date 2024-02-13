package com.intellisoft.lhss.detail.ui.main.routine

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.intellisoft.lhss.R
import com.intellisoft.lhss.fhir.data.DbObservation
import com.intellisoft.lhss.fhir.data.DbVaccineStockDetails
import com.intellisoft.lhss.fhir.data.FormatterClass

class VisitHistoryAdapter(private var encounterList: ArrayList<DbObservation>,
                          private val context: Context
) : RecyclerView.Adapter<VisitHistoryAdapter.Pager2ViewHolder>() {

    inner class Pager2ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        val tvHospital: TextView = itemView.findViewById(R.id.tvHospital)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)

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
                R.layout.visit_history,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: Pager2ViewHolder, position: Int) {


        val name = encounterList[position].name
        val date = encounterList[position].date

        val dateStr = FormatterClass().convertDateFormat(date)

        holder.tvHospital.text = name
        holder.tvDate.text = dateStr


    }

    override fun getItemCount(): Int {
        return encounterList.size
    }

}