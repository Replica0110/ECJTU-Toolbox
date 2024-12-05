package com.lonx.ecjtutoolbox.api

import com.lonx.ecjtutoolbox.utils.Constants.GET_CLASSES_URL
import com.lonx.ecjtutoolbox.utils.Constants.WEIXIN_JWXT_URL
import com.lonx.ecjtutoolbox.utils.ScheduledCourseInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import slimber.log.e
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ScheduledCourseApi(client: JWXTApi) {

    private val api: JWXTApi = client
    private suspend fun courseByjwxt(
        date: String = formattedDate(Date())
    ):List<ScheduledCourseInfo>{
        val response = api.post(GET_CLASSES_URL, mapOf("date" to date))
        val course=response.body?.string()?:"[]"
        println(course)  // TODO 由于教务系统返回的json数据为空，不实现具体的解析
        return emptyList()
    }
    private fun formattedDate(
        date: Date
    ): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(date)
    }

    private suspend fun courseByweixinid(
        date: String = formattedDate(Date()),
        weixinid: String
    ):List<ScheduledCourseInfo> = withContext(Dispatchers.IO){

        val params = mapOf("weiXinID" to weixinid, "date" to date)
        val response = api.get(WEIXIN_JWXT_URL, params = params)
        val courseHtml = response.body?.string() ?: ""


        val document: Document = Jsoup.parse(courseHtml)
        val courseList = mutableListOf<ScheduledCourseInfo>()
        val dateElement = document.select("div.center").text() ?: "N/A"
        val weekDay=dateElement.split(" ")[1].substringBefore("（")
        val courseElements = document.select("div.calendar ul.rl_info li")

        if (courseElements.isEmpty()||courseElements.all { it.select("img").isNotEmpty() }) { // 判断课程是否为空
            return@withContext courseList
        }

        for (element: Element in courseElements) {
            val classSpan = element.toString().substringAfter("时间：").substringBefore("<br>").split(" ")[1].trim()
            val courseName = element.toString().substringAfter("</span>").substringBefore("<br>").trim()
            val weekSpan = element.toString().substringAfter("时间：").substringBefore("<br>").split(" ")[0].trim()
            println(courseName)
            val pkType = courseName.split("(")[0].replace(")", "").trim()
            val classRoom = element.toString().substringAfter("地点：").substringBefore("<br>").trim()
            val teacher = element.toString().substringAfter("教师：").substringBefore("<br>").trim()

            val courseInfo = ScheduledCourseInfo(
                classSpan = classSpan,
                course = courseName.split("(")[0].trim(),
                courseName = courseName.split("(")[0].trim(),
                weekSpan = weekSpan,
                courseType = "unknown",
                teacher = teacher,
                weekDay = weekDay,
                classRoom = classRoom,
                pkType = pkType
            )
            courseList.add(courseInfo)
        }

        return@withContext courseList
    }

    suspend fun getCourse(
        weixinid: String =""
    ):List<ScheduledCourseInfo> = withContext(Dispatchers.IO){
        try {
            if (weixinid.isNotEmpty()){
                return@withContext courseByjwxt()
            }else {
                return@withContext courseByweixinid(weixinid = weixinid)
            }
        } catch (e: Exception) {
            e { "出现错误: ${e.message}" }
            return@withContext emptyList()
        }

    }

}
