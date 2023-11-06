package com.intellisoft.lhss.screening

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.intellisoft.lhss.R
import com.intellisoft.lhss.fhir.data.DbObservation
import com.intellisoft.lhss.fhir.data.FormatterClass


class ScreeningAdapter(private var entryList: ArrayList<DbObservation>,
                       private val context: Context
) : RecyclerView.Adapter<ScreeningAdapter.Pager2ViewHolder>() {

    inner class Pager2ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        val tvVaccineName: TextView = itemView.findViewById(R.id.tvVaccineName)
        val tvDosage: TextView = itemView.findViewById(R.id.tvDosage)

        init {

            itemView.setOnClickListener(this)
        }

        override fun onClick(p0: View) {

            val pos = adapterPosition
            val id = entryList[pos].text

            FormatterClass().saveSharedPref("encounterId", id, context)

            val intent = Intent(context, ReferralScreen::class.java)
            context.startActivity(intent)

        }


    }

    private fun createDialog(id: String) {



        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder.setMessage("The current issue")

        builder.setTitle("Screening Results")
        builder.setCancelable(false)
        builder.setPositiveButton("Do Referral",
            DialogInterface.OnClickListener { dialog: DialogInterface?, which: Int ->
                // When the user click yes button then app will close
//                finish()
            } as DialogInterface.OnClickListener)

        builder.setNegativeButton("No Referral",
            DialogInterface.OnClickListener { dialog: DialogInterface, which: Int ->
                dialog.cancel()
            } as DialogInterface.OnClickListener)

        val alertDialog: AlertDialog = builder.create()
        alertDialog.show()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): Pager2ViewHolder {
        return Pager2ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.administered_vaccines,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: Pager2ViewHolder, position: Int) {


        val vaccineName = entryList[position].text
        val vaccineDosage = entryList[position].answer

        holder.tvVaccineName.text = "$vaccineName"
        holder.tvDosage.text = "$vaccineDosage"


    }

    override fun getItemCount(): Int {
        return entryList.size
    }

}