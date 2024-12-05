package com.lonx.ecjtutoolbox.ui.wifi

import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import com.lonx.ecjtutoolbox.utils.WifiStatus
import com.lonx.ecjtutoolbox.extension.isWifiConnected

fun getWifiStatus(wifiManager: WifiManager, connectivityManager: ConnectivityManager): WifiStatus =
        when {
            !wifiManager.isWifiEnabled -> WifiStatus.Disabled
            connectivityManager.isWifiConnected == true -> WifiStatus.Connected
            else -> WifiStatus.Disconnected
        }

internal fun WifiManager.getNoConnectionPresentStatus(): WifiStatus =
        if (isWifiEnabled) WifiStatus.Disconnected else WifiStatus.Disabled

class WifiStatusGetter  constructor(
        private val wifiManager: WifiManager,
        private val connectivityManager: ConnectivityManager
    ) {
        operator fun invoke(): WifiStatus =
            getWifiStatus(wifiManager, connectivityManager)
    }

