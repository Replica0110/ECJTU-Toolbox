package com.lonx.ecjtutoolbox.utils

sealed class WifiStatus {
    data object Connected : WifiStatus()
    data object Disconnected : WifiStatus()
    data object Disabled : WifiStatus()
    data object Unknown : WifiStatus()
    data object Enabled : WifiStatus()
}