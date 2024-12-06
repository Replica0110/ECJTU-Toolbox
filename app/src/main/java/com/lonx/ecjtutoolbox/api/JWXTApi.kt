package com.lonx.ecjtutoolbox.api

import com.franmontiel.persistentcookiejar.PersistentCookieJar
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
        // 需要检查的域名
        val urlsToCheck = listOf(
            ECJTU_LOGIN_URL,  // 登录时需要检查的域名
            JWXT_LOGIN_URL, // 登录后需要检查的域名
        )

        // 获取存储的 cookies
        val allCookies = mutableListOf<Cookie>()
        // 获取 cookies
        urlsToCheck.forEach { url ->
            val httpUrl = url.toHttpUrl()
            val cookiesForDomain = cookieJar.loadForRequest(httpUrl)
            allCookies.addAll(cookiesForDomain)
        }
        return when (type) {
            0 -> {
                // 仅检查 CASTGC cookie
                e { "CASTGC cookie found: ${allCookies.any { it.name == "CASTGC" && it.value.isNotEmpty() }}" }
                allCookies.any { it.name == "CASTGC" && it.value.isNotEmpty() }
            }
            1 -> {
                // 检查是否有完整的 cookie 集合
                val requiredCookies = listOf("CASTGC", "JSESSIONID")
                requiredCookies.all { cookieName ->
                    allCookies.any { it.name == cookieName && it.value.isNotEmpty() }
                }
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

    suspend fun login(refresh: Boolean): String = withContext(Dispatchers.IO){
        d { "登录中..."}
        if (refresh) {
            e { "刷新登录..."}
            cookieJar.clear()
        }
        val encPassword = getEncryptedPassword(studentPassword)

        val loginPayload = mapOf(
            "username" to studentId,
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
                e { "登录失败：账号或密码错误" }
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
            e { "登录失败：重定向错误，状态码${finalResponse.code}" }
            return@withContext "登录失败：教务系统错误，错误码${finalResponse.code}"
        }
        d { "登录成功" }
        return@withContext "登录成功"
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