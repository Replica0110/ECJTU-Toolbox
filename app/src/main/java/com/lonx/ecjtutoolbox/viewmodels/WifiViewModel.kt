package com.lonx.ecjtutoolbox.viewmodels

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lonx.ecjtutoolbox.R
import com.lonx.ecjtutoolbox.api.JWXTApi
import com.lonx.ecjtutoolbox.api.WifiApi
import com.lonx.ecjtutoolbox.utils.AccountConfigHelper
import com.lonx.ecjtutoolbox.utils.LocationStatus
import com.lonx.ecjtutoolbox.utils.LocationStatusMonitor
import com.lonx.ecjtutoolbox.utils.PreferencesManager
import com.lonx.ecjtutoolbox.utils.WifiStatus
import com.lonx.ecjtutoolbox.utils.WifiStatusMonitor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import slimber.log.d

class WifiViewModel(
    private val jwxtApi: JWXTApi,
    private val wifiStatusMonitor: WifiStatusMonitor,
    private val locationStatusMonitor: LocationStatusMonitor,
    private val preferencesManager: PreferencesManager
) : ViewModel() {
    val wifiStatusIcon = ObservableField(R.drawable.ic_wifi_disabled)
    val wifiStatusText = ObservableField("WLAN 未启用")
    val ssid1 = ObservableField("当前无连接")
    val isLocationEnabled = ObservableField(false)
    private val isLoggingIn = MutableLiveData(false)
    private val isLoggingOut = MutableLiveData(false)
    private val _isAccountDialogShowing = MutableLiveData(false)
    private val wifiApi = WifiApi()
    private val LOCATION_PERMISSION_REQUEST_CODE = 100

    fun openWifiSettings(view: View) {
        // 获取视图所在的上下文，用于启动活动
        val context = view.context

        // 创建一个意图，指定要执行的动作是打开Wi-Fi设置
        val intent = Intent(Settings.ACTION_WIFI_SETTINGS)

        // 添加标志以确保此意图启动一个新的任务
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        // 使用上下文启动Wi-Fi设置界面
        context.startActivity(intent)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun observeStatuses(context: Context) {
        viewModelScope.launch {
            combine(
                wifiStatusMonitor.wifiStatus,
                locationStatusMonitor.locationStatus
            ) { wifiStatus, locationStatus ->
                Pair(wifiStatus, locationStatus)
            }.collectLatest { (wifiStatus, locationStatus) ->
                updateUi(wifiStatus, locationStatus, context)
            }
        }
    }

    fun checkAndRequestPermissions(view: View) {
        val context = view.context

        // 检查是否已开启位置信息
        if (!isLocationEnabled(context)) {
            AlertDialog.Builder(context).apply {
                setTitle("需要开启位置信息")
                setMessage("应用需要您开启位置信息服务以获取WiFi信息，是否前往设置开启？")
                setPositiveButton("去设置") { _, _ ->
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    context.startActivity(intent)
                }
                setNegativeButton("取消", null)
                show()
            }
            return
        }

        // 检查是否已经授予位置权限
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // 如果用户之前拒绝了权限请求且未选择"不再提示"，直接请求权限
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    context as Activity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                requestPermissions(
                    context,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE
                )
            } else {
                // 如果用户拒绝过，弹出说明
                AlertDialog.Builder(context).apply {
                    setTitle("需要位置权限")
                    setMessage("应用需要位置权限以获取WiFi信息，请授予该权限")
                    setPositiveButton("确定") { _, _ ->
                        // 请求权限
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = android.net.Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    }
                    setNegativeButton("取消", null)
                    show()
                }
            }
        } else {
            // 已经拥有权限，提示权限已授予
            infoDialog(context, "权限状态", "位置权限已授予，无需再次申请")
        }
    }


    private fun isLocationEnabled(context: Context): Boolean {
        val locationManager =
            context.getSystemService(Context.LOCATION_SERVICE) as? android.location.LocationManager
        return locationManager?.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER) ?: false ||
                locationManager?.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER) ?: false
    }

    fun accountConfig(view: View) {
        if (_isAccountDialogShowing.value == true) return
        _isAccountDialogShowing.value = true
        AccountConfigHelper(
            context = view.context,
            preferencesManager = preferencesManager,
            onCredentialsUpdate = { newId, newPwd ->
                jwxtApi.updateInfo(newId, newPwd)
            },
            onDismiss = {
                _isAccountDialogShowing.value = false
            }
        ).showAccountDialog()
    }

    fun updateSSID(context: Context) {
        val ssid = wifiStatusMonitor.getSSID(context)
        ssid1.set(ssid ?: "未知网络")
    }

    private fun infoDialog(context: Context, title: String, message: String) {
        AlertDialog.Builder(context).apply {
            setTitle(title)
            setMessage(message)
            setPositiveButton("确定") { _, _ -> }
            show()
        }
    }

    fun loginIn(view: View) {
        if (isLoggingIn.value == true) return
        if (wifiStatusIcon.get() != R.drawable.ic_wifi_connected) {
            infoDialog(view.context, "登录失败", "请先连接校园网")
            return
        }
        d { "Login in" }
        val stuId = preferencesManager.getString("student_id", "")
        val stuPwd = preferencesManager.getString("student_pwd", "")
        val isp = preferencesManager.getInt("isp", 1)

        if (stuId.isEmpty() || stuPwd.isEmpty()) {
            infoDialog(view.context, "登录信息", "请先设置学号和密码")
            return
        }

        // 开始登录前设置为 true
        isLoggingIn.value = true

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val title: String
                var result = wifiApi.login(stuId, stuPwd, isp)
                if (result.startsWith("E")) {
                    title = "登录失败"
                    result = result.substring(3)
                } else {
                    title = "登录成功"
                }
                withContext(Dispatchers.Main) {
                    infoDialog(view.context, title, result)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    infoDialog(view.context, "登录失败", "登录失败，请重试: ${e.message}")
                }
            } finally {
                withContext(Dispatchers.Main) {
                    isLoggingIn.value = false
                }
            }
        }
    }

    fun loginOut(view: View) {
        if (isLoggingOut.value == true) return
        if (wifiStatusIcon.get() != R.drawable.ic_wifi_connected) {
            infoDialog(view.context, "注销失败", "请先连接校园网")
            return
        }
        d { "Login out" }
        isLoggingOut.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                var title = ""
                var result = wifiApi.loginOut()
                if (result.startsWith("E")) {
                    title = "注销失败"
                    result = result.substring(3)
                } else {
                    title = "注销成功"
                }
                withContext(Dispatchers.Main) {
                    infoDialog(view.context, title, result)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    infoDialog(view.context, "注销失败", "注销失败，请重试: ${e.message}")
                }
            } finally {
                withContext(Dispatchers.Main) {
                    isLoggingOut.value = false
                }
            }
        }
    }

    private fun updateUi(wifiStatus: WifiStatus, locationStatus: LocationStatus, context: Context) {
            when (wifiStatus) {
                WifiStatus.Disabled -> {
                    wifiStatusIcon.set(R.drawable.ic_wifi_disabled)
                    wifiStatusText.set("WLAN 未启用")
                    ssid1.set("当前无连接")
                }

                WifiStatus.Disconnected -> {
                    wifiStatusIcon.set(R.drawable.ic_wifi_disconnected)
                    wifiStatusText.set("未连接 WiFi")
                    ssid1.set("当前无连接")
                }

                WifiStatus.Connected -> {
                    wifiStatusText.set("已连接 WiFi")
                    wifiStatusIcon.set(R.drawable.ic_wifi_connected)
                    val ssid = wifiStatusMonitor.getSSID(context)
                    ssid1.set(ssid ?: "获取网络SSID失败")
                }

                WifiStatus.Enabled -> {
                    wifiStatusIcon.set(R.drawable.ic_wifi_disconnected)
                    wifiStatusText.set("未连接 WiFi")
                    ssid1.set("当前无连接")
                }

                WifiStatus.Unknown -> {
                    wifiStatusIcon.set(R.drawable.ic_wifi_disabled)
                    wifiStatusText.set("未知状态")
                    ssid1.set("当前无连接")
                }
            }
        when (locationStatus) {
            LocationStatus.Disabled -> {
                ssid1.set("位置信息不可用，点击打开")
            }

            LocationStatus.Enabled -> {
            }

            LocationStatus.PermissionDenied -> {
                ssid1.set("未授予位置信息权限，点击授权")
            }

            LocationStatus.Unknown -> {
            }
        }
    }
}
