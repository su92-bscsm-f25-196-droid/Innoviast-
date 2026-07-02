package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val role: String, // ADMIN, HOD, TEACHER, STUDENT
    val isActive: Boolean = true,
    val passwordHash: String,
    val departmentId: String? = null,
    val phone: String = "",
    val address: String = "",
    val avatarUrl: String = "",
    val isRemembered: Boolean = false
)

@Entity(tableName = "departments")
data class DepartmentEntity(
    @PrimaryKey val id: String,
    val name: String,
    val code: String,
    val description: String = ""
)

@Entity(tableName = "courses")
data class CourseEntity(
    @PrimaryKey val id: String,
    val name: String,
    val code: String,
    val creditHours: Int,
    val semester: Int,
    val departmentId: String,
    val teacherId: String?, // Assigned teacher
    val description: String = "",
    val thumbnailUrl: String = ""
)

@Entity(tableName = "enrollments")
data class EnrollmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val studentId: String,
    val courseId: String,
    val status: String = "PENDING", // PENDING, APPROVED, REJECTED
    val enrollmentHistory: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "attendance")
data class AttendanceEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val courseId: String,
    val studentId: String,
    val date: String, // YYYY-MM-DD
    val status: String, // PRESENT, ABSENT, LATE
    val gpsVerified: Boolean = false,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)

@Entity(tableName = "assignments")
data class AssignmentEntity(
    @PrimaryKey val id: String,
    val courseId: String,
    val title: String,
    val description: String,
    val deadline: String, // YYYY-MM-DD HH:MM
    val maxMarks: Int,
    val instructions: String = "",
    val attachmentUrl: String = ""
)

@Entity(tableName = "assignment_submissions")
data class AssignmentSubmissionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val assignmentId: String,
    val studentId: String,
    val submittedText: String = "",
    val attachmentName: String = "",
    val attachmentUrl: String = "",
    val submittedAt: Long = System.currentTimeMillis(),
    val marksObtained: Double = -1.0, // -1 means un-graded
    val feedback: String = "",
    val status: String = "SUBMITTED" // SUBMITTED, GRADED, RETURNED
)

@Entity(tableName = "quizzes")
data class QuizEntity(
    @PrimaryKey val id: String,
    val courseId: String,
    val title: String,
    val durationMinutes: Int,
    val questionsJson: String // Serialized list of QuizQuestion
)

data class QuizQuestion(
    val id: String,
    val text: String,
    val type: String, // MCQ, TRUE_FALSE, SHORT_ANSWER, MULTIPLE_CORRECT
    val options: List<String> = emptyList(),
    val correctAnswers: List<Int> = emptyList(), // indices for MCQs/Multiple correct
    val correctAnswerText: String = "", // for short answers
    val negativeMarkingWeight: Double = 0.0
)

@Entity(tableName = "quiz_submissions")
data class QuizSubmissionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val quizId: String,
    val studentId: String,
    val score: Double,
    val totalQuestions: Int,
    val answersJson: String, // Mapping of question id to chosen answers
    val submittedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "timetables")
data class TimetableEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val courseId: String,
    val dayOfWeek: String, // MONDAY, TUESDAY, etc.
    val startTime: String, // HH:MM
    val endTime: String, // HH:MM
    val classroom: String,
    val liveStatus: String = "UPCOMING", // UPCOMING, LIVE, COMPLETED, MISSED
    val meetingLink: String = ""
)

@Entity(tableName = "fee_challans")
data class FeeChallanEntity(
    @PrimaryKey val id: String,
    val studentId: String,
    val title: String, // Tuition Fee, Exam Fee, etc.
    val amount: Double,
    val dueDate: String, // YYYY-MM-DD
    val scholarshipApplied: Double = 0.0,
    val discountApplied: Double = 0.0,
    val lateFeeApplied: Double = 0.0,
    val status: String = "PENDING", // PAID, PENDING
    val paidDate: String = "",
    val receiptId: String = ""
)

@Entity(tableName = "salary_slips")
data class SalarySlipEntity(
    @PrimaryKey val id: String,
    val employeeId: String, // Teacher or HOD ID
    val month: String, // e.g., "June 2026"
    val baseSalary: Double,
    val bonus: Double = 0.0,
    val allowance: Double = 0.0,
    val deduction: Double = 0.0,
    val status: String = "PENDING", // PAID, PENDING
    val paidDate: String = ""
)

@Entity(tableName = "exams")
data class ExamEntity(
    @PrimaryKey val id: String,
    val title: String, // e.g. Midterm Spring 2026
    val courseId: String,
    val date: String, // YYYY-MM-DD
    val time: String, // HH:MM
    val classroom: String,
    val invigilatorId: String,
    val seatingPlan: String = ""
)

@Entity(tableName = "exam_results")
data class ExamResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val examId: String,
    val studentId: String,
    val theoryMarks: Double,
    val practicalMarks: Double = 0.0,
    val vivaMarks: Double = 0.0,
    val totalMarks: Double,
    val grade: String = "F",
    val gpa: Double = 0.0
)

@Entity(tableName = "announcements")
data class AnnouncementEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val authorId: String,
    val authorName: String,
    val targetRole: String = "ALL", // ALL, STUDENT, TEACHER, HOD
    val targetDepartmentId: String? = null
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val senderId: String,
    val senderName: String,
    val recipientId: String? = null, // for direct chat
    val isGroup: Boolean = false,
    val groupId: String? = null, // e.g., courseId, departmentId
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val fileName: String? = null,
    val fileType: String? = null, // PDF, Image, etc.
    val isRead: Boolean = false
)
