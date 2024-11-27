package com.lonx.ecjtutoolbox.utils

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

class CookieJarImpl : CookieJar {
    private val cookieStore: MutableMap<HttpUrl, MutableList<Cookie>> = mutableMapOf()
    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val existingCookies = cookieStore[url]?.toMutableList() ?: mutableListOf()

        for (newCookie in cookies) {
            existingCookies.removeAll { it.name == newCookie.name }
            existingCookies.add(newCookie)
        }
        cookieStore[url] = existingCookies
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        return cookieStore.entries
            .filter { url.host.endsWith(it.key.host) }
            .flatMap { it.value }
            .filter { it.matches(url) }
    }

    fun hasCookie(name: String): Boolean {
        return cookieStore.values.flatten().any { it.name == name }
    }

    fun getAllCookies(): List<Cookie> {
        return cookieStore.values.flatten()
    }

    fun getCookiesForUrl(url: HttpUrl): List<Cookie> {
        return cookieStore[url] ?: emptyList()
    }
}
