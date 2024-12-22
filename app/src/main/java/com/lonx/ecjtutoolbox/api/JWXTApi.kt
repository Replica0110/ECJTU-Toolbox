package com.lonx.ecjtutoolbox.api

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.lonx.ecjtutoolbox.utils.Constants.CAS_ECJTU_DOMAIN
import com.lonx.ecjtutoolbox.utils.Constants.DCP_URL
import com.lonx.ecjtutoolbox.utils.Constants.ECJTU2JWXT_URL
import com.lonx.ecjtutoolbox.utils.Constants.ECJTU_LOGIN_URL
import com.lonx.ecjtutoolbox.utils.Constants.GET_STU_PROFILE_URL
import com.lonx.ecjtutoolbox.utils.Constants.JWXT_LOGIN_URL
import com.lonx.ecjtutoolbox.utils.Constants.PORTAL_ECJTU_DOMAIN
import com.lonx.ecjtutoolbox.utils.Constants.DCP_SSO_URL
import com.lonx.ecjtutoolbox.utils.Constants.PWD_ENC_URL
import com.lonx.ecjtutoolbox.utils.Constants.STU_AVATAR_L_URL
import com.lonx.ecjtutoolbox.utils.Constants.USER_AGENT
import com.lonx.ecjtutoolbox.utils.LoginResult
import com.lonx.ecjtutoolbox.utils.PersistentCookieJar
import com.lonx.ecjtutoolbox.utils.StuProfileInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.Cookie
import okhttp3.FormBody
import okhttp3.Headers
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.jsoup.Jsoup
import slimber.log.d
import slimber.log.e
import java.io.IOException

