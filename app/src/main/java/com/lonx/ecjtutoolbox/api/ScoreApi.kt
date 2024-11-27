package com.lonx.ecjtutoolbox.api


import android.util.Log
import com.lonx.ecjtutoolbox.utils.Constants.GET_GPA_URL
import com.lonx.ecjtutoolbox.utils.ScoreInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

class ScoreApi(client: JWXTApi) {
    private val api: JWXTApi = client

    private suspend fun score(semester: String = ""): MutableList<ScoreInfo> = withContext(Dispatchers.IO) {
        val response = api.get(GET_GPA_URL)
        val scores: MutableList<ScoreInfo> = mutableListOf()

        if (response.code != 200) return@withContext scores

        val document = response.body?.string()?.let { Jsoup.parse(it) }

        document?.let {
            val scoreElements = if (semester.isEmpty()) {
                it.select("ul.term_score")
            } else {
                it.select("ul.term_score")
                    .filter { ul ->
                        ul.selectFirst("li.s_t_term")?.text() == semester
                    }
            }

            // 解析每一门课程的成绩信息
            for (element in scoreElements) {
                val term = element.selectFirst("li.s_t_term")?.text() ?: continue
                val courseName = element.selectFirst("li.s_t_cour.s_t_cCon")?.text()?.replace(Regex("【.*?】"), "") ?: continue
                val courseNature = element.selectFirst("li.s_t_courRequire")?.text() ?: continue
                val credit = element.select("li").getOrNull(4)?.text() ?: "0"
                val grade = element.select("li").getOrNull(5)?.text() ?: "无成绩"

                // 添加到成绩列表
                scores.add(ScoreInfo(semester = term, courseName = courseName, courseNature = courseNature, credit = credit, grade = grade))
            }
        }

        return@withContext scores
    }
    suspend fun getScore(semester: String = ""): MutableList<ScoreInfo> {
        try {
            return score(semester)
        } catch (e: Exception) {
            Log.e(TAG,"Error occurred: ${e.message}")
            return mutableListOf()
        }
    }
    companion object {
        const val TAG = "ScoreApi"
    }
}
