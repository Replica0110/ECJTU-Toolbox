package com.lonx.ecjtutoolbox.data

sealed class BaseItem {
    abstract val viewType: Int
    companion object {
        const val TYPE_CLICKABLE = 0
        const val TYPE_SWITCH = 1
    }
}
