package com.lonx.ecjtutoolbox.ui.account

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lonx.ecjtutoolbox.api.JWXTApi
import com.lonx.ecjtutoolbox.utils.StuProfileInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.inject
import timber.log.Timber

class AccountViewModel(private val jwxtApi: JWXTApi) : ViewModel() {
    private val _userProfile = MutableLiveData<StuProfileInfo>()
    val userProfile: LiveData<StuProfileInfo> get() = _userProfile

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
}

