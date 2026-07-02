package com.example.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // --- Users ---
    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserById(id: String): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE role = :role")
    fun getUsersByRole(role: String): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("DELETE FROM users WHERE id = :id")
    suspend fun deleteUserById(id: String)

    @Query("UPDATE users SET passwordHash = :newPasswordHash WHERE id = :id")
    suspend fun resetUserPassword(id: String, newPasswordHash: String)

    @Query("UPDATE users SET isActive = :isActive WHERE id = :id")
    suspend fun setUserActiveState(id: String, isActive: Boolean)

    @Query("SELECT * FROM users WHERE isRemembered = 1 LIMIT 1")
    suspend fun getRememberedUser(): UserEntity?

    @Query("UPDATE users SET isRemembered = 0")
    suspend fun clearRememberedUsers()

    // --- Departments ---
    @Query("SELECT * FROM departments")
    fun getAllDepartments(): Flow<List<DepartmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDepartment(department: DepartmentEntity)

    @Update
    suspend fun updateDepartment(department: DepartmentEntity)

    @Query("DELETE FROM departments WHERE id = :id")
    suspend fun deleteDepartmentById(id: String)

    // --- Courses ---
    @Query("SELECT * FROM courses")
    fun getAllCourses(): Flow<List<CourseEntity>>

    @Query("SELECT * FROM courses WHERE departmentId = :departmentId")
    fun getCoursesByDepartment(departmentId: String): Flow<List<CourseEntity>>

    @Query("SELECT * FROM courses WHERE teacherId = :teacherId")
    fun getCoursesByTeacher(teacherId: String): Flow<List<CourseEntity>>

    @Query("SELECT * FROM courses WHERE id = :id")
    suspend fun getCourseById(id: String): CourseEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(course: CourseEntity)

    @Update
    suspend fun updateCourse(course: CourseEntity)

    @Query("DELETE FROM courses WHERE id = :id")
    suspend fun deleteCourseById(id: String)

    // --- Enrollments ---
    @Query("SELECT * FROM enrollments")
    fun getAllEnrollments(): Flow<List<EnrollmentEntity>>

    @Query("SELECT * FROM enrollments WHERE studentId = :studentId")
    fun getEnrollmentsByStudent(studentId: String): Flow<List<EnrollmentEntity>>

    @Query("SELECT * FROM enrollments WHERE courseId = :courseId")
    fun getEnrollmentsByCourse(courseId: String): Flow<List<EnrollmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEnrollment(enrollment: EnrollmentEntity)

    @Query("UPDATE enrollments SET status = :status WHERE id = :id")
    suspend fun updateEnrollmentStatus(id: Int, status: String)

    @Query("DELETE FROM enrollments WHERE id = :id")
    suspend fun deleteEnrollment(id: Int)

    @Query("DELETE FROM enrollments WHERE studentId = :studentId AND courseId = :courseId")
    suspend fun deleteEnrollment(studentId: String, courseId: String)

    // --- Attendance ---
    @Query("SELECT * FROM attendance")
    fun getAllAttendance(): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance WHERE courseId = :courseId")
    fun getAttendanceByCourse(courseId: String): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance WHERE studentId = :studentId")
    fun getAttendanceByStudent(studentId: String): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance WHERE courseId = :courseId AND date = :date")
    fun getAttendanceByCourseAndDate(courseId: String, date: String): Flow<List<AttendanceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: AttendanceEntity)

    // --- Assignments ---
    @Query("SELECT * FROM assignments")
    fun getAllAssignments(): Flow<List<AssignmentEntity>>

    @Query("SELECT * FROM assignments WHERE courseId = :courseId")
    fun getAssignmentsByCourse(courseId: String): Flow<List<AssignmentEntity>>

    @Query("SELECT * FROM assignments WHERE id = :id")
    suspend fun getAssignmentById(id: String): AssignmentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssignment(assignment: AssignmentEntity)

    @Update
    suspend fun updateAssignment(assignment: AssignmentEntity)

    @Query("DELETE FROM assignments WHERE id = :id")
    suspend fun deleteAssignmentById(id: String)

    // --- Assignment Submissions ---
    @Query("SELECT * FROM assignment_submissions")
    fun getAllSubmissions(): Flow<List<AssignmentSubmissionEntity>>

    @Query("SELECT * FROM assignment_submissions WHERE assignmentId = :assignmentId")
    fun getSubmissionsByAssignment(assignmentId: String): Flow<List<AssignmentSubmissionEntity>>

    @Query("SELECT * FROM assignment_submissions WHERE studentId = :studentId")
    fun getSubmissionsByStudent(studentId: String): Flow<List<AssignmentSubmissionEntity>>

    @Query("SELECT * FROM assignment_submissions WHERE assignmentId = :assignmentId AND studentId = :studentId")
    suspend fun getSubmissionForStudent(assignmentId: String, studentId: String): AssignmentSubmissionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubmission(submission: AssignmentSubmissionEntity)

    @Query("UPDATE assignment_submissions SET marksObtained = :marks, feedback = :feedback, status = :status WHERE id = :id")
    suspend fun gradeSubmission(id: Int, marks: Double, feedback: String, status: String = "GRADED")

    // --- Quizzes ---
    @Query("SELECT * FROM quizzes")
    fun getAllQuizzes(): Flow<List<QuizEntity>>

    @Query("SELECT * FROM quizzes WHERE courseId = :courseId")
    fun getQuizzesByCourse(courseId: String): Flow<List<QuizEntity>>

    @Query("SELECT * FROM quizzes WHERE id = :id")
    suspend fun getQuizById(id: String): QuizEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuiz(quiz: QuizEntity)

    @Update
    suspend fun updateQuiz(quiz: QuizEntity)

    @Query("DELETE FROM quizzes WHERE id = :id")
    suspend fun deleteQuizById(id: String)

    // --- Quiz Submissions ---
    @Query("SELECT * FROM quiz_submissions")
    fun getAllQuizSubmissions(): Flow<List<QuizSubmissionEntity>>

    @Query("SELECT * FROM quiz_submissions WHERE studentId = :studentId")
    fun getQuizSubmissionsByStudent(studentId: String): Flow<List<QuizSubmissionEntity>>

    @Query("SELECT * FROM quiz_submissions WHERE quizId = :quizId")
    fun getQuizSubmissionsByQuiz(quizId: String): Flow<List<QuizSubmissionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuizSubmission(submission: QuizSubmissionEntity)

    // --- Timetable ---
    @Query("SELECT * FROM timetables")
    fun getAllTimetableEvents(): Flow<List<TimetableEntity>>

    @Query("SELECT * FROM timetables WHERE courseId = :courseId")
    fun getTimetableForCourse(courseId: String): Flow<List<TimetableEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimetableEvent(event: TimetableEntity)

    @Update
    suspend fun updateTimetableEvent(event: TimetableEntity)

    @Query("DELETE FROM timetables WHERE id = :id")
    suspend fun deleteTimetableEvent(id: Int)

    // --- Fee Challans ---
    @Query("SELECT * FROM fee_challans")
    fun getAllFeeChallans(): Flow<List<FeeChallanEntity>>

    @Query("SELECT * FROM fee_challans WHERE studentId = :studentId")
    fun getFeeChallansByStudent(studentId: String): Flow<List<FeeChallanEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeeChallan(challan: FeeChallanEntity)

    @Query("UPDATE fee_challans SET status = :status, paidDate = :paidDate, receiptId = :receiptId WHERE id = :id")
    suspend fun updateFeePayment(id: String, status: String, paidDate: String, receiptId: String)

    // --- Salary Slips ---
    @Query("SELECT * FROM salary_slips")
    fun getAllSalarySlips(): Flow<List<SalarySlipEntity>>

    @Query("SELECT * FROM salary_slips WHERE employeeId = :employeeId")
    fun getSalarySlipsByEmployee(employeeId: String): Flow<List<SalarySlipEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSalarySlip(slip: SalarySlipEntity)

    @Query("UPDATE salary_slips SET status = :status, paidDate = :paidDate WHERE id = :id")
    suspend fun updateSalaryPayment(id: String, status: String, paidDate: String)

    // --- Exams ---
    @Query("SELECT * FROM exams")
    fun getAllExams(): Flow<List<ExamEntity>>

    @Query("SELECT * FROM exams WHERE courseId = :courseId")
    fun getExamsByCourse(courseId: String): Flow<List<ExamEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExam(exam: ExamEntity)

    @Update
    suspend fun updateExam(exam: ExamEntity)

    @Query("DELETE FROM exams WHERE id = :id")
    suspend fun deleteExamById(id: String)

    // --- Exam Results ---
    @Query("SELECT * FROM exam_results")
    fun getAllExamResults(): Flow<List<ExamResultEntity>>

    @Query("SELECT * FROM exam_results WHERE studentId = :studentId")
    fun getExamResultsByStudent(studentId: String): Flow<List<ExamResultEntity>>

    @Query("SELECT * FROM exam_results WHERE examId = :examId")
    fun getExamResultsByExam(examId: String): Flow<List<ExamResultEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExamResult(result: ExamResultEntity)

    // --- Announcements ---
    @Query("SELECT * FROM announcements ORDER BY timestamp DESC")
    fun getAllAnnouncements(): Flow<List<AnnouncementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnouncement(announcement: AnnouncementEntity)

    @Query("DELETE FROM announcements WHERE id = :id")
    suspend fun deleteAnnouncementById(id: Int)

    // --- Chat Messages ---
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllChatMessages(): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages WHERE (senderId = :user1 AND recipientId = :user2) OR (senderId = :user2 AND recipientId = :user1) ORDER BY timestamp ASC")
    fun getDirectChat(user1: String, user2: String): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages WHERE isGroup = 1 AND groupId = :groupId ORDER BY timestamp ASC")
    fun getGroupChat(groupId: String): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(message: ChatMessageEntity)
}

@Database(
    entities = [
        UserEntity::class,
        DepartmentEntity::class,
        CourseEntity::class,
        EnrollmentEntity::class,
        AttendanceEntity::class,
        AssignmentEntity::class,
        AssignmentSubmissionEntity::class,
        QuizEntity::class,
        QuizSubmissionEntity::class,
        TimetableEntity::class,
        FeeChallanEntity::class,
        SalarySlipEntity::class,
        ExamEntity::class,
        ExamResultEntity::class,
        AnnouncementEntity::class,
        ChatMessageEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao
}
