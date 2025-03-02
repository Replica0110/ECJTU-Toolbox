package com.lonx.ecjtutoolbox.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.lonx.ecjtutoolbox.R
import com.lonx.ecjtutoolbox.data.BaseItem
import com.lonx.ecjtutoolbox.data.ClickableItem
import com.lonx.ecjtutoolbox.data.SwitchItem
import com.lonx.ecjtutoolbox.databinding.ItemClickableCardBinding
import com.lonx.ecjtutoolbox.databinding.ItemSwitchCardBinding


class ItemAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var _items = mutableListOf<BaseItem>()
    fun updateData(newItems: List<BaseItem>) {
        _items.clear()
        _items.addAll(newItems)
        notifyDataSetChanged()
    }
    override fun getItemViewType(position: Int) = _items[position].viewType

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            R.layout.item_switch_card -> SwitchViewHolder(
                ItemSwitchCardBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            R.layout.item_clickable_card -> ClickableViewHolder(
                ItemClickableCardBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            // 添加其他Item类型
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = _items[position]) {
            is SwitchItem -> (holder as SwitchViewHolder).bind(item)
            is ClickableItem -> (holder as ClickableViewHolder).bind(item)
            // 处理其他类型
        }
    }

    override fun getItemCount() = _items.size

    // ViewHolders
    class SwitchViewHolder(val binding: ItemSwitchCardBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SwitchItem) {
            binding.run {
                icSwitch.setOnCheckedChangeListener(null) // 先移除旧监听器
                this.item = item
                icSwitch.isChecked = item.checked
                icSwitch.setOnCheckedChangeListener { _, checked ->
                    item.checked = checked
                    item.onCheckedChange(checked)
                }
                executePendingBindings()
            }
        }
    }

    class ClickableViewHolder(val binding: ItemClickableCardBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ClickableItem) {
            binding.item = item
            binding.executePendingBindings()
        }
    }
}