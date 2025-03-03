package com.lonx.ecjtutoolbox.viewmodels

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lonx.ecjtutoolbox.api.JWXTApi
import com.lonx.ecjtutoolbox.utils.PreferencesManager
import com.lonx.ecjtutoolbox.data.StuProfileInfo
import com.lonx.ecjtutoolbox.utils.AccountConfigHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class AccountViewModel(
    private val jwxtApi: JWXTApi,
    private val preferencesManager: PreferencesManager
) : ViewModel() {
    private val _userProfile = MutableLiveData<StuProfileInfo>()
    val userProfile: LiveData<StuProfileInfo> get() = _userProfile
    private val _isAccountDialogShowing = MutableLiveData(false)
    val isRefreshLogin = MutableLiveData(false)
    init {
        isRefreshLogin.value=preferencesManager.getBoolean("refresh_login", false)
    }
    fun refreshLogin(view: View) {
        isRefreshLogin.value=!isRefreshLogin.value!!
        preferencesManager.putBoolean("refresh_login", isRefreshLogin.value!!)
    }
    // 加载用户数据
    fun loadUserProfile() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val profile = jwxtApi.getProfile()
                _userProfile.postValue(profile) // 更新数据
            } catch (e: Exception) {
                Timber.tag("AccountViewModel").e(e, "Failed to load user profile")
            }
        }
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
}

