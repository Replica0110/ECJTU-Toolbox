package com.lonx.ecjtutoolbox.extension

import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.NetworkCapabilities
import com.lonx.ecjtutoolbox.utils.IPAddress

internal fun ConnectivityManager.ipAddresses(): List<IPAddress> =
    linkProperties?.linkAddresses?.map { IPAddress(it) } ?: emptyList()

internal val ConnectivityManager.linkProperties: LinkProperties?
    get() = getLinkProperties(activeNetwork)

internal val ConnectivityManager.isWifiConnected: Boolean?
    get() =
        getNetworkCapabilities(activeNetwork)?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
