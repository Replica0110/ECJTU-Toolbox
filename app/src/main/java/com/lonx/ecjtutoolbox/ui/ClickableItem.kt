package com.lonx.ecjtutoolbox.ui

import android.view.View
import com.lonx.ecjtutoolbox.R

data class ClickableItem(
    val icon: Int,
    val arrowIcon: Int = R.drawable.ic_arrow_right,
    val text: String,
    val subText: String,
    val onClick: (View) -> Unit
    ) {
    fun onClick(view: View) {
        onClick.invoke(view)
    }
}


