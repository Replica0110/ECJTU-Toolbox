package com.lonx.ecjtutoolbox.viewmodels

import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lonx.ecjtutoolbox.R
import com.lonx.ecjtutoolbox.api.JWXTApi
import com.lonx.ecjtutoolbox.data.ClickableItem
import com.lonx.ecjtutoolbox.utils.AccountConfigHelper
import com.lonx.ecjtutoolbox.utils.PreferencesManager

class JWXTViewModel(
    private val jwxtApi: JWXTApi,
    private val preferencesManager: PreferencesManager
) : ViewModel() {
    private val _isAccountDialogShowing = MutableLiveData(false)

    fun openElectiveCourse(view: View) {

    }
    fun openScheduleCourse(view: View) {

    }
    fun openScore(view: View) {

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