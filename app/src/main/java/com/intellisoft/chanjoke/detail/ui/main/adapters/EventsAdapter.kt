package com.intellisoft.chanjoke.detail.ui.main.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.intellisoft.chanjoke.MainActivity
import com.intellisoft.chanjoke.R
import com.intellisoft.chanjoke.detail.ui.main.AdverseEventActivity
import com.intellisoft.chanjoke.fhir.data.AdverseEventData
import com.intellisoft.chanjoke.fhir.data.FormatterClass
import com.intellisoft.chanjoke.fhir.data.NavigationDetails


class EventsAdapter(
    private var entryList: List<AdverseEventData>,
    private val context: Context
) : RecyclerView.Adapter<EventsAdapter.Pager2ViewHolder>() {

    inner class Pager2ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        val tvType: TextView = itemView.findViewById(R.id.tv_type)
        val tvDate: TextView = itemView.findViewById(R.id.tv_date)
        val linearView: LinearLayout = itemView.findViewById(R.id.linearView)

        init {
            itemView.setOnClickListener(this)
            linearView.setOnClickListener {


            }
        }

        override fun onClick(p0: View) {
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): Pager2ViewHolder {
        return Pager2ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.aefi_card_view,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: Pager2ViewHolder, position: Int) {
        val tvType = entryList[position].type
        val tvDate = entryList[position].date

        holder.tvType.text = tvType
        holder.tvDate.text = tvDate
        holder.linearView.apply {
            setOnClickListener {
                FormatterClass().saveSharedPref(
                    "encounter_logical", entryList[position].logicalId, context
                )
                val intent = Intent(context, AdverseEventActivity::class.java)
                context.startActivity(intent)
            }
        }


    }

    override fun getItemCount(): Int {
        return entryList.size
    }
}