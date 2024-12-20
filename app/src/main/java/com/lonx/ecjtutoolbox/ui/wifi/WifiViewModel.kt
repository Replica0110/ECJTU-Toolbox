package com.lonx.ecjtutoolbox.ui.wifi

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.lonx.ecjtutoolbox.R
import com.lonx.ecjtutoolbox.api.WifiApi
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
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module
import slimber.log.d

class WifiViewModel(
    private val wifiStatusMonitor: WifiStatusMonitor,
    private val locationStatusMonitor: LocationStatusMonitor,
    private val preferencesManager: PreferencesManager
) : ViewModel() {
    private val ispOptions = arrayOf("中国电信", "中国移动", "中国联通")
    val wifiStatusIcon = ObservableField(R.drawable.ic_wifi_disabled)
    val wifiStatusText = ObservableField("WLAN 未启用")
    val ssid1 = ObservableField("当前无连接")
    val isLocationEnabled = ObservableField(false)
    val isLoggingIn = MutableLiveData(false)
    val isLoggingOut = MutableLiveData(false)
    val dialogShowed = MutableLiveData(false)
    private val wifiApi = WifiApi()
    private val LOCATION_PERMISSION_REQUEST_CODE = 100
    fun openWifiSettings(view: View) {
        val context = view.context
        val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
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
                updateUi(wifiStatus, locationStatus,context)
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
                // 用户之前拒绝了权限，直接弹出权限请求
                requestPermissions(
                    context,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE
                )
            } else {
                // 如果用户未拒绝过或选择了"不再提示"，弹出说明并直接请求权限
                AlertDialog.Builder(context).apply {
                    setTitle("需要位置权限")
                    setMessage("应用需要位置权限以获取WiFi信息，请授予该权限")
                    setPositiveButton("确定") { _, _ ->
                        // 请求权限
                        requestPermissions(
                            context,
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                            LOCATION_PERMISSION_REQUEST_CODE
                        )
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

    fun accountConfig(view1: View) {
        dialogShowed.value = true
        val context = view1.context
        val view = View.inflate(context, R.layout.dialog_add_account, null)
        val stuIdEditText = view.findViewById<EditText>(R.id.account_stuid)
        val stuPwdEditText = view.findViewById<EditText>(R.id.account_passwrod)
        val ispSpinner = view.findViewById<Spinner>(R.id.account_isp)
        ispSpinner.adapter =
            ArrayAdapter(context, android.R.layout.simple_spinner_item, ispOptions).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
        val studentId = preferencesManager.getString("student_id", "")
        val studentPwd = preferencesManager.getString("student_pwd", "")
        val isp = preferencesManager.getInt("isp", 1)
        stuIdEditText.setText(studentId)
        stuPwdEditText.setText(studentPwd)
        ispSpinner.setSelection(isp - 1)
        MaterialAlertDialogBuilder(context)
            .setView(view)
            .setTitle("账号配置")
            .setPositiveButton("确定") { _, _ ->
                preferencesManager.putString("student_id", stuIdEditText.text.toString())
                preferencesManager.putString("student_pwd", stuPwdEditText.text.toString())
                preferencesManager.putInt("isp", ispSpinner.selectedItemPosition + 1)
            }
            .setNegativeButton("取消") { _, _ -> }
            .show()
        dialogShowed.value = false
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

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun loginIn(view: View) {
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

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun loginOut(view: View) {
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
                ssid1.set("位置信息不可用")
            }

            LocationStatus.Enabled -> {
            }

            LocationStatus.PermissionDenied -> {
                ssid1.set("位置信息不可用")
            }

            LocationStatus.Unknown -> {
            }
        }
        }

}
