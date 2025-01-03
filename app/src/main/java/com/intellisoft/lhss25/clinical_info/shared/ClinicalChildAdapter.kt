package com.intellisoft.lhss25.clinical_info.shared

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.intellisoft.lhss25.R
import com.intellisoft.lhss25.shared.DbFormData


class ClinicalChildAdapter(
    private val childItemList: List<DbFormData>
) : RecyclerView.Adapter<ClinicalChildAdapter.ChildViewHolder>() {

    inner class ChildViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.child_item_name)
        val descriptionTextView: TextView = itemView.findViewById(R.id.child_item_description)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChildViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.clinical_child_item, parent, false)
        return ChildViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChildViewHolder, position: Int) {
        val childItem = childItemList[position]
        holder.nameTextView.text = childItem.tag
        holder.descriptionTextView.text = childItem.text
    }

    override fun getItemCount(): Int = childItemList.size
}
