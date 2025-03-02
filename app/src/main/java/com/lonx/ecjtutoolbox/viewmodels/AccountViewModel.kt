package com.lonx.ecjtutoolbox.viewmodels

import android.view.View
import androidx.databinding.ObservableBoolean
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lonx.ecjtutoolbox.R
import com.lonx.ecjtutoolbox.api.JWXTApi
import com.lonx.ecjtutoolbox.data.BaseItem
import com.lonx.ecjtutoolbox.data.ClickableItem
import com.lonx.ecjtutoolbox.utils.PreferencesManager
import com.lonx.ecjtutoolbox.data.StuProfileInfo
import com.lonx.ecjtutoolbox.data.SwitchItem
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

    val isRefreshLogin = MutableLiveData(false)
    val items = MutableLiveData<List<BaseItem>>()
    init {
        isRefreshLogin.value=preferencesManager.getBoolean("refresh_login", false)
    }

    fun loadItems() {
        val items = listOf(
            ClickableItem(
                icon = R.drawable.ic_menu_account,
                text = "账号配置",
                subText = "配置账号密码及运营商",
                onClick = { view -> accountConfig(view) }
            ),
            SwitchItem(
                icon = R.drawable.ic_account_refresh,
                text = "刷新登录",
                subText = "启动应用时重新登录以更新cookies",
                checked = isRefreshLogin.value ?: false,
                onCheckedChange = { checked ->
                    isRefreshLogin.value = checked
                    preferencesManager.putBoolean("refresh_login", checked)
                }
            )
        )
        this.items.value = items
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
        AccountConfigHelper(
            context = view.context,
            preferencesManager = preferencesManager,
            onCredentialsUpdate = { newId, newPwd ->
                jwxtApi.updateInfo(newId, newPwd)
            }
        ).showAccountDialog()
    }
}

