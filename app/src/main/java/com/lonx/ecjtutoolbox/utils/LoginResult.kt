package com.lonx.ecjtutoolbox.utils

sealed class LoginResult {
    data class Success(val message: String) : LoginResult()
    data class Failure(val error: String) : LoginResult()
}