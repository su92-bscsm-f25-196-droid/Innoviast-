package com.example.data.repository

import android.content.Context
import androidx.room.Room
import com.example.data.local.AppDao
import com.example.data.local.AppDatabase
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class LmsRepository(private val context: Context) {

    private val db: AppDatabase = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "edusphere_database"
    ).build()

    val dao: AppDao = db.appDao()

    // --- Exposed Observables (Flows) ---
    val allUsers: Flow<List<UserEntity>> = dao.getAllUsers()
    val allDepartments: Flow<List<DepartmentEntity>> = dao.getAllDepartments()
    val allCourses: Flow<List<CourseEntity>> = dao.getAllCourses()
    val allEnrollments: Flow<List<EnrollmentEntity>> = dao.getAllEnrollments()
    val allAttendance: Flow<List<AttendanceEntity>> = dao.getAllAttendance()
    val allAssignments: Flow<List<AssignmentEntity>> = dao.getAllAssignments()
    val allSubmissions: Flow<List<AssignmentSubmissionEntity>> = dao.getAllSubmissions()
    val allQuizzes: Flow<List<QuizEntity>> = dao.getAllQuizzes()
    val allQuizSubmissions: Flow<List<QuizSubmissionEntity>> = dao.getAllQuizSubmissions()
    val allTimetableEvents: Flow<List<TimetableEntity>> = dao.getAllTimetableEvents()
    val allFeeChallans: Flow<List<FeeChallanEntity>> = dao.getAllFeeChallans()
    val allSalarySlips: Flow<List<SalarySlipEntity>> = dao.getAllSalarySlips()
    val allExams: Flow<List<ExamEntity>> = dao.getAllExams()
    val allExamResults: Flow<List<ExamResultEntity>> = dao.getAllExamResults()
    val allAnnouncements: Flow<List<AnnouncementEntity>> = dao.getAllAnnouncements()
    val allChatMessages: Flow<List<ChatMessageEntity>> = dao.getAllChatMessages()

    suspend fun initDatabaseWithSeedData() = withContext(Dispatchers.IO) {
        val usersCount = dao.getAllUsers().first().size
        if (usersCount == 0) {
            // Seed Departments
            val csDept = DepartmentEntity("CS", "Computer Science", "CS", "Department of Computer Science and Engineering")
            val seDept = DepartmentEntity("SE", "Software Engineering", "SE", "Department of Modern Software Methodologies")
            val itDept = DepartmentEntity("IT", "Information Technology", "IT", "Department of Cloud Computing & Networks")
            val busDept = DepartmentEntity("BUS", "Business Administration", "BUS", "School of Business & Enterprise")
            val engDept = DepartmentEntity("ENG", "English Literature", "ENG", "Department of Languages & Linguistics")

            dao.insertDepartment(csDept)
            dao.insertDepartment(seDept)
            dao.insertDepartment(itDept)
            dao.insertDepartment(busDept)
            dao.insertDepartment(engDept)

            // Seed Users
            // Admin
            val adminUser = UserEntity(
                id = "admin",
                name = "Dr. Alice Smith",
                email = "admin@edusphere.edu",
                role = "ADMIN",
                passwordHash = "admin123",
                avatarUrl = "avatar_admin"
            )
            // HOD
            val csHod = UserEntity(
                id = "cs_hod",
                name = "Prof. Robert Downey",
                email = "hod.cs@edusphere.edu",
                role = "HOD",
                passwordHash = "hod123",
                departmentId = "CS",
                phone = "+1 (555) 019-2831",
                address = "Office 301, Tech Building",
                avatarUrl = "avatar_hod"
            )
            // Teachers
            val csTeacher = UserEntity(
                id = "cs_teacher",
                name = "Dr. Charles Xavier",
                email = "teacher.cs@edusphere.edu",
                role = "TEACHER",
                passwordHash = "teacher123",
                departmentId = "CS",
                phone = "+1 (555) 302-9182",
                address = "CS Lab 4B",
                avatarUrl = "avatar_teacher1"
            )
            val seTeacher = UserEntity(
                id = "se_teacher",
                name = "Sarah Connor",
                email = "teacher.se@edusphere.edu",
                role = "TEACHER",
                passwordHash = "teacher123",
                departmentId = "SE",
                phone = "+1 (555) 492-1284",
                address = "Design Lab 102",
                avatarUrl = "avatar_teacher2"
            )
            // Students
            val student1 = UserEntity(
                id = "student1",
                name = "John Doe",
                email = "student.john@edusphere.edu",
                role = "STUDENT",
                passwordHash = "student123",
                departmentId = "CS",
                phone = "+1 (555) 918-4029",
                address = "Residence Hall A, Rm 405",
                avatarUrl = "avatar_student1"
            )
            val student2 = UserEntity(
                id = "student2",
                name = "Jane Miller",
                email = "student.jane@edusphere.edu",
                role = "STUDENT",
                passwordHash = "student123",
                departmentId = "SE",
                phone = "+1 (555) 123-4567",
                address = "Residence Hall B, Rm 112",
                avatarUrl = "avatar_student2"
            )

            dao.insertUser(adminUser)
            dao.insertUser(csHod)
            dao.insertUser(csTeacher)
            dao.insertUser(seTeacher)
            dao.insertUser(student1)
            dao.insertUser(student2)

            // Seed Courses
            val course1 = CourseEntity(
                id = "CS-101",
                name = "Advanced Database Systems",
                code = "CS-101",
                creditHours = 4,
                semester = 4,
                departmentId = "CS",
                teacherId = "cs_teacher",
                description = "Deep dive into SQL, indexing, transactional database internals, query plans, and local Room persistence frameworks.",
                thumbnailUrl = "database_course"
            )
            val course2 = CourseEntity(
                id = "SE-201",
                name = "Software Architecture & Patterns",
                code = "SE-201",
                creditHours = 3,
                semester = 4,
                departmentId = "SE",
                teacherId = "se_teacher",
                description = "Master Design Patterns (Singleton, Factory, Observer) and architectural paradigms like MVVM, Clean Architecture, and Compose.",
                thumbnailUrl = "design_patterns_course"
            )
            val course3 = CourseEntity(
                id = "IT-301",
                name = "Cloud Computing Concepts",
                code = "IT-301",
                creditHours = 3,
                semester = 4,
                departmentId = "CS",
                teacherId = "cs_teacher",
                description = "Explore cloud infrastructures, containerization, microservices deployment, and serverless architectures.",
                thumbnailUrl = "cloud_course"
            )

            dao.insertCourse(course1)
            dao.insertCourse(course2)
            dao.insertCourse(course3)

            // Seed Enrollments
            dao.insertEnrollment(EnrollmentEntity(studentId = "student1", courseId = "CS-101", status = "APPROVED"))
            dao.insertEnrollment(EnrollmentEntity(studentId = "student1", courseId = "IT-301", status = "APPROVED"))
            dao.insertEnrollment(EnrollmentEntity(studentId = "student2", courseId = "SE-201", status = "APPROVED"))
            // Pending requests for demo
            dao.insertEnrollment(EnrollmentEntity(studentId = "student1", courseId = "SE-201", status = "PENDING", enrollmentHistory = "Applied on 2026-07-01"))
            dao.insertEnrollment(EnrollmentEntity(studentId = "student2", courseId = "CS-101", status = "PENDING", enrollmentHistory = "Applied on 2026-07-02"))

            // Seed Timetable Events
            dao.insertTimetableEvent(TimetableEntity(courseId = "CS-101", dayOfWeek = "MONDAY", startTime = "09:00", endTime = "11:00", classroom = "CS-Lab 1", liveStatus = "LIVE", meetingLink = "https://meet.google.com/abc-defg-hij"))
            dao.insertTimetableEvent(TimetableEntity(courseId = "SE-201", dayOfWeek = "TUESDAY", startTime = "11:00", endTime = "13:00", classroom = "Lecture Hall 402", liveStatus = "UPCOMING"))
            dao.insertTimetableEvent(TimetableEntity(courseId = "IT-301", dayOfWeek = "WEDNESDAY", startTime = "14:00", endTime = "15:30", classroom = "Tech Lab 3", liveStatus = "UPCOMING"))

            // Seed Assignments
            val csAssign = AssignmentEntity(
                id = "cs_assign_1",
                courseId = "CS-101",
                title = "Design an ERP Database Schema",
                description = "Provide an ER diagram and physical schema modeling for a modern university's students, teachers, classes, and fee challans.",
                deadline = "2026-07-15 23:59",
                maxMarks = 20,
                instructions = "Deliverable must be in PDF or PNG format. Include indexing structures."
            )
            val seAssign = AssignmentEntity(
                id = "se_assign_1",
                courseId = "SE-201",
                title = "Implement Decorator Design Pattern",
                description = "Write a fully functional Kotlin console program highlighting the open-closed design rule through wrapper modifiers.",
                deadline = "2026-07-20 23:59",
                maxMarks = 15,
                instructions = "Zip file containing source codes and a short execution PDF README."
            )

            dao.insertAssignment(csAssign)
            dao.insertAssignment(seAssign)

            // Seed Submissions
            dao.insertSubmission(
                AssignmentSubmissionEntity(
                    assignmentId = "cs_assign_1",
                    studentId = "student1",
                    submittedText = "Completed the database design. Added tables for fee, exams and course allocations. Included foreign keys.",
                    attachmentName = "student1_erd_schema.pdf",
                    submittedAt = System.currentTimeMillis() - 86400000,
                    status = "SUBMITTED"
                )
            )

            // Seed Quizzes
            val questionsJson = """
                [
                  {
                    "id": "q1",
                    "text": "What does '3NF' stand for in database normalization?",
                    "type": "MCQ",
                    "options": ["Third Normal Form", "Triple Network File", "Three Node Filtering", "Third Name Facility"],
                    "correctAnswers": [0],
                    "negativeMarkingWeight": 0.25
                  },
                  {
                    "id": "q2",
                    "text": "A primary key column can allow null values.",
                    "type": "TRUE_FALSE",
                    "options": ["True", "False"],
                    "correctAnswers": [1],
                    "negativeMarkingWeight": 0.0
                  },
                  {
                    "id": "q3",
                    "text": "Which clause is used to filter aggregated group records in SQL?",
                    "type": "MCQ",
                    "options": ["WHERE", "GROUP BY", "HAVING", "LIMIT"],
                    "correctAnswers": [2],
                    "negativeMarkingWeight": 0.0
                  },
                  {
                    "id": "q4",
                    "text": "Briefly state the main difference between SQL and NoSQL databases.",
                    "type": "SHORT_ANSWER",
                    "correctAnswerText": "SQL is relational and table-based with schema, while NoSQL is non-relational and document/key-value based with dynamic schema.",
                    "negativeMarkingWeight": 0.0
                  }
                ]
            """.trimIndent()

            val csQuiz = QuizEntity(
                id = "cs_quiz_1",
                courseId = "CS-101",
                title = "Relational Algebra & Normalization Fundamentals",
                durationMinutes = 15,
                questionsJson = questionsJson
            )
            dao.insertQuiz(csQuiz)

            // Seed Fee Challans
            dao.insertFeeChallan(FeeChallanEntity("challan_1", "student1", "Tuition Fee - Spring Semester 2026", 1200.0, "2026-07-20", scholarshipApplied = 200.0, status = "PENDING"))
            dao.insertFeeChallan(FeeChallanEntity("challan_2", "student1", "Library & Tech Resource Fee", 75.0, "2026-06-30", status = "PAID", paidDate = "2026-06-25", receiptId = "REC-8921A"))
            dao.insertFeeChallan(FeeChallanEntity("challan_3", "student2", "Tuition Fee - Spring Semester 2026", 1200.0, "2026-07-20", discountApplied = 100.0, status = "PAID", paidDate = "2026-06-29", receiptId = "REC-3029B"))

            // Seed Salary Slips
            dao.insertSalarySlip(SalarySlipEntity("slip_1", "cs_teacher", "June 2026", 4500.0, bonus = 250.0, deduction = 100.0, status = "PAID", paidDate = "2026-06-30"))
            dao.insertSalarySlip(SalarySlipEntity("slip_2", "se_teacher", "June 2026", 4500.0, status = "PAID", paidDate = "2026-06-30"))
            dao.insertSalarySlip(SalarySlipEntity("slip_3", "cs_hod", "June 2026", 6200.0, allowance = 400.0, status = "PAID", paidDate = "2026-06-30"))

            // Seed Exams
            dao.insertExam(ExamEntity("exam_1", "CS-101 Semester Finals", "CS-101", "2026-07-12", "09:00", "CS-Main Exam Hall", "cs_teacher", "Row A1 to F10"))
            dao.insertExam(ExamEntity("exam_2", "SE-201 Patterns Theory", "SE-201", "2026-07-14", "11:00", "LMS Hall 2", "se_teacher", "Row G1 to K12"))

            // Seed Results
            dao.insertExamResult(ExamResultEntity(examId = "exam_1", studentId = "student1", theoryMarks = 75.0, practicalMarks = 15.0, vivaMarks = 10.0, totalMarks = 100.0, grade = "A", gpa = 4.0))

            // Seed Announcements
            dao.insertAnnouncement(
                AnnouncementEntity(
                    title = "Annual University Coding Hackathon 2026",
                    content = "Register your team of 3-4 students for the annual hackathon. Massive prizes, developer goodies, and interview referrals from top tech companies. Registration ends on July 5th!",
                    authorId = "admin",
                    authorName = "Dr. Alice Smith",
                    targetRole = "ALL"
                )
            )
            dao.insertAnnouncement(
                AnnouncementEntity(
                    title = "Department Course Allocations Finalized",
                    content = "The HOD has finalized the syllabus alignments and course assignments for Semester 4. Please download the timetables and check your dashboards.",
                    authorId = "cs_hod",
                    authorName = "Prof. Robert Downey",
                    targetRole = "ALL",
                    targetDepartmentId = "CS"
                )
            )

            // Seed Chat Messages
            dao.insertChatMessage(
                ChatMessageEntity(
                    senderId = "cs_teacher",
                    senderName = "Dr. Charles Xavier",
                    isGroup = true,
                    groupId = "CS-101",
                    message = "Welcome to Advanced Database Systems! Slide documents for Lecture 1 (Transaction Internals) have been uploaded to the File Manager. Let's study!"
                )
            )
            dao.insertChatMessage(
                ChatMessageEntity(
                    senderId = "student1",
                    senderName = "John Doe",
                    isGroup = true,
                    groupId = "CS-101",
                    message = "Thanks Dr. Charles! Looking forward to SQL and Room. Will the assignments be PDF format?"
                )
            )
            dao.insertChatMessage(
                ChatMessageEntity(
                    senderId = "cs_teacher",
                    senderName = "Dr. Charles Xavier",
                    isGroup = true,
                    groupId = "CS-101",
                    message = "Yes, John! Submit a physical diagram/PDF on the Assignment module."
                )
            )
        }
    }
}
