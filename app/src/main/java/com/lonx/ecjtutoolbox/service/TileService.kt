package com.lonx.ecjtutoolbox.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.core.app.NotificationCompat
import com.lonx.ecjtutoolbox.MainActivity
import com.lonx.ecjtutoolbox.R
import com.lonx.ecjtutoolbox.api.WIFIApi
import com.lonx.ecjtutoolbox.utils.NetworkType
import com.lonx.ecjtutoolbox.utils.PreferencesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TileService : TileService() {

    private val wifiApi by lazy { WIFIApi() }
    private val preferencesManager by lazy { PreferencesManager.getInstance(this) }

    private companion object {
        const val CHANNEL_ID = "ecjtutoolbox_channel"
        const val CHANNEL_NAME = "ECJTUToolboxChannel"
        const val CHANNEL_DESCRIPTION = "Channel for ECJTUToolbox notifications"
        const val NOTIFICATION_ID = 1
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT).apply {
            description = CHANNEL_DESCRIPTION
        }
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun doLogin(studentid: String, password: String, theISP: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val state = wifiApi.getState()
            val (title, message) = when (state) {
                1 -> "登录失败" to "请检查网络连接"
                2 -> "登录失败" to "未知错误，请检查网络和设备状态"
                3 -> if (studentid.isEmpty() || password.isEmpty()) {
                    "账号/密码为空" to "请检查账号/密码是否填写并保存"
                } else {
                    try {
                        val result = wifiApi.login(studentid, password, theISP)
                        if (result.startsWith("E")) {
                            "登录失败" to result.substring(3)
                        } else {
                            "登录成功" to result
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        "登录失败" to "未知错误：${e.message}"
                    }
                }
                4 -> "登录成功" to "您已经登录到校园网了"
                else -> "登录失败" to "未知错误，请检查网络和设备状态"
            }
            sendNotification(title, message)
        }
    }

    override fun onStartListening() {
        super.onStartListening()
        qsTile.updateTile()
    }

    override fun onStopListening() {
        super.onStopListening()
        qsTile.apply {
            state = Tile.STATE_INACTIVE
            updateTile()
        }
    }

    private fun wifiStatus(): Int {
        return when (wifiApi.getNetworkType(this)) {
            NetworkType.WIFI -> 1
            NetworkType.CELLULAR -> 2
            else -> 3
        }
    }

    override fun onClick() {
        super.onClick()
        val studentid = preferencesManager.getString("student_id", "")
        val password = preferencesManager.getString("student_pwd", "")
        val theISP = preferencesManager.getInt("isp", 1)
        val wifiStatus = wifiStatus()

        when (wifiStatus) {
            2 -> sendNotification("登录失败", "未开启WLAN")
            3 -> sendNotification("登录失败", "未知的网络类型")
            else -> if (studentid.isEmpty() || password.isEmpty()) {
                sendNotification("登录失败", "请先配置账号和密码")
            } else {
                doLogin(studentid, password, theISP)
            }
        }
    }

    private fun sendNotification(title: String, message: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.tile_icon)
            .setContentTitle(title)
            .setShowWhen(true)
            .setSubText("一键登录")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager) {
            notify(NOTIFICATION_ID, builder.build())
        }
    }
}
