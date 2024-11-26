package com.intellisoft.lhss.clinical_info.viewmodel

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.intellisoft.lhss.databinding.LandingSectionPageItemBinding

class ClinicalLayoutsRecyclerViewAdapter(
    private val onItemClick: (ClinicalLayoutListViewModel.Layout) -> Unit
) :
    ListAdapter<ClinicalLayoutListViewModel.Layout, LayoutViewHolder>(LayoutDiffUtil()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LayoutViewHolder {
        return LayoutViewHolder(
            LandingSectionPageItemBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            onItemClick,
        )
    }

    override fun onBindViewHolder(holder: LayoutViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class LayoutViewHolder(
    val binding: LandingSectionPageItemBinding,
    private val onItemClick: (ClinicalLayoutListViewModel.Layout) -> Unit,
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(layout: ClinicalLayoutListViewModel.Layout) {
        binding.componentLayoutIconImageview.setImageResource(layout.iconId)
        binding.componentLayoutTextView.text = layout.textId
        binding.root.setOnClickListener { onItemClick(layout) }
    }
}

class LayoutDiffUtil : DiffUtil.ItemCallback<ClinicalLayoutListViewModel.Layout>() {
    override fun areItemsTheSame(
        oldLayout: ClinicalLayoutListViewModel.Layout,
        newLayout: ClinicalLayoutListViewModel.Layout,
    ) = oldLayout === newLayout

    override fun areContentsTheSame(
        oldLayout: ClinicalLayoutListViewModel.Layout,
        newLayout: ClinicalLayoutListViewModel.Layout,
    ) = oldLayout == newLayout
}