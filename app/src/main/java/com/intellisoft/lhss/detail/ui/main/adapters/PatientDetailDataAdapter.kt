package com.intellisoft.lhss.detail.ui.main.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.intellisoft.lhss.R
import com.intellisoft.lhss.fhir.data.DbObservation
import com.intellisoft.lhss.fhir.data.DbPatientDataDetails
import com.intellisoft.lhss.fhir.data.DbVaccineStockDetails
import com.intellisoft.lhss.fhir.data.FormatterClass

class PatientDetailDataAdapter(private var encounterList: ArrayList<DbPatientDataDetails>,
                               private val context: Context
) : RecyclerView.Adapter<PatientDetailDataAdapter.Pager2ViewHolder>() {

    inner class Pager2ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        val tvKey: TextView = itemView.findViewById(R.id.tvKey)
        val tvValue: TextView = itemView.findViewById(R.id.tvValue)

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
                R.layout.patient_detail_layout,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: Pager2ViewHolder, position: Int) {


        val key = encounterList[position].key
        val value = encounterList[position].value

        holder.tvKey.text = key
        holder.tvValue.text = value


    }

    override fun getItemCount(): Int {
        return encounterList.size
    }

}