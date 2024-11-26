package com.intellisoft.lhss.clinical_info.shared

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.intellisoft.lhss.R
import com.intellisoft.lhss.shared.FormData
import com.intellisoftkenya.a24cblhss.R


class ClinicalParentAdapter(
    private val parentItemList: List<FormData>
) : RecyclerView.Adapter<ClinicalParentAdapter.ParentViewHolder>() {

    inner class ParentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val childRecyclerView: RecyclerView = itemView.findViewById(R.id.child_recycler_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.clinical_parent_item, parent, false)
        return ParentViewHolder(view)
    }

    override fun onBindViewHolder(holder: ParentViewHolder, position: Int) {
        val parentItem = parentItemList[position]

        // Setup the Child RecyclerView
        holder.childRecyclerView.apply {
            layoutManager = LinearLayoutManager(holder.itemView.context, LinearLayoutManager.VERTICAL, false)
            adapter = ClinicalChildAdapter(parentItem.formDataList)
        }
    }

    override fun getItemCount(): Int = parentItemList.size
}