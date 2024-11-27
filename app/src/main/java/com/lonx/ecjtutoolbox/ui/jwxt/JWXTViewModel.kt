package com.lonx.ecjtutoolbox.ui.jwxt

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class JWXTViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "教务系统页面"
    }
    val text: LiveData<String> = _text
}