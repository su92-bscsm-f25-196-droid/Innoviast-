package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.viewmodel.LmsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ==========================================
// 1. TIMETABLE SCREEN MODULE
// ==========================================
@Composable
fun TimetableScreen(currentUser: UserEntity, vm: LmsViewModel) {
    val timetableEvents by vm.timetableEvents.collectAsState()
    val courses by vm.courses.collectAsState()
    
    var selectedDayTab by remember { mutableStateOf("MONDAY") }
    val days = listOf("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY")

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Weekly Class Timetable", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text("Active classes and virtual links for current semester", style = MaterialTheme.typography.bodySmall, color = Color.Gray, modifier = Modifier.padding(bottom = 12.dp))

        // Days Filter Scrollable Chips
        ScrollableTabRow(
            selectedTabIndex = days.indexOf(selectedDayTab),
            edgePadding = 8.dp,
            modifier = Modifier.fillMaxWidth().testTag("timetable_days")
        ) {
            days.forEach { day ->
                Tab(
                    selected = selectedDayTab == day,
                    onClick = { selectedDayTab = day }
                ) {
                    Text(day.lowercase().capitalize(), modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val dayEvents = timetableEvents.filter { it.dayOfWeek == selectedDayTab }
        if (dayEvents.isEmpty()) {
            EmptyState(message = "No lectures scheduled on ${selectedDayTab.lowercase().capitalize()}. Enjoy your study break!")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(dayEvents) { event ->
                    val courseObj = courses.find { it.id == event.courseId }
                    
                    val isLive = event.liveStatus == "LIVE"
                    val cardBg = if (isLive) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    val cardBorderColor = if (isLive) Color.Red else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, cardBorderColor, RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = cardBg)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left Timing Frame
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(64.dp)) {
                                Text(event.startTime, fontWeight = FontWeight.Black, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                                Text("to", fontSize = 10.sp, color = Color.Gray)
                                Text(event.endTime, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color.Gray)
                            }

                            Spacer(modifier = Modifier.width(16.dp))
                            
                            // Middle Details
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(courseObj?.name ?: event.courseId, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    if (isLive) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Badge(containerColor = Color.Red, contentColor = Color.White) {
                                            Text("LIVE", fontWeight = FontWeight.Bold, fontSize = 8.sp)
                                        }
                                    }
                                }
                                Text("Room: ${event.classroom}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                Text("Course Code: ${event.courseId}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 2. ASSIGNMENTS MODULE (SUBMISSIONS / GRADES)
// ==========================================
@Composable
fun AssignmentScreen(currentUser: UserEntity, vm: LmsViewModel) {
    val assignments by vm.assignments.collectAsState()
    val submissions by vm.submissions.collectAsState()
    val courses by vm.courses.collectAsState()

    var showSubmitDialog by remember { mutableStateOf<AssignmentEntity?>(null) }
    var submissionText by remember { mutableStateOf("") }
    var submissionFile by remember { mutableStateOf("") }

    var selectedSubmissionForView by remember { mutableStateOf<AssignmentSubmissionEntity?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Course Assignments Desk", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text("Review due assignments, upload submissions, and track marks", style = MaterialTheme.typography.bodySmall, color = Color.Gray, modifier = Modifier.padding(bottom = 12.dp))

        if (currentUser.role == "STUDENT") {
            // Student View: Assignments they must do
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(assignments) { assign ->
                    val courseObj = courses.find { it.id == assign.courseId }
                    val userSub = submissions.find { it.assignmentId == assign.id && it.studentId == currentUser.id }
                    
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(assign.title, fontWeight = FontWeight.Bold)
                                    Text("Course: ${courseObj?.name ?: assign.courseId}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }
                                SuggestionChip(
                                    onClick = {},
                                    label = { Text("Max Marks: ${assign.maxMarks}") }
                                )
                            }
                            Text(assign.description, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp))
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Red)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Deadline: ${assign.deadline}", style = MaterialTheme.typography.labelSmall, color = Color.Red)
                            }

                            Divider(modifier = Modifier.padding(vertical = 12.dp))

                            if (userSub != null) {
                                // Already submitted
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.Green)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Submitted online", color = Color.Green, style = MaterialTheme.typography.bodySmall)
                                    }
                                    if (userSub.status == "GRADED") {
                                        Text("Graded: ${userSub.marksObtained}/${assign.maxMarks}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    } else {
                                        Text("Pending Grade", fontWeight = FontWeight.Bold, color = Color.Gray)
                                    }
                                }
                                if (userSub.feedback.isNotBlank()) {
                                    Text("Teacher Feedback: ${userSub.feedback}", style = MaterialTheme.typography.bodySmall, color = Color.Gray, modifier = Modifier.padding(top = 6.dp))
                                }
                            } else {
                                // Submit trigger
                                Button(
                                    onClick = { showSubmitDialog = assign },
                                    modifier = Modifier.align(Alignment.End).testTag("student_open_submit_dialog_${assign.id}")
                                ) {
                                    Text("Upload Submission")
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Admin or Teacher View: Submissions of students
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(submissions) { sub ->
                    val assignObj = assignments.find { it.id == sub.assignmentId }
                    val courseObj = courses.find { it.id == assignObj?.courseId }
                    
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Student UID: ${sub.studentId}", fontWeight = FontWeight.Bold)
                            Text("Assignment: ${assignObj?.title ?: sub.assignmentId}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            Text("Course: ${courseObj?.name ?: ""}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Submitted Document: ${sub.attachmentName}", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
                            
                            Divider(modifier = Modifier.padding(vertical = 8.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Status: ${sub.status}", fontWeight = FontWeight.Bold)
                                if (sub.status == "GRADED") {
                                    Text("Marks: ${sub.marksObtained}", fontWeight = FontWeight.Bold, color = Color.Green)
                                } else {
                                    Text("Requires Review", color = Color.Red, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Student Submit form
    if (showSubmitDialog != null) {
        val assign = showSubmitDialog!!
        AlertDialog(
            onDismissRequest = { showSubmitDialog = null },
            title = { Text("Upload Assignment: ${assign.title}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Provide project notes or explanation details below:")
                    OutlinedTextField(
                        value = submissionText,
                        onValueChange = { submissionText = it },
                        label = { Text("Submission Text Notes") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 4
                    )
                    OutlinedTextField(
                        value = submissionFile,
                        onValueChange = { submissionFile = it },
                        label = { Text("Simulated File Name (e.g. solution.pdf)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Supported Formats: PDF, DOCX, ZIP, PNG", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val fName = if (submissionFile.isNotBlank()) submissionFile else "assignment_project.pdf"
                        vm.submitAssignmentOnline(assign.id, currentUser.id, submissionText, fName)
                        showSubmitDialog = null
                        submissionText = ""
                        submissionFile = ""
                    },
                    modifier = Modifier.testTag("student_submit_assignment_final")
                ) {
                    Text("Finalize Submission")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSubmitDialog = null }) { Text("Cancel") }
            }
        )
    }
}

// ==========================================
// 3. QUIZ MODULE (INTERACTIVE PLAYER WITH TIMER)
// ==========================================
@Composable
fun QuizScreen(currentUser: UserEntity, vm: LmsViewModel) {
    val quizzes by vm.quizzes.collectAsState()
    val courses by vm.courses.collectAsState()
    val submissions by vm.quizSubmissions.collectAsState()

    var activeQuizForTest by remember { mutableStateOf<QuizEntity?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Online Quiz Assessment Module", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text("Take dynamic interactive quizzes with timed auto-grading engine", style = MaterialTheme.typography.bodySmall, color = Color.Gray, modifier = Modifier.padding(bottom = 12.dp))

        if (activeQuizForTest != null) {
            // Render the live interactive Quiz Player!
            QuizPlayer(
                quiz = activeQuizForTest!!,
                studentId = currentUser.id,
                vm = vm,
                onFinished = { activeQuizForTest = null }
            )
        } else {
            // List active quizzes
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(quizzes) { quiz ->
                    val courseObj = courses.find { it.id == quiz.courseId }
                    val userSub = submissions.find { it.quizId == quiz.id && it.studentId == currentUser.id }

                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(quiz.title, fontWeight = FontWeight.Bold)
                                    Text("Course: ${courseObj?.name ?: quiz.courseId}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }
                                SuggestionChip(
                                    onClick = {},
                                    label = { Text("${quiz.durationMinutes} Mins") }
                                )
                            }

                            Divider(modifier = Modifier.padding(vertical = 12.dp))

                            if (userSub != null) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.Green)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Attempted", color = Color.Green, fontWeight = FontWeight.Bold)
                                    }
                                    Text("Score: ${userSub.score}/${userSub.totalQuestions * 10}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                                }
                            } else {
                                Button(
                                    onClick = { activeQuizForTest = quiz },
                                    modifier = Modifier.align(Alignment.End).testTag("start_quiz_${quiz.id}")
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Attempt Quiz")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Interactive Live Quiz Play Module with active Countdown timer
@Composable
fun QuizPlayer(
    quiz: QuizEntity,
    studentId: String,
    vm: LmsViewModel,
    onFinished: () -> Unit
) {
    val moshi = com.squareup.moshi.Moshi.Builder().add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory()).build()
    val listType = com.squareup.moshi.Types.newParameterizedType(List::class.java, Map::class.java)
    val adapter: com.squareup.moshi.JsonAdapter<List<Map<String, Any>>> = moshi.adapter(listType)
    
    val questions: List<QuizQuestion> = remember(quiz) {
        try {
            val rawList: List<Map<String, Any>> = adapter.fromJson(quiz.questionsJson) ?: emptyList()
            rawList.map { map ->
                val id = map["id"] as? String ?: ""
                val text = map["text"] as? String ?: ""
                val type = map["type"] as? String ?: "MCQ"
                val options = (map["options"] as? List<*>)?.map { it.toString() } ?: emptyList()
                val correctAnswers = (map["correctAnswers"] as? List<*>)?.map { (it as Double).toInt() } ?: emptyList()
                val correctAnswerText = map["correctAnswerText"] as? String ?: ""
                val negativeWeight = map["negativeMarkingWeight"] as? Double ?: 0.0
                QuizQuestion(id, text, type, options, correctAnswers, correctAnswerText, negativeWeight)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    var currentQuestionIndex by remember { mutableStateOf(0) }
    val selectedAnswers = remember { mutableStateMapOf<String, List<Int>>() }
    val shortAnswers = remember { mutableStateMapOf<String, String>() }
    
    // Live Timer state
    var timeLeftSeconds by remember { mutableStateOf(quiz.durationMinutes * 60) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(key1 = quiz) {
        while (timeLeftSeconds > 0) {
            delay(1000)
            timeLeftSeconds--
        }
        // Auto submit on time exhaustion!
        val score = calculateScore(questions, selectedAnswers, shortAnswers)
        vm.submitQuizScore(quiz.id, studentId, score, questions.size, selectedAnswers.toMap())
        onFinished()
    }

    if (questions.isEmpty()) {
        Card { Text("Error loading quiz questions.") }
        return
    }

    val currentQuestion = questions[currentQuestionIndex]

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Timer and Progress
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Question ${currentQuestionIndex + 1} of ${questions.size}",
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
                
                // Red Warning timer if under 60 seconds
                val timerColor = if (timeLeftSeconds < 60) Color.Red else MaterialTheme.colorScheme.primary
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.HourglassEmpty, contentDescription = null, tint = timerColor, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = String.format("%02d:%02d", timeLeftSeconds / 60, timeLeftSeconds % 60),
                        fontWeight = FontWeight.Black,
                        color = timerColor,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            LinearProgressIndicator(
                progress = (currentQuestionIndex + 1).toFloat() / questions.size,
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
            )

            // Question Text
            Text(
                text = currentQuestion.text,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Answers Selection Render based on Type
            when (currentQuestion.type) {
                "MCQ", "TRUE_FALSE" -> {
                    currentQuestion.options.forEachIndexed { optIdx, option ->
                        val isSelected = selectedAnswers[currentQuestion.id]?.contains(optIdx) == true
                        OutlinedButton(
                            onClick = {
                                selectedAnswers[currentQuestion.id] = listOf(optIdx) // MCQ allows single pick
                            },
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).testTag("quiz_opt_${optIdx}")
                        ) {
                            Text(option, textAlign = TextAlign.Left, modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
                "SHORT_ANSWER" -> {
                    OutlinedTextField(
                        value = shortAnswers[currentQuestion.id] ?: "",
                        onValueChange = { shortAnswers[currentQuestion.id] = it },
                        label = { Text("Write short explanation answer") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Footer controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(
                    onClick = { if (currentQuestionIndex > 0) currentQuestionIndex-- },
                    enabled = currentQuestionIndex > 0
                ) {
                    Text("Previous")
                }

                if (currentQuestionIndex == questions.size - 1) {
                    Button(
                        onClick = {
                            val score = calculateScore(questions, selectedAnswers, shortAnswers)
                            vm.submitQuizScore(quiz.id, studentId, score, questions.size, selectedAnswers.toMap())
                            onFinished()
                        },
                        modifier = Modifier.testTag("submit_quiz_answers")
                    ) {
                        Text("Submit Test Answers")
                    }
                } else {
                    Button(
                        onClick = { currentQuestionIndex++ },
                        modifier = Modifier.testTag("quiz_next_question")
                    ) {
                        Text("Next Question")
                    }
                }
            }
        }
    }
}

private fun calculateScore(
    questions: List<QuizQuestion>,
    answers: Map<String, List<Int>>,
    shorts: Map<String, String>
): Double {
    var score = 0.0
    questions.forEach { q ->
        if (q.type == "MCQ" || q.type == "TRUE_FALSE") {
            val studentPick = answers[q.id]
            if (studentPick == q.correctAnswers) {
                score += 10.0 // 10 points per question
            } else if (studentPick != null && q.negativeMarkingWeight > 0.0) {
                score -= q.negativeMarkingWeight * 10.0 // Negative penalty
            }
        } else if (q.type == "SHORT_ANSWER") {
            val studentText = shorts[q.id]?.trim() ?: ""
            if (studentText.isNotBlank()) {
                score += 8.0 // Partial auto-grade placeholder
            }
        }
    }
    return Math.max(0.0, score)
}

// ==========================================
// 4. EXAMINATION MODULE (GPAS / SCHEDULERS)
// ==========================================
@Composable
fun ExaminationScreen(currentUser: UserEntity, vm: LmsViewModel) {
    val exams by vm.exams.collectAsState()
    val examResults by vm.examResults.collectAsState()
    val courses by vm.courses.collectAsState()

    var showSeatPlanForExam by remember { mutableStateOf<ExamEntity?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Examinations & CGPA Registry", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text("View seating plans, datesheets, and institutional GPA scorecards", style = MaterialTheme.typography.bodySmall, color = Color.Gray, modifier = Modifier.padding(bottom = 12.dp))

        if (currentUser.role == "STUDENT") {
            // Dynamic GPA Calculator display for John Doe
            val studentResults = examResults.filter { it.studentId == currentUser.id }
            val avgGpa = if (studentResults.isNotEmpty()) studentResults.map { it.gpa }.average() else 3.82

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Active Semester CGPA", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text("Average Grade: A- / Excellent Standings", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                    Text(
                        text = String.format("%.2f", avgGpa),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        Text("Official Datesheet & Locations", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(exams) { exam ->
                val cObj = courses.find { it.id == exam.courseId }
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(exam.title, fontWeight = FontWeight.Bold)
                                Text("Course: ${cObj?.name ?: exam.courseId}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                            IconButton(onClick = { showSeatPlanForExam = exam }) {
                                Icon(Icons.Default.AirlineSeatReclineNormal, contentDescription = "Seat plan")
                            }
                        }
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text("Date: ${exam.date}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Text("Time: ${exam.time}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Text("Hall: ${exam.classroom}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }

    if (showSeatPlanForExam != null) {
        val ex = showSeatPlanForExam!!
        AlertDialog(
            onDismissRequest = { showSeatPlanForExam = null },
            title = { Text("Seating Arrangement Allocation") },
            text = {
                Column {
                    Text("Exam Hall: ${ex.classroom}", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Sector Row: ${ex.seatingPlan}", fontSize = 18.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Ensure to bring your digital university clearance card and physical student ID.")
                }
            },
            confirmButton = { Button(onClick = { showSeatPlanForExam = null }) { Text("Dismiss") } }
        )
    }
}

// ==========================================
// 5. FINANCE ERP (FEE CHALLANS / SALARIES)
// ==========================================
@Composable
fun FinanceScreen(currentUser: UserEntity, vm: LmsViewModel) {
    val challans by vm.feeChallans.collectAsState()
    val slips by vm.salarySlips.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("ERP Financial Clearing Portal", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text("Dues clearances, online payments, receipts, and salary receipts", style = MaterialTheme.typography.bodySmall, color = Color.Gray, modifier = Modifier.padding(bottom = 12.dp))

        if (currentUser.role == "STUDENT") {
            // Student view of fees
            val studentChallans = challans.filter { it.studentId == currentUser.id }
            
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(studentChallans) { challan ->
                    val isPending = challan.status == "PENDING"
                    val bColor = if (isPending) Color.Red else Color.Green
                    val containerBg = if (isPending) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)

                    Card(
                        modifier = Modifier.fillMaxWidth().border(1.dp, bColor, RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = containerBg)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(challan.title, fontWeight = FontWeight.Bold)
                                    Text("Challan Code: ${challan.id}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }
                                Text("$${challan.amount}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            
                            if (challan.scholarshipApplied > 0.0) {
                                Text("Scholarship deduction: -$${challan.scholarshipApplied}", style = MaterialTheme.typography.labelSmall, color = Color.Green)
                            }
                            if (challan.discountApplied > 0.0) {
                                Text("Discount deduction: -$${challan.discountApplied}", style = MaterialTheme.typography.labelSmall, color = Color.Green)
                            }

                            Divider(modifier = Modifier.padding(vertical = 12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(8.dp).background(bColor, CircleShape))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isPending) "Pending Clearance | Due: ${challan.dueDate}" else "Cleared on ${challan.paidDate}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = bColor
                                    )
                                }
                                
                                if (isPending) {
                                    Button(
                                        onClick = { vm.payFeeChallan(challan.id) },
                                        modifier = Modifier.testTag("pay_challan_${challan.id}")
                                    ) {
                                        Text("Pay Dues Now")
                                    }
                                } else {
                                    Text("Receipt: ${challan.receiptId}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Teacher or HOD views payroll slips
            val mySlips = slips.filter { it.employeeId == currentUser.id }
            
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(mySlips) { slip ->
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Payroll Slip: ${slip.month}", fontWeight = FontWeight.Bold)
                                    Text("Ref ID: ${slip.id}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }
                                Text("$${slip.baseSalary + slip.bonus - slip.deduction}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.Green)
                            }
                            Divider(modifier = Modifier.padding(vertical = 12.dp))
                            Text("Base: $${slip.baseSalary} | Allowances: $${slip.allowance} | Deductions: -$${slip.deduction}", style = MaterialTheme.typography.bodySmall)
                            Text("Status: ${slip.status}", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 6. ACADEMIC PLANNER SCREEN
// ==========================================
@Composable
fun AcademicPlannerScreen(currentUser: UserEntity, vm: LmsViewModel) {
    val events = listOf(
        Triple("Summer Vacations 2026", "2026-07-04 to 2026-08-15", "HOLIDAY"),
        Triple("LMS System Migration", "2026-07-06", "SEMINAR"),
        Triple("Semester End Exams", "2026-07-10 to 2026-07-20", "EXAM"),
        Triple("Annual Graduation Ceremony", "2026-08-25", "EVENT")
    )

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Academic Calendar Planner", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text("Upcoming institutional events, exams and holiday schedules", style = MaterialTheme.typography.bodySmall, color = Color.Gray, modifier = Modifier.padding(bottom = 12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(events) { (title, date, type) ->
                val typeColor = when (type) {
                    "HOLIDAY" -> Color(0xFFE91E63)
                    "SEMINAR" -> Color(0xFF2196F3)
                    "EXAM" -> Color(0xFFFF9800)
                    else -> Color(0xFF4CAF50)
                }

                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(typeColor, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(title, fontWeight = FontWeight.Bold)
                            Text("Schedules: $date", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            Text("Categorization: $type", style = MaterialTheme.typography.labelSmall, color = typeColor, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}
