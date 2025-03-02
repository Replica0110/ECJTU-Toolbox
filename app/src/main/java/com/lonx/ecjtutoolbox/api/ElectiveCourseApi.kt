package com.lonx.ecjtutoolbox.api

import com.lonx.ecjtutoolbox.data.Constants.GET_ELERTIVE_COURSE_URL_TEMPLATE
import com.lonx.ecjtutoolbox.data.ElectiveCourseInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import slimber.log.e

class ElectiveCourseApi(client: JWXTApi) {
    private val api: JWXTApi = client
    private suspend fun fetchEleCourses(semester: String = ""): List<ElectiveCourseInfo> {
        var params: Map<String, String> = mapOf()
        if (semester.isNotEmpty()) {params = mapOf("term" to semester)}
        val response = api.get(GET_ELERTIVE_COURSE_URL_TEMPLATE, params = params)
        val courseHtml = response.body?.string() ?: ""
        val document: Document = Jsoup.parse(courseHtml)
        val rows: List<Element> = document.select("tbody > tr")
        return rows.mapNotNull { row ->
            try {
                val columns = row.select("td")
                ElectiveCourseInfo(
                    semester = columns[0].text(), // 学期
                    className = columns[11].text(), // 小班名称
                    classType = columns[4].text(), // 课程要求
                    classAssessmentMethod = columns[5].text(), // 考核方式
                    classInfo = columns[8].text(), // 上课时间
                    classNumber = columns[12].text(), // 小班序号
                    credit = columns[7].text(), // 学分
                    teacher = columns[9].text() // 任课教师
                )
            } catch (e: Exception) {
                e { "Failed to parse row: " + row + ", error: " + e.message }
                null
            }
        }
    }
    suspend fun fetchCourses(semester: String? = null): List<ElectiveCourseInfo> = withContext(Dispatchers.IO) {
        fetchEleCourses(semester ?: "")
    }
}
