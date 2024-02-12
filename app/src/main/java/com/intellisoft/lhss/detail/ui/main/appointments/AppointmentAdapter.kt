package com.intellisoft.lhss.detail.ui.main.appointments

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.intellisoft.lhss.R
import com.intellisoft.lhss.fhir.data.DbAppointmentData
import com.intellisoft.lhss.fhir.data.FormatterClass
import org.hl7.fhir.r4.model.Appointment.AppointmentStatus

class AppointmentAdapter(
    private var entryList: ArrayList<DbAppointmentData>,
    private val context: Context
) : RecyclerView.Adapter<AppointmentAdapter.Pager2ViewHolder>() {

    inner class Pager2ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        val tvAppointment: TextView = itemView.findViewById(R.id.tvAppointment)
        val tvAppointmentDate: TextView = itemView.findViewById(R.id.tvAppointmentDate)
        val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)

        init {
            itemView.setOnClickListener(this)

        }

        override fun onClick(p0: View) {

            val formatterClass = FormatterClass()
            val pos = adapterPosition
            val id = entryList[pos].id

            formatterClass.saveSharedPref("appointmentId",id.toString(), context)

            val intent = Intent(context, AppointmentDetails::class.java)
            context.startActivity(intent)

        }


    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): Pager2ViewHolder {
        return Pager2ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.appointments,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: Pager2ViewHolder, position: Int) {

        val title = entryList[position].title
        val description = entryList[position].description
        val dateScheduled = entryList[position].dateScheduled
        val status = entryList[position].status
        val recommendationList = entryList[position].recommendationList

        val text = if (recommendationList.isNullOrEmpty()){
            title
        }else{
            recommendationList[0].vaccineName
        }

        holder.tvAppointment.text = text
        holder.tvDescription.text = description

        holder.tvStatus.text = status.lowercase()
        holder.tvAppointmentDate.text = dateScheduled


        if (status.equals(AppointmentStatus.BOOKED.name, ignoreCase = true)){
            holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary))
        }else{
            holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.red))
        }

    }

    override fun getItemCount(): Int {
        return entryList.size
    }

}