package com.lonx.ecjtutoolbox.utils

sealed class LocationStatus {
    data object Disabled : LocationStatus()
    data object Enabled : LocationStatus()
    data object PermissionDenied : LocationStatus()
    data object Unknown : LocationStatus()
}