# 项目已迁移到[ECJTU-PDA](https://github.com/Replica0110/ECJTU-PDA)

## 待添加的功能
### 个人信息url
（包含学号、在班编号、
姓名、班级、
性别、民族、
出生日期、身份证号、
政治面貌、籍贯、
培养方案编号、英语分级级别、
学籍状态、处分状态、
高考考生号、高考成绩、
生源地信息、联系方式（可编辑）、修改密码功能）
```text
https://jwxt.ecjtu.edu.cn/stuMag/UserInfoAction_findUserInfo.action
```
### 考试安排查询
```text
https://jwxt.ecjtu.edu.cn/examArrange/stuExam_findTerm.action?item=0205
```
### 学期课表查询
```text
https://jwxt.ecjtu.edu.cn/Schedule/Schedule_getUserSchedume.action
```
### 班级相关查询
可查询全校所有班级的班级名单、班级任务书和班级课表
```text
班级名单-https://jwxt.ecjtu.edu.cn/doubleDegree/fxclass_findClassList.action
班级任务书-https://jwxt.ecjtu.edu.cn/doubleDegree/fxclassTask_toTaskClass.action
班级课表-https://jwxt.ecjtu.edu.cn/doubleDegree/fxSchedule_iniSchedule.action
```
...
## 所有可查询url如下
```text
<h3>信息查询</h3>
<a href="/infoQuery/XKStu_findTerm.action">选课小班序号</a>
<a href="/eduScheme/eduScheme_iniScheme.action">专业培养方案</a>
<a href="/infoQuery/class_toTaskTeacher.action">教师任务书</a>
<a href="/infoQuery/myEduScheme.jsp">我的培养方案</a>
<a href="/infoQuery/class_toTaskClass.action">班级任务书</a>
<a href="/infoQuery/ElecCert_myElecCert.action">我的电子证明</a>
<a href="/infoQuery/class_findStudentWorkbooks.action">学生手册</a>

<h3>课表/考试</h3>
<a href="/Schedule/Schedule_iniSchedule.action">班级课表</a>
<a href="/Schedule/Schedule_initeacherSchedule.action">教师课表</a>
<a href="/Schedule/KSchedule_findKongClass.action">空教室</a>
<a href="/Schedule/ClassRoom_iniClassRoom.action">教室信息</a>
<a href="/Schedule/Schedule_getUserSchedume.action">我的课表</a>
<a href="/examArrange/stuExam_findTerm.action">考试安排</a>
<a href="/examArrange/stuBKExam_stuBKExam.action">补考安排</a>				
<a href="/Schedule/Weekcalendar_getStudentWeekcalendar.action">我的周历</a>
<a class="privilege" href="/Schedule/StuDealApply_StuDealApply.action">请假<!-- /休学 -->申请 </a>
<!-- <a href="/Schedule/ClassRoomApply_findClassRoomApplyinfos.action">教室申请</a> -->

<h3>考试报名</h3>
<!--<a href="/examEnroll/showExam_toExamEnroll.action?projectGroup=cet46">英语四六级</a>
<a href="/examEnroll/showExam_toExamEnroll.action?projectGroup=computer">计算机等级</a>-->
<a href="/examEnroll/showExam_toExamEnroll.action?projectGroup=zzy">转专业</a>
<a href="/examEnroll/showExam_toExamEnroll.action?projectGroup=sxw">辅修报名</a>
<a href="/examEnroll/showExam_toExamEnroll.action?projectGroup=other">其他</a>

<h3>成绩查询</h3>
<a href="/scoreQuery/stuScoreQue_getStuScore.action">我的成绩</a>
<!--<a href="/scoreQuery/CETScore_showCETScore.action">英语四六级</a>-->
				
<a href="/scoreQuery/secondCreQue_findSecondCredit.action">素质拓展学分</a>
<a href="/scoreQuery/ExamSusApply_findExamSusApplyinfos.action">缓考申请</a>
<a href="/scoreQuery/StuStudyExtension_findStuStudyExtensionApplyinfos.action">延长学习年限申请</a>

<h3>教学评价</h3>
<a href="/assess/stuAssess_getPJCourseInfo.action">学生评教</a>		
<a class="privilege" href="/assess/teachmessage_PJTeachMessageStu.action">教学信息反馈</a>

<h3>实践教学</h3>
<a href="/Experiment/StudentExperiment_getExperiment.action">实验安排</a>
<!-- <a href="javascript:;">实习实训安排</a>  -->

<h3>毕业设计/论文</h3>
<a href="/graduation/graduStu_getGdtProjectStudents.action?eduType=1">毕设选题</a>
<!-- <a href="/graduation/graduStu_getGdtProjectStudents.action?eduType=2">辅修毕设选题</a> -->				

<a href="/doubleDegree/fxclass_findClassList.action">班级名单</a>
<a href="/doubleDegree/fxclassTask_toTaskClass.action">班级任务书</a>
<a href="/doubleDegree/fxSchedule_iniSchedule.action">班级课表</a>

```
