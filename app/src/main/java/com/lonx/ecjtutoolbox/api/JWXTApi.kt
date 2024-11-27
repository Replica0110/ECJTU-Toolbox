package com.lonx.ecjtutoolbox.api

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.lonx.ecjtutoolbox.utils.Constants.CAS_ECJTU_DOMAIN
import com.lonx.ecjtutoolbox.utils.Constants.ECJTU2JWXT_URL
import com.lonx.ecjtutoolbox.utils.Constants.ECJTU_LOGIN_URL
import com.lonx.ecjtutoolbox.utils.Constants.GET_STU_INFO_URL
import com.lonx.ecjtutoolbox.utils.Constants.GET_STU_PROFILE_URL
import com.lonx.ecjtutoolbox.utils.Constants.JWXT_ECJTU_DOMAIN
import com.lonx.ecjtutoolbox.utils.Constants.JWXT_LOGIN_URL
import com.lonx.ecjtutoolbox.utils.Constants.PORTAL_ECJTU_DOMAIN
import com.lonx.ecjtutoolbox.utils.Constants.PWD_ENC_URL
import com.lonx.ecjtutoolbox.utils.Constants.STU_AVATAR_L_URL
import com.lonx.ecjtutoolbox.utils.Constants.USER_AGENT
import com.lonx.ecjtutoolbox.utils.CookieJarImpl
import com.lonx.ecjtutoolbox.utils.SSLManager.getUnsafeSslSocketFactory
import com.lonx.ecjtutoolbox.utils.SSLManager.getUnsafeTrustManager
import com.lonx.ecjtutoolbox.utils.StuProfileInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.FormBody
import okhttp3.Headers
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.jsoup.Jsoup
import java.io.IOException
import java.util.concurrent.TimeUnit

