package com.lonx.ecjtutoolbox.utils

import android.content.Context
import android.view.LayoutInflater
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.lonx.ecjtutoolbox.databinding.DialogAccountConfigBinding

class AccountConfigHelper(
    private val context: Context,
    private val preferencesManager: PreferencesManager,
    private val onCredentialsUpdate: (newId: String, newPwd: String) -> Unit
) {
    // 数据绑定类
    data class AccountConfig(
        var studentId: String = "",
        var studentPwd: String = "",
        var ispIndex: Int = 0
    ) {
        fun hasChanged(originalId: String, originalPwd: String, originalIsp: Int) =
            studentId != originalId || studentPwd != originalPwd || ispIndex != originalIsp
    }

    fun showAccountDialog() {
        val originalId = preferencesManager.getString("student_id", "")
        val originalPwd = preferencesManager.getString("student_pwd", "")
        val originalIsp = preferencesManager.getInt("isp", 1) - 1

        val binding = DialogAccountConfigBinding.inflate(LayoutInflater.from(context)).apply {
            account = AccountConfig(originalId, originalPwd, originalIsp)

        }

        MaterialAlertDialogBuilder(context)
            .setView(binding.root)
            .setTitle("账号配置")
            .setPositiveButton("确定") { _, _ ->
                binding.account?.let { config ->
                    if (config.hasChanged(originalId, originalPwd, originalIsp)) {
                        saveCredentials(config)
                        onCredentialsUpdate(config.studentId, config.studentPwd)
                    }
                }
            }
            .setNegativeButton("取消", null)
            .setOnDismissListener {
                // 清理绑定引用
                binding.unbind()
            }
            .show()
    }

    private fun saveCredentials(config: AccountConfig) {
        preferencesManager.apply {
            putString("student_id", config.studentId)
            putString("student_pwd", config.studentPwd)
            putInt("isp", config.ispIndex + 1)
        }
    }
}

