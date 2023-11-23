package com.intellisoft.lhss

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.intellisoft.lhss.shared.DbPatient

class PatientAdapter(
    private var dbPatientList: ArrayList<PatientListViewModel.PatientItem>,
    private val context: Context
) : RecyclerView.Adapter<PatientAdapter.Pager2ViewHolder>() {

    inner class Pager2ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        val name: TextView = itemView.findViewById(R.id.name)
        val field_name: TextView = itemView.findViewById(R.id.field_name)

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
                R.layout.client_list_item_view,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: Pager2ViewHolder, position: Int) {


        val name = dbPatientList[position].name
        val age = dbPatientList[position].dob

        holder.name.text = name
        holder.field_name.text = age.toString()



    }

    override fun getItemCount(): Int {
        return dbPatientList.size
    }

}