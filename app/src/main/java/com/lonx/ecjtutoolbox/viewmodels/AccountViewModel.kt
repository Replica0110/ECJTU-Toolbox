package com.lonx.ecjtutoolbox.viewmodels

import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.lonx.ecjtutoolbox.R
import com.lonx.ecjtutoolbox.api.JWXTApi
import com.lonx.ecjtutoolbox.utils.LoginResult
import com.lonx.ecjtutoolbox.utils.PreferencesManager
import com.lonx.ecjtutoolbox.data.StuProfileInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class AccountViewModel(
    private val jwxtApi: JWXTApi,
    private val preferencesManager: PreferencesManager
) : ViewModel() {
    private val ispOptions = arrayOf("中国电信", "中国移动", "中国联通")
    private val _userProfile = MutableLiveData<StuProfileInfo>()
    val userProfile: LiveData<StuProfileInfo> get() = _userProfile
    private val dialogShowed = MutableLiveData(false)
    var isRefreshLogin =  MutableLiveData(false)
    init {
        isRefreshLogin.value=preferencesManager.getBoolean("refresh_login", false)
    }
    // 加载用户数据
    fun loadUserProfile() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val profile = jwxtApi.getProfile()
//                Log.e("AccountViewModel", "Profile loaded: $profile")
                _userProfile.postValue(profile) // 更新数据
            } catch (e: Exception) {
                Timber.tag("AccountViewModel").e(e, "Failed to load user profile")
            }
        }
    }
    fun refreshLogin(view: View) {
        isRefreshLogin.value=!isRefreshLogin.value!!
        preferencesManager.putBoolean("refresh_login", isRefreshLogin.value!!)
    }
    fun accountConfig(view1: View) {
        if (dialogShowed.value == true) return
        dialogShowed.value = true
        val context = view1.context
        val builder = MaterialAlertDialogBuilder(context)
        val view = View.inflate(context, R.layout.dialog_account_config, null)

        val etStuId = view.findViewById<EditText>(R.id.account_stuid)
        val etStuPassword = view.findViewById<EditText>(R.id.account_passwrod)
        val spIsp = view.findViewById<Spinner>(R.id.account_isp)
        etStuId.setText(preferencesManager.getString("student_id", ""))
        etStuPassword.setText(preferencesManager.getString("student_pwd", ""))
        spIsp.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, ispOptions).apply{
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        spIsp.setSelection(preferencesManager.getInt("isp", 1) - 1)
        builder.setView(view)
            .setPositiveButton("保存并登录") { _, _ ->
                preferencesManager.putString("student_id", etStuId.text.toString())
                preferencesManager.putString("student_pwd", etStuPassword.text.toString())
                preferencesManager.putInt("isp", spIsp.selectedItemPosition + 1)
                try {
                    viewModelScope.launch(Dispatchers.IO) {
                        jwxtApi.updateInfo(etStuId.text.toString(),etStuPassword.text.toString()) // 更新账号信息
                        val result = jwxtApi.login(true)
                        val state = when (result) {
                            is LoginResult.Success -> "登录成功"
                            is LoginResult.Failure -> "登录失败：${result.error}"
                        }
                        if (result is LoginResult.Success) {
                            val profile = jwxtApi.getProfile()
                            _userProfile.postValue(profile)
                        }
                        withContext(Dispatchers.Main) { Toast.makeText(context, state, Toast.LENGTH_LONG).show()}
                    }

                } catch (e: Exception){
                    e.printStackTrace()
                    Toast.makeText(context, "出现错误：${e.message}", Toast.LENGTH_SHORT).show()
                } finally {
                    dialogShowed.value = false
                }
            }
            .setNegativeButton("取消") { dialog, _ ->
                dialog.dismiss()
                dialogShowed.value = false
            }
            .setOnDismissListener{
                dialogShowed.value = false
            }

        builder.create().show()
    }
}

