package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.LmsRepository
import com.squareup.moshi.Moshi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class LmsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = LmsRepository(application)
    val dao = repository.dao

    // --- State Observables mapped from Room Flows ---
    val users: StateFlow<List<UserEntity>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val departments: StateFlow<List<DepartmentEntity>> = repository.allDepartments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val courses: StateFlow<List<CourseEntity>> = repository.allCourses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val enrollments: StateFlow<List<EnrollmentEntity>> = repository.allEnrollments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val attendanceList: StateFlow<List<AttendanceEntity>> = repository.allAttendance
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val assignments: StateFlow<List<AssignmentEntity>> = repository.allAssignments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val submissions: StateFlow<List<AssignmentSubmissionEntity>> = repository.allSubmissions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val quizzes: StateFlow<List<QuizEntity>> = repository.allQuizzes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val quizSubmissions: StateFlow<List<QuizSubmissionEntity>> = repository.allQuizSubmissions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val timetableEvents: StateFlow<List<TimetableEntity>> = repository.allTimetableEvents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val feeChallans: StateFlow<List<FeeChallanEntity>> = repository.allFeeChallans
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val salarySlips: StateFlow<List<SalarySlipEntity>> = repository.allSalarySlips
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val exams: StateFlow<List<ExamEntity>> = repository.allExams
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val examResults: StateFlow<List<ExamResultEntity>> = repository.allExamResults
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val announcements: StateFlow<List<AnnouncementEntity>> = repository.allAnnouncements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chatMessages: StateFlow<List<ChatMessageEntity>> = repository.allChatMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    // --- Global Search Query ---
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // --- Dynamic QR Attendance System states ---
    private val _attendanceQrCode = MutableStateFlow<String?>(null)
    val attendanceQrCode = _attendanceQrCode.asStateFlow()

    fun generateAttendanceQr(courseId: String) {
        val uniqueSession = "ATT-${courseId}-${System.currentTimeMillis()}"
        _attendanceQrCode.value = uniqueSession
    }

    fun clearAttendanceQr() {
        _attendanceQrCode.value = null
    }

    fun scanAttendanceQrAndMark(qrContent: String, studentId: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            if (qrContent.startsWith("ATT-")) {
                val parts = qrContent.split("-")
                if (parts.size >= 3) {
                    val courseId = parts[1]
                    val todayDate = "2026-07-02" // Consistent with mockup current dates
                    val attendance = AttendanceEntity(
                        courseId = courseId,
                        studentId = studentId,
                        date = todayDate,
                        status = "PRESENT",
                        gpsVerified = true,
                        latitude = 31.5204, // Mock GPS Coordinates (e.g., University gate)
                        longitude = 74.3587
                    )
                    dao.insertAttendance(attendance)
                    onResult(true, "Attendance marked successfully via QR scan!")
                } else {
                    onResult(false, "Invalid QR Format.")
                }
            } else {
                onResult(false, "Unknown QR code parsed.")
            }
        }
    }


    // --- Admin Action: User CRUD ---
    fun createAccount(id: String, name: String, email: String, role: String, passwordHash: String, deptId: String?) {
        viewModelScope.launch {
            val user = UserEntity(
                id = id,
                name = name,
                email = email,
                role = role,
                passwordHash = passwordHash,
                departmentId = deptId,
                isActive = true
            )
            dao.insertUser(user)
        }
    }

    fun updateAccount(user: UserEntity) {
        viewModelScope.launch {
            dao.updateUser(user)
        }
    }

    fun deleteAccount(id: String) {
        viewModelScope.launch {
            dao.deleteUserById(id)
        }
    }

    fun setUserActiveState(id: String, isActive: Boolean) {
        viewModelScope.launch {
            dao.setUserActiveState(id, isActive)
        }
    }

    fun resetPassword(id: String, newPass: String) {
        viewModelScope.launch {
            dao.resetUserPassword(id, newPass)
        }
    }

    // --- Admin Action: Department CRUD ---
    fun addDepartment(id: String, name: String, code: String, desc: String) {
        viewModelScope.launch {
            dao.insertDepartment(DepartmentEntity(id, name, code, desc))
        }
    }

    fun deleteDepartment(id: String) {
        viewModelScope.launch {
            dao.deleteDepartmentById(id)
        }
    }

    // --- Admin/HOD Action: Course CRUD ---
    fun addCourse(id: String, name: String, code: String, credits: Int, sem: Int, deptId: String, teacherId: String?, desc: String) {
        viewModelScope.launch {
            dao.insertCourse(CourseEntity(id, name, code, credits, sem, deptId, teacherId, desc, "course_banner_placeholder"))
        }
    }

    fun updateCourse(course: CourseEntity) {
        viewModelScope.launch {
            dao.updateCourse(course)
        }
    }

    fun deleteCourse(id: String) {
        viewModelScope.launch {
            dao.deleteCourseById(id)
        }
    }

    // --- Enrollment requests management ---
    fun requestSelfEnrollment(studentId: String, courseId: String) {
        viewModelScope.launch {
            dao.insertEnrollment(
                EnrollmentEntity(
                    studentId = studentId,
                    courseId = courseId,
                    status = "PENDING",
                    enrollmentHistory = "Requested online on 2026-07-02"
                )
            )
        }
    }

    fun approveEnrollment(id: Int) {
        viewModelScope.launch {
            dao.updateEnrollmentStatus(id, "APPROVED")
        }
    }

    fun rejectEnrollment(id: Int) {
        viewModelScope.launch {
            dao.updateEnrollmentStatus(id, "REJECTED")
        }
    }

    fun deleteEnrollment(id: Int) {
        viewModelScope.launch {
            dao.deleteEnrollment(id)
        }
    }

    // --- Teacher Actions: Attendance ---
    fun takeAttendanceManual(courseId: String, studentId: String, date: String, status: String) {
        viewModelScope.launch {
            dao.insertAttendance(
                AttendanceEntity(
                    courseId = courseId,
                    studentId = studentId,
                    date = date,
                    status = status
                )
            )
        }
    }

    // --- Teacher Actions: Assignment CRUD ---
    fun createAssignment(courseId: String, title: String, description: String, deadline: String, marks: Int, instructions: String) {
        viewModelScope.launch {
            val id = UUID.randomUUID().toString()
            val assign = AssignmentEntity(id, courseId, title, description, deadline, marks, instructions)
            dao.insertAssignment(assign)
        }
    }

    fun deleteAssignment(id: String) {
        viewModelScope.launch {
            dao.deleteAssignmentById(id)
        }
    }

    fun gradeSubmission(submissionId: Int, marks: Double, feedback: String) {
        viewModelScope.launch {
            dao.gradeSubmission(submissionId, marks, feedback, "GRADED")
        }
    }

    // --- Student Actions: Submit Assignment ---
    fun submitAssignmentOnline(assignmentId: String, studentId: String, text: String, fileName: String) {
        viewModelScope.launch {
            val submission = AssignmentSubmissionEntity(
                assignmentId = assignmentId,
                studentId = studentId,
                submittedText = text,
                attachmentName = fileName,
                attachmentUrl = "uploads/assignments/$fileName",
                status = "SUBMITTED"
            )
            dao.insertSubmission(submission)
        }
    }

    // --- Teacher Actions: Quiz CRUD ---
    fun createQuiz(courseId: String, title: String, duration: Int, questions: List<QuizQuestion>) {
        viewModelScope.launch {
            val id = UUID.randomUUID().toString()
            val moshi = Moshi.Builder().add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory()).build()
            val adapter = moshi.adapter(List::class.java)
            // Format quiz structure cleanly
            val mapList = questions.map { q ->
                mapOf(
                    "id" to q.id,
                    "text" to q.text,
                    "type" to q.type,
                    "options" to q.options,
                    "correctAnswers" to q.correctAnswers,
                    "correctAnswerText" to q.correctAnswerText,
                    "negativeMarkingWeight" to q.negativeMarkingWeight
                )
            }
            val jsonStr = adapter.toJson(mapList)
            dao.insertQuiz(QuizEntity(id, courseId, title, duration, jsonStr))
        }
    }

    fun deleteQuiz(id: String) {
        viewModelScope.launch {
            dao.deleteQuizById(id)
        }
    }

    // --- Student Actions: Submit Quiz ---
    fun submitQuizScore(quizId: String, studentId: String, score: Double, totalQuestions: Int, answersMap: Map<String, List<Int>>) {
        viewModelScope.launch {
            val moshi = Moshi.Builder().add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory()).build()
            val adapter = moshi.adapter(Map::class.java)
            val jsonStr = adapter.toJson(answersMap)
            val submission = QuizSubmissionEntity(
                quizId = quizId,
                studentId = studentId,
                score = score,
                totalQuestions = totalQuestions,
                answersJson = jsonStr
            )
            dao.insertQuizSubmission(submission)
        }
    }

    // --- Messaging / Chat System ---
    fun sendChatMessage(senderId: String, senderName: String, recipientId: String?, isGroup: Boolean, groupId: String?, msgText: String, file: String? = null, fType: String? = null) {
        viewModelScope.launch {
            val msg = ChatMessageEntity(
                senderId = senderId,
                senderName = senderName,
                recipientId = recipientId,
                isGroup = isGroup,
                groupId = groupId,
                message = msgText,
                fileName = file,
                fileType = fType
            )
            dao.insertChatMessage(msg)
        }
    }

    // --- Academic Planner & Timetables ---
    fun addTimetableSession(courseId: String, day: String, start: String, end: String, room: String, isLive: Boolean) {
        viewModelScope.launch {
            val event = TimetableEntity(
                courseId = courseId,
                dayOfWeek = day,
                startTime = start,
                endTime = end,
                classroom = room,
                liveStatus = if (isLive) "LIVE" else "UPCOMING",
                meetingLink = if (isLive) "https://meet.google.com/edusphere-live-session" else ""
            )
            dao.insertTimetableEvent(event)
        }
    }

    // --- Fee Slips & Salaries ERP module ---
    fun addFeeChallan(studentId: String, title: String, amount: Double, dueDate: String, scholarship: Double = 0.0, discount: Double = 0.0) {
        viewModelScope.launch {
            val id = "CHA-" + UUID.randomUUID().toString().substring(0, 6).uppercase()
            val challan = FeeChallanEntity(id, studentId, title, amount, dueDate, scholarship, discount)
            dao.insertFeeChallan(challan)
        }
    }

    fun payFeeChallan(id: String) {
        viewModelScope.launch {
            val receiptId = "REC-" + UUID.randomUUID().toString().substring(0, 8).uppercase()
            dao.updateFeePayment(id, "PAID", "2026-07-02", receiptId)
        }
    }

    fun addSalarySlip(employeeId: String, month: String, base: Double, bonus: Double = 0.0, allowance: Double = 0.0, deduction: Double = 0.0) {
        viewModelScope.launch {
            val id = "SAL-" + UUID.randomUUID().toString().substring(0, 6).uppercase()
            val slip = SalarySlipEntity(id, employeeId, month, base, bonus, allowance, deduction, "PENDING")
            dao.insertSalarySlip(slip)
        }
    }

    fun paySalarySlip(id: String) {
        viewModelScope.launch {
            dao.updateSalaryPayment(id, "PAID", "2026-07-02")
        }
    }

    // --- Academic Exams & Marks ---
    fun addExamSchedule(title: String, courseId: String, date: String, time: String, room: String, invigilatorId: String, seat: String) {
        viewModelScope.launch {
            val id = "EXM-" + UUID.randomUUID().toString().substring(0, 6).uppercase()
            dao.insertExam(ExamEntity(id, title, courseId, date, time, room, invigilatorId, seat))
        }
    }

    fun addExamResult(examId: String, studentId: String, theory: Double, practical: Double, viva: Double) {
        viewModelScope.launch {
            val total = theory + practical + viva
            val gpa: Double
            val grade: String
            when {
                total >= 90 -> { gpa = 4.0; grade = "A+" }
                total >= 80 -> { gpa = 4.0; grade = "A" }
                total >= 70 -> { gpa = 3.5; grade = "B+" }
                total >= 60 -> { gpa = 3.0; grade = "B" }
                total >= 50 -> { gpa = 2.0; grade = "C" }
                else -> { gpa = 0.0; grade = "F" }
            }
            dao.insertExamResult(ExamResultEntity(examId = examId, studentId = studentId, theoryMarks = theory, practicalMarks = practical, vivaMarks = viva, totalMarks = total, grade = grade, gpa = gpa))
        }
    }

    // --- Announcements ---
    fun publishAnnouncement(title: String, content: String, authorId: String, authorName: String, targetRole: String, targetDeptId: String? = null) {
        viewModelScope.launch {
            dao.insertAnnouncement(AnnouncementEntity(title = title, content = content, authorId = authorId, authorName = authorName, targetRole = targetRole, targetDepartmentId = targetDeptId))
        }
    }

    fun removeAnnouncement(id: Int) {
        viewModelScope.launch {
            dao.deleteAnnouncementById(id)
        }
    }
}
