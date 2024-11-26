package com.intellisoft.lhss.shared

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.intellisoft.lhss.R

class ItemAdapter(private val itemList: List<Item>) : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    // ViewHolder class that holds the views for each item
    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
        val imgBtn: ImageView = itemView.findViewById(R.id.imgBtn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.profile_item_list, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = itemList[position]

        // Set the data to the views
        holder.tvTitle.text = item.title
        holder.tvDescription.text = item.description
        holder.imgBtn.setImageResource(item.imageResource)

        // Optionally handle button click
        holder.imgBtn.setOnClickListener {
            // Handle button click action here
        }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }
}
