package com.lonx.ecjtutoolbox.data

import androidx.databinding.BaseObservable
import kotlinx.serialization.Serializable

@Serializable
class StuProfileModel() {
    @Serializable
    data class Data(
        val idNumber: String,
        val birthday: String,
        val userId: String,
        val unitName: String,
        val mobile: String,
        val userName: String,
        val idType: String,
        val idTypeName: String,
        val isMainIdentity: String,
        val sexName:String,
        val userSex: String,
        val avatar: String
    ):BaseObservable() {
        fun getAvatarUrl(): String {
            return avatar
        }
        fun getSex(): String {
            return userSex
        }
    }
}