package com.lonx.ecjtutoolbox.ui.wifi

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.lonx.ecjtutoolbox.R
import com.lonx.ecjtutoolbox.api.WifiApi
import com.lonx.ecjtutoolbox.utils.PreferencesManager
import com.lonx.ecjtutoolbox.utils.WifiStatus
import com.lonx.ecjtutoolbox.utils.WifiStatusMonitor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import slimber.log.d
import slimber.log.e

class WifiViewModel(
    private val wifiStatusMonitor: WifiStatusMonitor,
    private val preferencesManager: PreferencesManager
) : ViewModel() {
    private val ispOptions = arrayOf("中国电信", "中国移动", "中国联通")
    val wifiStatusIcon = ObservableField(R.drawable.ic_wifi_disabled)
    val wifiStatusText = ObservableField("WLAN 未启用")
    val currentSSID = ObservableField("当前无连接")
    val wifiApi = WifiApi()
    fun openWifiSettings(context: Context) {
        val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
    fun observeWifiStatus(context: Context) {
        viewModelScope.launch {
            wifiStatusMonitor.wifiStatus.collectLatest { status ->
                updateUi(status,context)
            }
        }
    }
    fun accountConfig(view1: View) {
        val context = view1.context
        val view = View.inflate(context, R.layout.dialog_add_account, null)
        val stuIdEditText = view.findViewById<EditText>(R.id.account_stuid)
        val stuPwdEditText = view.findViewById<EditText>(R.id.account_passwrod)
        val ispSpinner = view.findViewById<Spinner>(R.id.account_isp)
        ispSpinner.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, ispOptions).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        val studentId = preferencesManager.getString("student_id", "")
        val studentPwd = preferencesManager.getString("student_pwd", "")
        val isp = preferencesManager.getInt("isp", 1)
        stuIdEditText.setText(studentId)
        stuPwdEditText.setText(studentPwd)
        ispSpinner.setSelection(isp-1)
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

    }
    fun updateCurrentSSID(context: Context) {
        val ssid = wifiStatusMonitor.getCurrentWifiSSID(context)
        currentSSID.set(ssid ?: "未知网络")
    }

    private fun showAlertDialog(context: Context,title: String, message: String) {
        AlertDialog.Builder(context).apply {
            setTitle(title)
            setMessage(message)
            setPositiveButton("确定") { _, _ -> }
            show()
        }
    }
    fun loginIn(view: View) {
        d { "Login in" }
        val stuId=preferencesManager.getString("student_id", "")
        val stuPwd=preferencesManager.getString("student_pwd", "")
        val isp=preferencesManager.getInt("isp", 1)
        if (stuId.isEmpty() || stuPwd.isEmpty()){
            showAlertDialog(view.context,"登录信息","请先设置学号和密码")
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val result = wifiApi.login(stuId, stuPwd, isp)
            withContext(Dispatchers.Main){
                showAlertDialog(view.context,"登录信息",result)
            }
        }


    }
    fun loginOut(view: View) {
        d { "Login out" }
        viewModelScope.launch(Dispatchers.IO) {
            val result = wifiApi.loginOut()
            withContext(Dispatchers.Main){
                showAlertDialog(view.context,"注销信息",result)
            }
        }


    }
    private fun updateUi(status: WifiStatus,context: Context) {
        when (status) {
            WifiStatus.Disabled -> {
                wifiStatusIcon.set(R.drawable.ic_wifi_disabled)
                wifiStatusText.set("WLAN 未启用")
                currentSSID.set("当前无连接")
            }
            WifiStatus.Disconnected -> {
                wifiStatusIcon.set(R.drawable.ic_wifi_disconnected)
                wifiStatusText.set("未连接 WiFi")
                currentSSID.set("当前无连接")
            }
            WifiStatus.Connected -> {
                wifiStatusIcon.set(R.drawable.ic_wifi_connected)
                wifiStatusText.set("已连接 WiFi")
                val ssid = wifiStatusMonitor.getCurrentWifiSSID(context)
                currentSSID.set(ssid ?: "当前无连接")
            }
        }
    }
}
