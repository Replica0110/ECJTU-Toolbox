package com.lonx.ecjtutoolbox.utils

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.lonx.ecjtutoolbox.databinding.ItemClickableCardBinding
import com.lonx.ecjtutoolbox.ui.ClickableItem


class ItemClickableAdapter(private var clickableItems: List<ClickableItem>) :
    RecyclerView.Adapter<ItemClickableAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemClickableCardBinding) :
        RecyclerView.ViewHolder(binding.root)
    fun updateData(newClickableItems: List<ClickableItem>) {
        clickableItems = newClickableItems
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemClickableCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.item = clickableItems[position]
        holder.binding.executePendingBindings()
    }

    override fun getItemCount() = clickableItems.size
}