class JWXTApi(
    studId: String,
    stuPwd: String,
    private val cookieJar: PersistentCookieJar,
    private val client: OkHttpClient
) {
    private val maxRetries = 5
    private var studentId:String = studId
    private var studentPassword: String = stuPwd
    fun updateInfo(studentId: String, studentPwd: String){
        this.studentId = studentId
        this.studentPassword = studentPwd
    }
    fun hasLogin(type: Int = 0): Boolean {
        val urlsToCheck = listOf(ECJTU_LOGIN_URL, JWXT_LOGIN_URL)
        val allCookies = urlsToCheck.flatMap { url ->
            cookieJar.loadForRequest(url.toHttpUrl())
        }

        return when (type) {
            0 -> allCookies.any { it.name == "CASTGC" && it.value.isNotEmpty() }
            1 -> listOf("CASTGC", "JSESSIONID").all { cookieName ->
                allCookies.any { it.name == cookieName && it.value.isNotEmpty() }
            }
            else -> false
        }
    }

    suspend fun getProfile(): StuProfileInfo {
        get(DCP_URL) // 获取 key_dcp_v6 cookie

        val requestBody = """
        {
          "map": {
            "method": "getInfo",
            "params": null
          },
          "javaClass": "java.util.HashMap"
        }
        """.trimIndent().toRequestBody("application/json; charset=utf-8".toMediaType())
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
            requestBuilder.header("render", "json")
            requestBuilder.header("clientType", "json")
            requestBuilder.post(requestBody)
        }
        val avatarResponse = post(GET_STU_PROFILE_URL) { requestBuilder ->
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
    suspend fun getYktNum(): String { // 获取一卡通余额，与 getProfile() 方法的区别只在于请求体中的method不一致，可通过修改此参数获取其他信息
        get(DCP_URL) // 确保有key_dcp_v6
        val requestBody = """
            {
              "map": {
                "method": "getYktNum", 
                "params": null
              },
              "javaClass": "java.util.HashMap"
            }
        """.trimIndent().toRequestBody("application/json; charset=utf-8".toMediaType())
        val response = post(DCP_SSO_URL) { requestBuilder ->
            requestBuilder.header("render", "json")
            requestBuilder.header("clientType", "json")
            requestBuilder.post(requestBody)
        }
        return response.body?.string()?:"0.00"
    }
    private suspend fun getEncryptedPassword(oriPWD: String): String = withContext(Dispatchers.IO) {
        val formBody = FormBody.Builder()
            .add("pwd", oriPWD)
            .build()
        val request = Request.Builder()
            .url(PWD_ENC_URL)
            .post(formBody)
            .build()
        d {"密码加密中..."}
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                e { "密码加密失败: ${response.code}" }
            }

            val responseBody = response.body?.string()?: ""

            val jsonElement = Json.parseToJsonElement(responseBody)
            return@withContext jsonElement.jsonObject["passwordEnc"]?.jsonPrimitive?.content.toString()
        }
    }

    suspend fun login(refresh: Boolean): LoginResult = withContext(Dispatchers.IO) {
        d { "登录中..." }

        // 刷新登录，清除 Cookie
        if (refresh) {
            e { "刷新登录..." }
            cookieJar.clear()
        }

        // 获取加密后的密码
        val encPassword = getEncryptedPassword(studentPassword)

        // 登录请求数据
        val loginPayload = mapOf(
            "username" to studentId,
            "password" to encPassword,
            "service" to PORTAL_ECJTU_DOMAIN
        )

        // 公共请求头
        val headers = Headers.Builder()
            .add("User-Agent", USER_AGENT)
            .add("Host", CAS_ECJTU_DOMAIN)
            .build()

        // 获取登录页面
        val ltValue = getLoginLtValue(headers) ?: return@withContext LoginResult.Failure("无法获取登录页面数据")

        // 构建登录请求体并发送登录请求
        val loginResponse = loginWithCredentials(loginPayload, ltValue, headers)
        if (loginResponse == null || !loginResponse.isSuccessful || !loginResponse.headers("Set-Cookie").any { it.contains("CASTGC") }) {
            e { "登录失败：账号或密码错误" }
            return@withContext LoginResult.Failure("账号或密码错误")
        }

        // 执行重定向请求
        val finalResponse = handleRedirection(headers)
        if (finalResponse != null && finalResponse.code != 200) {
            e { "登录失败：重定向错误，状态码${finalResponse.code}" }
            return@withContext LoginResult.Failure("重定向错误，状态码${finalResponse.code}")
        }

        d { "登录成功" }
        return@withContext LoginResult.Success("登录成功")
    }

    // 获取登录页面的 LT 参数
    private fun getLoginLtValue(headers: Headers): String? {
        return try {
            val response = client.newCall(
                Request.Builder()
                    .url(ECJTU_LOGIN_URL)
                    .headers(headers)
                    .build()
            ).execute()

            val document = response.body?.string()?.let { Jsoup.parse(it) }
            document?.select("input[name=lt]")?.attr("value")
        } catch (e: Exception) {
            e { "获取登录页面失败: ${e.message}" }
            null
        }
    }

    // 使用提供的凭证和 LT 参数登录
    private fun loginWithCredentials(payload: Map<String, String>, ltValue: String, headers: Headers): Response? {
        val loginRequestBody = FormBody.Builder()
            .add("username", payload["username"]!!)
            .add("password", payload["password"]!!)
            .add("lt", ltValue)
            .build()

        return try {
            val request = Request.Builder()
                .url(ECJTU_LOGIN_URL)
                .post(loginRequestBody)
                .headers(headers)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("Referer", ECJTU_LOGIN_URL)
                .build()

            client.newCall(request).execute()
        } catch (e: Exception) {
            e { "登录请求失败: ${e.message}" }
            null
        }
    }

    // 处理重定向并获取最终响应
    private fun handleRedirection(headers: Headers): Response? {
        return try {
            // 首先请求 JWXT 登录
            client.newCall(
                Request.Builder()
                    .url(JWXT_LOGIN_URL)
                    .headers(headers)
                    .build()
            ).execute()

            // 获取重定向 URL
            val ecjt2jwxtResponse = client.newCall(
                Request.Builder()
                    .url(ECJTU2JWXT_URL)
                    .headers(headers)
                    .build()
            ).execute()

            val redirectUrl = ecjt2jwxtResponse.header("Location")

            // 如果有重定向 URL，执行最终请求
            redirectUrl?.let {
                client.newCall(
                    Request.Builder()
                        .url(it)
                        .headers(headers)
                        .build()
                ).execute()
            }
        } catch (e: Exception) {
            e { "重定向请求失败: ${e.message}" }
            null
        }
    }


    suspend fun post(
        url: String,
        formData: Map<String, String>? = null,
        currentRetries: Int = 0,
        headers: (Request.Builder) -> Unit = {}
    ): Response = withContext(Dispatchers.IO){
        d { "POST $url" }
        if (!hasLogin()) login(false)

        val bodyBuilder = FormBody.Builder()
        formData?.forEach { (key, value) -> bodyBuilder.add(key, value) }

        val requestBuilder = Request.Builder()
            .url(url)
            .post(bodyBuilder.build())

        headers(requestBuilder)

        val request = requestBuilder.build()

        return@withContext try {
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
    ): Response = withContext(Dispatchers.IO){
        d { "GET $url" }
        if (!hasLogin()) login(false)

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
            return@withContext get(url, params, headers, currentRetries + 1)
        }
        return@withContext response
    }
}