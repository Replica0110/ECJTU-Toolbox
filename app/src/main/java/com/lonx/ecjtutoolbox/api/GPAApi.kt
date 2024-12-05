package com.lonx.ecjtutoolbox.api

import com.lonx.ecjtutoolbox.utils.Constants.GET_GPA_URL
import com.lonx.ecjtutoolbox.utils.GPAInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

class GPAApi(client: JWXTApi) {
    private val api: JWXTApi = client
    private suspend fun gpa(): GPAInfo = withContext(Dispatchers.IO){
        val response = api.get(url= GET_GPA_URL)

        if (!response.isSuccessful) {
            throw RuntimeException("Failed to get GPA, status code: ${response.code}")
        }

        val document = response.body?.string()?.let { Jsoup.parse(it) }
        val rows = document?.select("tr")
        if (rows != null) {
            if (rows.size <= 3) {
                return@withContext GPAInfo.fromData(emptyList())
            }
        }

        val data = rows?.get(3)?.select("td")?.map { it.text() }
        if (data != null) {
            if (data.size < 7) {
                return@withContext GPAInfo.fromData(emptyList())
            }
        }

        return@withContext data?.let { GPAInfo.fromData(it) }!!
    }
    suspend fun getGPA(): GPAInfo {
        return gpa()
    }
}