class JWXTApi(
    private val studId: String?,
    private val password: String?,
    private val cookieJar: CookieJarImpl = CookieJarImpl()
) {

    private val maxRetries = 5
    private val timeout: Long = 30
    private val client: OkHttpClient = OkHttpClient.Builder()
        .sslSocketFactory(getUnsafeSslSocketFactory(), getUnsafeTrustManager())
        .hostnameVerifier { _, _ -> true }
        .cookieJar(cookieJar)
        .cache(null)
        .connectTimeout(timeout, TimeUnit.SECONDS)
        .readTimeout(timeout, TimeUnit.SECONDS)
        .writeTimeout(timeout, TimeUnit.SECONDS)
        .build()

    val hasLogin: Boolean
        get() = cookieJar.hasCookie("CASTGC")

    private suspend fun profile(): StuProfileInfo {
        val response = get(GET_STU_INFO_URL)
        val document = response.body?.string()?.let { Jsoup.parse(it) }
        val sessValue = document?.select("#sess")?.attr("value") ?: ""
        println("sess value: $sessValue")

        val requestBody = """
        {
          "map": {
            "method": "getInfo",
            "params": null
          },
          "javaClass": "java.util.HashMap"
        }
        """.trimIndent()
            .toRequestBody("application/json; charset=utf-8".toMediaType())
        val avatarBody = """
        {
          "map": {
            "method": "getAvatar",
            "params": null
          },
          "javaClass": "java.util.HashMap"
        }
        """.trimIndent()
            .toRequestBody("application/json; charset=utf-8".toMediaType())
        val profileResponse = post(GET_STU_PROFILE_URL) { requestBuilder ->
            requestBuilder.header("Cookie", "key_dcp_v6=$sessValue")
            requestBuilder.header("render", "json")
            requestBuilder.header("clientType", "json")
            requestBuilder.post(requestBody)
        }
        val avatarResponse = post(GET_STU_PROFILE_URL) { requestBuilder ->
            requestBuilder.header("Cookie", "key_dcp_v6=$sessValue")
            requestBuilder.header("render", "json")
            requestBuilder.header("clientType", "json")
            requestBuilder.post(avatarBody)
        }

        val profileResponseBody = profileResponse.body?.string()
        val avatarResponseBody = avatarResponse.body?.string()

        val gson = Gson()
        val avatarUrl = gson.fromJson(avatarResponseBody, JsonObject::class.java)
            .getAsJsonArray("list").firstNotNullOfOrNull {
                it.asJsonObject.getAsJsonObject("map")
                    .get("AVATAR_L_ID")
                    ?.asString
            } .let {
                "$STU_AVATAR_L_URL$it"
            }
        val stuProfile: StuProfileInfo = gson.fromJson(profileResponseBody, JsonObject::class.java)
            .getAsJsonArray("list")
            .map { it.asJsonObject.getAsJsonObject("map") }
            .map { json ->
                StuProfileInfo(
                    idNumber = json.get("ID_NUMBER").asString,
                    birthday = json.get("BIRTHDAY").asString,
                    userId = json.get("USER_ID").asString,
                    unitName = json.get("UNIT_NAME").asString,
                    mobile = json.get("MOBILE").asString,
                    userName = json.get("USER_NAME").asString,
                    idType = json.get("ID_TYPE").asString,
                    idTypeName = json.get("ID_TYPE_NAME").asString,
                    isMainIdentity = json.get("IS_MAIN_IDENTITY").asString,
                    sexName = json.get("SEX_NAME").asString,
                    userSex = json.get("USER_SEX").asString,
                    avatar = avatarUrl
                )
            }.first()


        return stuProfile
    }
    suspend fun getProfile(): StuProfileInfo {
        return try {
            profile()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get profile",e)
            StuProfileInfo.fromData(emptyList())
        }
    }

    private suspend fun getEncryptedPassword(oriPWD: String): String = withContext(Dispatchers.IO) {
        val formBody = FormBody.Builder()
            .add("pwd", oriPWD)
            .build()
        val request = Request.Builder()
            .url(PWD_ENC_URL)
            .post(formBody)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                Log.e(TAG,"Failed to get encrypted password: ${response.code}")
            }

            val responseBody = response.body?.string()?: ""

            val jsonElement = Json.parseToJsonElement(responseBody)
            return@withContext jsonElement.jsonObject["passwordEnc"]?.jsonPrimitive?.content.toString()
        }
    }

    suspend fun login(): String = withContext(Dispatchers.IO){
        println("Logging in...")

        val encPassword = password?.let { getEncryptedPassword(it) }

        val loginPayload = mapOf(
            "username" to (studId ?: throw IllegalArgumentException("Student ID is required")),
            "password" to encPassword,
            "service" to PORTAL_ECJTU_DOMAIN
        )

        val headers = Headers.Builder()
            .add("User-Agent", USER_AGENT)
            .add("Host", CAS_ECJTU_DOMAIN)
            .build()

        // 获取登录页面
        val response = client.newCall(
            Request.Builder()
                .url(ECJTU_LOGIN_URL)
                .headers(headers)
                .build()
        ).execute()

        val document = response.body?.string()?.let { Jsoup.parse(it) }
        val ltValue = document?.select("input[name=lt]")?.attr("value")

        // 构建登录请求体
        val loginRequestBody = ltValue?.let {
            FormBody.Builder()
                .add("username", loginPayload["username"]!!)
                .add("password", loginPayload["password"]!!)
                .add("lt", it)
                .build()
        }

        // 发送登录请求
        val loginResponse = loginRequestBody?.let {
            Request.Builder()
                .url(ECJTU_LOGIN_URL)
                .post(it)
                .headers(headers)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("Referer", ECJTU_LOGIN_URL)
                .build()
        }?.let {
            client.newCall(it).execute()
        }

        if (loginResponse != null) {
            if (!loginResponse.isSuccessful || !loginResponse.headers("Set-Cookie").any { it.contains("CASTGC") }) {
                Log.e(TAG,"Login failed: Invalid account or password")
                return@withContext "登录失败：账号或密码错误"
            }
        }

        client.newCall(
            Request.Builder()
                .url(JWXT_LOGIN_URL)
                .headers(headers)
                .build()
        ).execute()

        // 获取重定向URL
        val ecjt2jwxtResponse = client.newCall(
            Request.Builder()
                .url(ECJTU2JWXT_URL)
                .headers(headers)
                .build()
        ).execute()

        val redirectUrl = ecjt2jwxtResponse.header("Location")


        val finalResponse = redirectUrl?.let {
            client.newCall(
                Request.Builder()
                    .url(it)
                    .headers(headers)
                    .build()
            ).execute()
        }

        if (finalResponse != null && finalResponse.code != 200) {
            Log.e(TAG,"Error in JWXT system, login failed: ${finalResponse.code}")
            return@withContext "登录失败：教务系统错误，错误码${finalResponse.code}"
        }
//        Log.e(TAG, "Login successful")
        println(finalResponse?.code)
        return@withContext "登录成功"
    }

    suspend fun post(
        url: String,
        formData: Map<String, String>? = null,
        currentRetries: Int = 0,
        headers: (Request.Builder) -> Unit = {}
    ): Response {
        if (!hasLogin) login()

        val bodyBuilder = FormBody.Builder()
        formData?.forEach { (key, value) -> bodyBuilder.add(key, value) }

        val requestBuilder = Request.Builder()
            .url(url)
            .post(bodyBuilder.build())

        headers(requestBuilder)

        val request = requestBuilder.build()

        return try {
            client.newCall(request).execute()
        } catch (e: Exception) {
            if (currentRetries >= maxRetries) throw e
            post(url, formData, currentRetries + 1, headers)
        }
    }

    suspend fun get(
        url: String,
        params: Map<String, String>? = null,
        headers: Map<String, String>? = null,
        currentRetries: Int = 0
    ): Response {
        if (!hasLogin) login()

        val urlBuilder = url.toHttpUrlOrNull()?.newBuilder()
        params?.forEach { (key, value) -> urlBuilder?.addQueryParameter(key, value) }

        val requestBuilder = Request.Builder().url(urlBuilder?.build() ?: throw IllegalArgumentException("Invalid URL"))

        headers?.forEach { (key, value) -> requestBuilder.addHeader(key, value) }

        val request = requestBuilder.build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            if (currentRetries >= maxRetries) {
                throw IOException("Max retries exceeded with URL: $url")
            }
            return get(url, params, headers, currentRetries + 1)
        }
        return response
    }

    companion object {
        const val TAG = "JWXTApi"
    }

}