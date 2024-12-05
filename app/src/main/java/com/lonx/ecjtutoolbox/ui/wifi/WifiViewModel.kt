package com.lonx.ecjtutoolbox.ui.wifi

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class WifiViewModel : ViewModel() {
    private val _text = MutableLiveData<String>().apply {
        value = "校园网登录页面"
    }
    val text: LiveData<String> = _text
}