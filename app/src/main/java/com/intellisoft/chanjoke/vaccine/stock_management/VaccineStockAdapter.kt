package com.intellisoft.chanjoke.vaccine.stock_management

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.intellisoft.chanjoke.MainActivity
import com.intellisoft.chanjoke.R
import com.intellisoft.chanjoke.fhir.data.DbVaccineData
import com.intellisoft.chanjoke.fhir.data.DbVaccineStockDetails
import com.intellisoft.chanjoke.fhir.data.FormatterClass
import com.intellisoft.chanjoke.fhir.data.NavigationDetails

class VaccineStockAdapter(private var dbVaccineStockDetailsList: ArrayList<DbVaccineStockDetails>,
                          private val context: Context
) : RecyclerView.Adapter<VaccineStockAdapter.Pager2ViewHolder>() {

    inner class Pager2ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        val tvDetailName: TextView = itemView.findViewById(R.id.tvDetailName)
        val tvDetail: TextView = itemView.findViewById(R.id.tvDetail)

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
                R.layout.vaccine_details,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: Pager2ViewHolder, position: Int) {


        val name = dbVaccineStockDetailsList[position].name
        val value = dbVaccineStockDetailsList[position].value

        var vaccine = FormatterClass().formatString(value)

//        if (vaccine.contains("Vaccination Target Disease")) {
//            vaccine = "Vaccine name"
//        }
        holder.tvDetailName.text = vaccine
        holder.tvDetail.text = name


    }

    override fun getItemCount(): Int {
        return dbVaccineStockDetailsList.size
    }

}