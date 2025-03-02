package com.lonx.ecjtutoolbox.data

import com.lonx.ecjtutoolbox.R

data class SwitchItem(
    val icon: Int,
    val text: String,
    val subText: String,
    var checked: Boolean,
    val onCheckedChange: (Boolean) -> Unit
):BaseItem() {
    override val viewType = R.layout.item_switch_card
    fun onCheckedChange(checked: Boolean) {
        this.checked = checked
        onCheckedChange.invoke(checked)
    }
}