package com.lonx.ecjtutoolbox.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.SupplicantState
import android.net.wifi.WifiManager
import androidx.core.app.ActivityCompat
import com.lonx.ecjtutoolbox.extension.isWifiConnected
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate

class WifiStatusMonitor(
    private val wifiManager: WifiManager,
    private val connectivityManager: ConnectivityManager
) {
    private val networkRequest = NetworkRequest
        .Builder()
        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
        .build()
    private fun getWifiStatus(wifiManager: WifiManager, connectivityManager: ConnectivityManager): WifiStatus =
        when {
            !wifiManager.isWifiEnabled -> WifiStatus.Disabled
            connectivityManager.isWifiConnected == true -> WifiStatus.Connected
            else -> WifiStatus.Disconnected
        }
    fun getCurrentWifiSSID(context: Context): String? {
        // 检查位置权限
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // 如果权限未授予，返回 null
            return "未授予位置权限，无法获取网络名称"
        }

        // 获取连接的 Wi-Fi 网络信息
        val wifiInfo = wifiManager.connectionInfo
        return if (wifiInfo.supplicantState == SupplicantState.COMPLETED) {
            wifiInfo.ssid?.replace("\"", "") // 去除双引号
        } else {
            null
        }
    }
    fun WifiManager.getNoConnectionPresentStatus(): WifiStatus =
        if (isWifiEnabled) WifiStatus.Disconnected else WifiStatus.Disabled

    val wifiStatus: Flow<WifiStatus> = callbackFlow {
        channel.trySend(
            getWifiStatus(
                wifiManager,
                connectivityManager
            )
                .log { "Sent $it as initial" }
        )

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                channel.trySend(WifiStatus.Connected.log { "Sent $it onAvailable" })
            }

            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                if (network == connectivityManager.activeNetwork) {
                    channel.trySend(WifiStatus.Connected.log { "Sent $it onCapabilitiesChanged" })
                }
            }

            override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {
                if (network == connectivityManager.activeNetwork) {
                    channel.trySend(WifiStatus.Connected.log { "Sent $it onLinkPropertiesChanged" })
                }
            }

            override fun onUnavailable() {
                channel.trySend(
                    wifiManager.getNoConnectionPresentStatus()
                        .log { "Sent $it onUnavailable" }
                )
            }

            override fun onLost(network: Network) {
                channel.trySend(
                    wifiManager.getNoConnectionPresentStatus()
                        .log { "Sent $it onLost" }
                )
            }
        }

        connectivityManager.registerNetworkCallback(
            networkRequest,
            callback
        )

        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }.conflate()
}