package com.lonx.ecjtutoolbox.utils

import com.lonx.ecjtutoolbox.utils.SSLManager.getUnsafeSslSocketFactory
import com.lonx.ecjtutoolbox.utils.SSLManager.getUnsafeTrustManager
import okhttp3.CookieJar
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class MyOkHttpClient(
    private val cookieJar: CookieJar,
    private val timeout: Long
) {
    fun createClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .sslSocketFactory(getUnsafeSslSocketFactory(), getUnsafeTrustManager())
            .hostnameVerifier { _, _ -> true }
            .cookieJar(cookieJar)
            .cache(null)
            .connectTimeout(timeout, TimeUnit.SECONDS)
            .readTimeout(timeout, TimeUnit.SECONDS)
            .writeTimeout(timeout, TimeUnit.SECONDS)
            .build()
    }

}