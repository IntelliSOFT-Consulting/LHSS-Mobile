package com.intellisoft.lhss.patient_list

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.intellisoft.lhss.R
import com.intellisoft.lhss.fhir.data.FormatterClass
import java.time.LocalDate
import java.time.Period
import androidx.navigation.fragment.findNavController

class PatientListAdapter(
    private var patientList: List<PatientListViewModel.PatientItem>,
    private val context: Context
) :RecyclerView.Adapter<PatientListAdapter.ViewHolder>(){

    inner class ViewHolder(view:View): RecyclerView.ViewHolder(view), View.OnClickListener {
        val name:TextView
        val field_name:TextView
        init {
            name = view.findViewById(R.id.name)
            field_name = view.findViewById(R.id.field_name)
            view.setOnClickListener(this)
        }

        override fun onClick(p0: View) {
            val pos = adapterPosition
            val id = patientList[pos].resourceId

            FormatterClass().saveSharedPref(
                "patientId",
                id,
                context)

            findNavController(p0).navigate(R.id.patientDetailActivity)

        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.patient_list_item_view, viewGroup, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = patientList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.name.text = patientList[position].name
        holder.field_name.text = getDate(patientList[position].dob)
    }

    private fun getDate(localDate:LocalDate?):String{
        val currentDate = LocalDate.now()
        val age = Period.between(localDate, currentDate)
        return "${age.years} years"
    }


}