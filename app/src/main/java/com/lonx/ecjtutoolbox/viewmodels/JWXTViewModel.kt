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
    val items = MutableLiveData<List<ClickableItem>>()
    fun loadItems() {
        val clickableItems = listOf(
            ClickableItem(
                icon = R.drawable.ic_jwxt_elective,
                text = "选项课",
                subText = "",
                onClick = { view -> openElectiveCourse(view) }
            ),
            ClickableItem(
                icon = R.drawable.ic_jwxt_course,
                text = "必修课",
                subText = "",
                onClick = { view -> openScheduleCourse(view) }
            ),
            ClickableItem(
                icon = R.drawable.ic_menu_account,
                text = "账号配置",
                subText = "配置账号密码及运营商",
                onClick = { view -> accountConfig(view) }
            )
        )
        this.items.value = clickableItems
    }
    private fun openElectiveCourse(view: View) {

    }
    private fun openScheduleCourse(view: View) {

    }
    private fun accountConfig(view: View) {
        AccountConfigHelper(
            context = view.context,
            preferencesManager = preferencesManager,
            onCredentialsUpdate = { newId, newPwd ->
                jwxtApi.updateInfo(newId, newPwd)
            }
        ).showAccountDialog()
    }
}