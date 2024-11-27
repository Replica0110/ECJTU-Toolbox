package com.lonx.ecjtutoolbox.utils

data class GPAInfo(
    val studentName: String,
    val gpa: String,
    val status: String
) {
    companion object {
        fun fromData(data: List<String>): GPAInfo {
            return GPAInfo(
                studentName = data[1],
                gpa = data[6],
                status = data[2]
            )
        }
    }
}
data class ScheduledCourseInfo(
    val classSpan: String,
    val course:String,
    val courseName: String,
    val weekSpan: String,
    val courseType: String,
    val teacher: String,
    val weekDay: String,
    val classRoom: String,
    val pkType: String
){

    companion object {
        fun fromData(data: List<String>): ScheduledCourseInfo {
            return ScheduledCourseInfo(
                classSpan = data[0],
                course = data[1],
                courseName = data[2],
                weekSpan = data[3],
                courseType = data[4],
                teacher = data[5],
                weekDay = data[6],
                classRoom = data[7],
                pkType = data[8]
            )
        }
    }
}

data class ElectiveCourseInfo(
    val semester: String, // 学期，例如2023.1
    val className: String, // 小班名称，例如自然语言处理导论(20231-1)【小2班】
    val classType: String, // 选课类型/要求，例如必修课
    val classAssessmentMethod: String, // 考核方式，例如考察，考试
    val classInfo: String, // 上课时间，例如第1-16周 星期三 第3,4节[14-103]
    val classNumber: String, //小班序号，例如14
    val credit: String, // 学分，例如2.0
    val teacher: String // 教师，例如张三
) {
    companion object {
        fun fromData(data: List<String>): ElectiveCourseInfo {
            return ElectiveCourseInfo(
                semester = data[0],
                className = data[1],
                classType = data[2],
                classAssessmentMethod = data[3],
                classInfo = data[4],
                classNumber = data[5],
                credit = data[6],
                teacher = data[7]
            )
        }
    }
}

data class ScoreInfo(
    val semester: String,
    val courseName: String,
    val courseNature: String,
    val credit: String,
    val grade: String
) {
    companion object {
        fun fromData(data: List<String>): ScoreInfo {
            return ScoreInfo(
                semester = data[0],
                courseName = data[1],
                courseNature = data[2],
                credit = data[3],
                grade = data[4]
            )
        }
    }
}
data class StuProfileInfo(
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
){
    companion object {
        fun fromData(data: List<String>): StuProfileInfo {
            return StuProfileInfo(
                idNumber = data[0],
                birthday = data[1],
                userId = data[2],
                unitName = data[3],
                mobile = data[4],
                userName = data[5],
                idType = data[6],
                idTypeName = data[7],
                isMainIdentity = data[8],
                sexName = data[9],
                userSex = data[10],
                avatar = data[11]
            )
        }
    }
}