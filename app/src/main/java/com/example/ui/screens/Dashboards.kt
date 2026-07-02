package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.*
import com.example.ui.viewmodel.LmsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// --- Main Dashboards Manager Switcher ---
@Composable
fun DashboardManagerScreen(
    currentUser: UserEntity,
    lmsViewModel: LmsViewModel,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when (currentUser.role) {
            "ADMIN" -> AdminDashboard(currentUser, lmsViewModel)
            "HOD" -> HodDashboard(currentUser, lmsViewModel)
            "TEACHER" -> TeacherDashboard(currentUser, lmsViewModel)
            "STUDENT" -> StudentDashboard(currentUser, lmsViewModel)
            else -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Role configuration error. Contact administration.")
                }
            }
        }
    }
}

// ==========================================
// 1. ADMIN DASHBOARD WORKSPACE
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboard(admin: UserEntity, vm: LmsViewModel) {
    val users by vm.users.collectAsState()
    val courses by vm.courses.collectAsState()
    val depts by vm.departments.collectAsState()
    val enrollments by vm.enrollments.collectAsState()
    
    var currentSubTab by remember { mutableStateOf("OVERVIEW") } // OVERVIEW, USERS, DEPTS_COURSES, ENROLLMENTS
    var showUserCreateDialog by remember { mutableStateOf(false) }
    var showCourseCreateDialog by remember { mutableStateOf(false) }

    // State holders for User Account creation
    var newUserId by remember { mutableStateOf("") }
    var newUserName by remember { mutableStateOf("") }
    var newUserEmail by remember { mutableStateOf("") }
    var newUserRole by remember { mutableStateOf("STUDENT") }
    var newUserPass by remember { mutableStateOf("123456") }
    var newUserDept by remember { mutableStateOf("CS") }

    // State holders for Course creation
    var newCourseId by remember { mutableStateOf("") }
    var newCourseName by remember { mutableStateOf("") }
    var newCourseCode by remember { mutableStateOf("") }
    var newCourseCredits by remember { mutableStateOf(3) }
    var newCourseSemester by remember { mutableStateOf(4) }
    var newCourseTeacher by remember { mutableStateOf("cs_teacher") }
    var newCourseDept by remember { mutableStateOf("CS") }
    var newCourseDesc by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        // Tab Controls
        ScrollableTabRow(
            selectedTabIndex = when (currentSubTab) {
                "OVERVIEW" -> 0
                "USERS" -> 1
                "DEPTS_COURSES" -> 2
                "ENROLLMENTS" -> 3
                else -> 0
            },
            edgePadding = 16.dp,
            modifier = Modifier.fillMaxWidth().testTag("admin_sub_tabs")
        ) {
            Tab(selected = currentSubTab == "OVERVIEW", onClick = { currentSubTab = "OVERVIEW" }) {
                Text("Stats & Analytics", modifier = Modifier.padding(14.dp), fontWeight = FontWeight.Bold)
            }
            Tab(selected = currentSubTab == "USERS", onClick = { currentSubTab = "USERS" }) {
                Text("User Accounts", modifier = Modifier.padding(14.dp), fontWeight = FontWeight.Bold)
            }
            Tab(selected = currentSubTab == "DEPTS_COURSES", onClick = { currentSubTab = "DEPTS_COURSES" }) {
                Text("Syllabus Registry", modifier = Modifier.padding(14.dp), fontWeight = FontWeight.Bold)
            }
            Tab(selected = currentSubTab == "ENROLLMENTS", onClick = { currentSubTab = "ENROLLMENTS" }) {
                val pendingCount = enrollments.count { it.status == "PENDING" }
                BadgedBox(badge = {
                    if (pendingCount > 0) Badge { Text("$pendingCount") }
                }) {
                    Text("Admissions", modifier = Modifier.padding(horizontal = 8.dp, vertical = 14.dp), fontWeight = FontWeight.Bold)
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (currentSubTab) {
                "OVERVIEW" -> {
                    item {
                        Text(
                            text = "Executive ERP Control Center",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Count metrics
                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            DashboardStatsCard(
                                title = "Students",
                                value = users.count { it.role == "STUDENT" }.toString(),
                                icon = Icons.Default.People,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.weight(1f)
                            )
                            DashboardStatsCard(
                                title = "Faculty",
                                value = users.count { it.role == "TEACHER" }.toString(),
                                icon = Icons.Default.School,
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            DashboardStatsCard(
                                title = "Syllabus Courses",
                                value = courses.size.toString(),
                                icon = Icons.Default.Book,
                                color = MaterialTheme.colorScheme.tertiaryContainer,
                                modifier = Modifier.weight(1f)
                            )
                            DashboardStatsCard(
                                title = "Pending Admits",
                                value = enrollments.count { it.status == "PENDING" }.toString(),
                                icon = Icons.Default.HourglassEmpty,
                                color = MaterialTheme.colorScheme.errorContainer,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Custom Canvas charts
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Financial Stream: Tuition Receipts vs Salaries",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )
                                InteractiveBarChart(
                                    barData = listOf(
                                        "Jan-Feb" to 75f,
                                        "Mar-Apr" to 90f,
                                        "May-Jun" to 65f,
                                        "Jul-Aug" to 85f
                                    ),
                                    yMax = 100f
                                )
                            }
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Interactive GPA Distribution Graph",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    InteractivePieChart(
                                        values = listOf(40f, 35f, 15f, 7f, 3f)
                                    )
                                    Column {
                                        LegendChip(Color(0xFF4CAF50), "Grade A (40%)")
                                        LegendChip(Color(0xFF2196F3), "Grade B (35%)")
                                        LegendChip(Color(0xFFFFC107), "Grade C (15%)")
                                        LegendChip(Color(0xFFE91E63), "Grade D (7%)")
                                        LegendChip(Color(0xFF9C27B0), "Grade F (3%)")
                                    }
                                }
                            }
                        }
                    }
                }

                "USERS" -> {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Account Directory", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Button(
                                onClick = { showUserCreateDialog = true },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("admin_add_user_btn")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("New Account")
                            }
                        }
                    }

                    items(users) { usr ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Mini avatar representation
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = usr.name.firstOrNull()?.uppercase().toString(),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                
                                Spacer(modifier = Modifier.width(12.dp))
                                
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(usr.name, fontWeight = FontWeight.Bold)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        SuggestionChip(
                                            onClick = {},
                                            label = { Text(usr.role) },
                                            modifier = Modifier.padding(end = 4.dp)
                                        )
                                        if (usr.departmentId != null) {
                                            Text(usr.departmentId, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                        }
                                    }
                                    Text(usr.email, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Row {
                                        Switch(
                                            checked = usr.isActive,
                                            onCheckedChange = { vm.setUserActiveState(usr.id, it) },
                                            modifier = Modifier.testTag("user_active_toggle_${usr.id}")
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        IconButton(onClick = { vm.deleteAccount(usr.id) }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                                        }
                                    }
                                    TextButton(onClick = { vm.resetPassword(usr.id, "123456") }) {
                                        Text("Reset Pass", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                "DEPTS_COURSES" -> {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Courses Management", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Button(onClick = { showCourseCreateDialog = true }) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("New Course")
                            }
                        }
                    }

                    item {
                        Text("Existing Academic Departments", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = Color.Gray)
                    }

                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            depts.forEach { dept ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(dept.code, fontWeight = FontWeight.Black)
                                        Text(dept.name, fontSize = 9.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Text("Active Courses Directory", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = Color.Gray)
                    }

                    items(courses) { course ->
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(course.name, fontWeight = FontWeight.Bold)
                                        Text("Code: ${course.code} | Credits: ${course.creditHours} Hrs", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                    }
                                    IconButton(onClick = { vm.deleteCourse(course.id) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                                    }
                                }
                                Text(course.description, style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Teacher ID: ${course.teacherId ?: "UNASSIGNED"}", style = MaterialTheme.typography.labelSmall, color = Color.DarkGray)
                                }
                            }
                        }
                    }
                }

                "ENROLLMENTS" -> {
                    item {
                        Text("Admissions Enrollment Requests", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }

                    val pendingEnrollments = enrollments.filter { it.status == "PENDING" }
                    if (pendingEnrollments.isEmpty()) {
                        item {
                            EmptyState(message = "No pending admissions requests on file.")
                        }
                    } else {
                        items(pendingEnrollments) { enroll ->
                            val userObj = users.find { it.id == enroll.studentId }
                            val courseObj = courses.find { it.id == enroll.courseId }
                            
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Student: ${userObj?.name ?: enroll.studentId}", fontWeight = FontWeight.Bold)
                                    Text("Requested Course: ${courseObj?.name ?: enroll.courseId} (${enroll.courseId})", style = MaterialTheme.typography.bodySmall)
                                    Text("History: ${enroll.enrollmentHistory}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                    
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        FilledTonalButton(
                                            onClick = { vm.rejectEnrollment(enroll.id) },
                                            colors = ButtonDefaults.filledTonalButtonColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                                            modifier = Modifier.testTag("enroll_reject_${enroll.id}")
                                        ) {
                                            Text("Reject", color = MaterialTheme.colorScheme.onErrorContainer)
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Button(
                                            onClick = { vm.approveEnrollment(enroll.id) },
                                            modifier = Modifier.testTag("enroll_approve_${enroll.id}")
                                        ) {
                                            Text("Approve Admissions")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal: User Account Creation
    if (showUserCreateDialog) {
        AlertDialog(
            onDismissRequest = { showUserCreateDialog = false },
            title = { Text("Register ERP Account") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = newUserId, onValueChange = { newUserId = it }, label = { Text("Account UID / Login Username") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = newUserName, onValueChange = { newUserName = it }, label = { Text("Display Full Name") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = newUserEmail, onValueChange = { newUserEmail = it }, label = { Text("Institutional Email Address") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = newUserPass, onValueChange = { newUserPass = it }, label = { Text("Login Password") }, modifier = Modifier.fillMaxWidth())
                    
                    Text("Select User Role Cluster:")
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                        listOf("STUDENT", "TEACHER", "HOD", "ADMIN").forEach { role ->
                            FilterChip(
                                selected = newUserRole == role,
                                onClick = { newUserRole = role },
                                label = { Text(role) }
                            )
                        }
                    }
                    OutlinedTextField(value = newUserDept, onValueChange = { newUserDept = it }, label = { Text("Assigned Department (e.g. CS, SE)") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        vm.createAccount(newUserId, newUserName, newUserEmail, newUserRole, newUserPass, newUserDept)
                        showUserCreateDialog = false
                    },
                    modifier = Modifier.testTag("admin_dialog_save_user")
                ) {
                    Text("Create Registry")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUserCreateDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Modal: Course Creation
    if (showCourseCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCourseCreateDialog = false },
            title = { Text("Draft Syllabus Course") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = newCourseId, onValueChange = { newCourseId = it }, label = { Text("Course UUID (e.g., CS-101)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = newCourseName, onValueChange = { newCourseName = it }, label = { Text("Full Syllabus Course Name") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = newCourseCode, onValueChange = { newCourseCode = it }, label = { Text("Display Code (e.g. CS101)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = newCourseCredits.toString(), onValueChange = { newCourseCredits = it.toIntOrNull() ?: 3 }, label = { Text("Credit Hours (e.g. 3, 4)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = newCourseSemester.toString(), onValueChange = { newCourseSemester = it.toIntOrNull() ?: 4 }, label = { Text("Syllabus Semester Allocation") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = newCourseTeacher, onValueChange = { newCourseTeacher = it }, label = { Text("Assigned Teacher login ID") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = newCourseDept, onValueChange = { newCourseDept = it }, label = { Text("Assigned Department (e.g. CS)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = newCourseDesc, onValueChange = { newCourseDesc = it }, label = { Text("Course Overview & Syllabus") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        vm.addCourse(newCourseId, newCourseName, newCourseCode, newCourseCredits, newCourseSemester, newCourseDept, newCourseTeacher, newCourseDesc)
                        showCourseCreateDialog = false
                    }
                ) {
                    Text("Register Course")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCourseCreateDialog = false }) { Text("Cancel") }
            }
        )
    }
}

// ==========================================
// 2. HOD DASHBOARD WORKSPACE (Head Of Department)
// ==========================================
@Composable
fun HodDashboard(hod: UserEntity, vm: LmsViewModel) {
    val users by vm.users.collectAsState()
    val courses by vm.courses.collectAsState()
    val timetables by vm.timetableEvents.collectAsState()
    val slips by vm.salarySlips.collectAsState()

    val deptId = hod.departmentId ?: "CS"
    val deptTeachers = users.filter { it.role == "TEACHER" && it.departmentId == deptId }
    val deptStudents = users.filter { it.role == "STUDENT" && it.departmentId == deptId }
    val deptCourses = courses.filter { it.departmentId == deptId }
    
    var showSlipDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "${deptId} Department Head Supervisor Workspace",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Stats Row
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DashboardStatsCard(
                    title = "Students",
                    value = deptStudents.size.toString(),
                    icon = Icons.Default.People,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.weight(1f)
                )
                DashboardStatsCard(
                    title = "Faculty Teachers",
                    value = deptTeachers.size.toString(),
                    icon = Icons.Default.School,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.weight(1f)
                )
                DashboardStatsCard(
                    title = "Syllabi",
                    value = deptCourses.size.toString(),
                    icon = Icons.Default.Book,
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // HOD Salary Slips Access
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("My Payroll Salary Slip", fontWeight = FontWeight.Bold)
                        Text("Download VC slip statements for current cycle", fontSize = 12.sp, color = Color.Gray)
                    }
                    Button(onClick = { showSlipDialog = true }) {
                        Icon(Icons.Default.Download, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Open Slip")
                    }
                }
            }
        }

        // Monitor Attendance Trends
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Department Weekly Attendance Tracker", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
                    InteractiveLineChart(
                        points = listOf(92f, 95f, 88f, 96f, 91f),
                        labels = listOf("Mon", "Tue", "Wed", "Thu", "Fri")
                    )
                }
            }
        }

        // HOD Course Allocations Review
        item {
            Text("Assigned Department Faculty Courses", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        }

        items(deptCourses) { course ->
            val teacherObj = deptTeachers.find { it.id == course.teacherId }
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Class, contentDescription = null, modifier = Modifier.size(36.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(course.name, fontWeight = FontWeight.Bold)
                        Text("Code: ${course.code} | Semester: ${course.semester}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        Text("Faculty Instructor: ${teacherObj?.name ?: "No Teacher Assigned"}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    }
                    Button(onClick = { /* approve course allocation */ }) {
                        Text("Approved")
                    }
                }
            }
        }
    }

    if (showSlipDialog) {
        val slip = slips.find { it.employeeId == hod.id }
        AlertDialog(
            onDismissRequest = { showSlipDialog = false },
            title = { Text("Institutional HOD Salary Receipt") },
            text = {
                if (slip != null) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Employee ID: ${slip.employeeId}", fontWeight = FontWeight.Bold)
                        Text("Billing Month: ${slip.month}", fontWeight = FontWeight.Bold)
                        Text("Basic Monthly Salary: $${slip.baseSalary}")
                        Text("Special Duty Allowance: +$${slip.allowance}")
                        Text("Bonus Allocations: +$${slip.bonus}")
                        Text("Tax Deductions: -$${slip.deduction}")
                        Divider()
                        Text("Net Salary Disbursed: $${slip.baseSalary + slip.allowance + slip.bonus - slip.deduction}", fontWeight = FontWeight.Black, color = Color.Green)
                        Text("Disbursement Status: ${slip.status}", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Text("No payroll record generated for this month.")
                }
            },
            confirmButton = {
                Button(onClick = { showSlipDialog = false }) {
                    Text("Done")
                }
            }
        )
    }
}

// ==========================================
// 3. TEACHER DASHBOARD WORKSPACE
// ==========================================
@Composable
fun TeacherDashboard(teacher: UserEntity, vm: LmsViewModel) {
    val courses by vm.courses.collectAsState()
    val submissions by vm.submissions.collectAsState()
    val students by vm.users.collectAsState()
    val slips by vm.salarySlips.collectAsState()

    val myCourses = courses.filter { it.teacherId == teacher.id }
    
    var selectedCourseForAttendance by remember { mutableStateOf<CourseEntity?>(null) }
    var selectedSubmissionForGrading by remember { mutableStateOf<AssignmentSubmissionEntity?>(null) }
    
    var showSalaryDialog by remember { mutableStateOf(false) }
    var showLiveClassDialog by remember { mutableStateOf(false) }
    var liveCourseId by remember { mutableStateOf("") }

    var gradingMarks by remember { mutableStateOf("") }
    var gradingFeedback by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Faculty Academic Desk: Welcome ${teacher.name}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Quick Controls
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { showLiveClassDialog = true },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).testTag("teacher_sched_live_btn")
                ) {
                    Icon(Icons.Default.Videocam, null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Schedule Live")
                }

                Button(
                    onClick = { showSalaryDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).testTag("teacher_payroll_btn")
                ) {
                    Icon(Icons.Default.Payment, null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("My Payroll")
                }
            }
        }

        item {
            Text("My Assigned Classes & Syllabi", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        }

        items(myCourses) { course ->
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(course.name, fontWeight = FontWeight.Bold)
                    Text("Code: ${course.code} | Credits: ${course.creditHours} Hrs", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        OutlinedButton(onClick = { selectedCourseForAttendance = course }) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Attendance")
                        }

                        Button(onClick = { vm.generateAttendanceQr(course.id) }) {
                            Icon(Icons.Default.QrCode, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Launch QR Session")
                        }
                    }
                }
            }
        }

        // Active QR session monitor
        item {
            val qrSession by vm.attendanceQrCode.collectAsState()
            AnimatedVisibility(visible = qrSession != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Dynamic Attendance QR Active!", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text("Session ID: ${qrSession ?: ""}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // QR code display representation
                        Canvas(modifier = Modifier.size(120.dp)) {
                            // Renders fake scan code grid
                            val sizeBox = this.size.width / 10f
                            for (x in 0..9) {
                                for (y in 0..9) {
                                    if ((x + y) % 2 == 0) {
                                        drawRect(
                                            color = Color.Black,
                                            topLeft = Offset(x * sizeBox, y * sizeBox),
                                            size = Size(sizeBox, sizeBox)
                                        )
                                    }
                                }
                            }
                        }

                        TextButton(onClick = { vm.clearAttendanceQr() }) {
                            Text("Terminate QR Session", color = Color.Red, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Submissions grading queue
        item {
            Text("Assignment Grading Queue", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        }

        val myCourseIds = myCourses.map { it.id }
        val courseSubmissions = submissions.filter { sub ->
            sub.assignmentId == "cs_assign_1" && sub.status == "SUBMITTED" // simplified filter for seed assignment
        }

        if (courseSubmissions.isEmpty()) {
            item {
                EmptyState(message = "No pending assignment submissions for grading.")
            }
        } else {
            items(courseSubmissions) { sub ->
                val studentObj = students.find { it.id == sub.studentId }
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Student: ${studentObj?.name ?: sub.studentId}", fontWeight = FontWeight.Bold)
                        Text("Document: ${sub.attachmentName}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        Text("Written text submission: ${sub.submittedText}", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Button(
                            onClick = { selectedSubmissionForGrading = sub },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Grade Submission")
                        }
                    }
                }
            }
        }
    }

    // Modal: Schedule Live Class
    if (showLiveClassDialog) {
        AlertDialog(
            onDismissRequest = { showLiveClassDialog = false },
            title = { Text("Schedule Live Virtual Class") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Select Course target:")
                    OutlinedTextField(value = liveCourseId, onValueChange = { liveCourseId = it }, label = { Text("Course ID (e.g. CS-101)") }, modifier = Modifier.fillMaxWidth())
                    Text("Students will receive an automated Push Notification containing the Google Meet link once started.")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        vm.addTimetableSession(liveCourseId, "THURSDAY", "09:00", "10:30", "Virtual Hall B", isLive = true)
                        showLiveClassDialog = false
                    }
                ) {
                    Text("Start Live stream link")
                }
            }
        )
    }

    // Modal: Manual Attendance Marker
    if (selectedCourseForAttendance != null) {
        val course = selectedCourseForAttendance!!
        val courseStudents = students.filter { it.role == "STUDENT" } // Seed demo list
        
        AlertDialog(
            onDismissRequest = { selectedCourseForAttendance = null },
            title = { Text("Take attendance - ${course.name}") },
            text = {
                Column(modifier = Modifier.height(300.dp)) {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(courseStudents) { stud ->
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(stud.name, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                    Row {
                                        Button(onClick = { vm.takeAttendanceManual(course.id, stud.id, "2026-07-02", "PRESENT") }) {
                                            Text("P", fontSize = 11.sp)
                                        }
                                        Spacer(modifier = Modifier.width(4.dp))
                                        FilledTonalButton(onClick = { vm.takeAttendanceManual(course.id, stud.id, "2026-07-02", "ABSENT") }) {
                                            Text("A", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { selectedCourseForAttendance = null }) {
                    Text("Finalize List")
                }
            }
        )
    }

    // Modal: Marking Assignment Submissions
    if (selectedSubmissionForGrading != null) {
        val sub = selectedSubmissionForGrading!!
        AlertDialog(
            onDismissRequest = { selectedSubmissionForGrading = null },
            title = { Text("Grade & Return Assignment") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = gradingMarks,
                        onValueChange = { gradingMarks = it },
                        label = { Text("Marks Awarded (Max 20)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = gradingFeedback,
                        onValueChange = { gradingFeedback = it },
                        label = { Text("Feedback Comment") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val mVal = gradingMarks.toDoubleOrNull() ?: 15.0
                        vm.gradeSubmission(sub.id, mVal, gradingFeedback)
                        selectedSubmissionForGrading = null
                        gradingMarks = ""
                        gradingFeedback = ""
                    },
                    modifier = Modifier.testTag("submit_grade_btn")
                ) {
                    Text("Submit Grade")
                }
            }
        )
    }

    // Modal: Salary
    if (showSalaryDialog) {
        val slip = slips.find { it.employeeId == teacher.id }
        AlertDialog(
            onDismissRequest = { showSalaryDialog = false },
            title = { Text("Faculty Salary Details") },
            text = {
                if (slip != null) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Billing Cycle: ${slip.month}", fontWeight = FontWeight.Bold)
                        Text("Base salary amount: $${slip.baseSalary}")
                        Text("Bonus allocations: +$${slip.bonus}")
                        Text("Deductions: -$${slip.deduction}")
                        Divider()
                        Text("Disbursed total: $${slip.baseSalary + slip.bonus - slip.deduction}", fontWeight = FontWeight.Bold, color = Color.Green)
                    }
                } else {
                    Text("No records generated for current payroll cycle.")
                }
            },
            confirmButton = {
                Button(onClick = { showSalaryDialog = false }) { Text("Close") }
            }
        )
    }
}

// ==========================================
// 4. STUDENT DASHBOARD WORKSPACE
// ==========================================
@Composable
fun StudentDashboard(student: UserEntity, vm: LmsViewModel) {
    val enrollments by vm.enrollments.collectAsState()
    val courses by vm.courses.collectAsState()
    val timetable by vm.timetableEvents.collectAsState()
    val attendance by vm.attendanceList.collectAsState()

    val myEnrollments = enrollments.filter { it.studentId == student.id && it.status == "APPROVED" }
    val myCourses = courses.filter { course ->
        myEnrollments.any { it.courseId == course.id }
    }
    
    var showScanOverlay by remember { mutableStateOf(false) }
    var scanMessage by remember { mutableStateOf<String?>(null) }
    
    var activeLiveSession by remember { mutableStateOf<TimetableEntity?>(null) }
    val activeLive = timetable.find { it.liveStatus == "LIVE" && myCourses.any { c -> c.id == it.courseId } }

    var selectedCourseForNotes by remember { mutableStateOf<CourseEntity?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Student Desk: Welcome ${student.name}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Active live class highlight banner
        if (activeLive != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).background(Color.Red, CircleShape))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("LIVE CLASS NOW", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onErrorContainer)
                            }
                            val cObj = courses.find { it.id == activeLive.courseId }
                            Text(cObj?.name ?: activeLive.courseId, fontWeight = FontWeight.Bold)
                            Text("Room: ${activeLive.classroom} | Ends: ${activeLive.endTime}", fontSize = 12.sp)
                        }
                        Button(
                            onClick = { activeLiveSession = activeLive },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                            modifier = Modifier.testTag("student_join_live_btn")
                        ) {
                            Text("Join Lecture")
                        }
                    }
                }
            }
        }

        // Action controls
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { showScanOverlay = true },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).testTag("student_scan_attendance_btn")
                ) {
                    Icon(Icons.Default.QrCodeScanner, null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("QR Attendance")
                }
            }
        }

        // Attendance stats card
        item {
            val myAttList = attendance.filter { it.studentId == student.id }
            val presentCount = myAttList.count { it.status == "PRESENT" }
            val totalCount = myAttList.size
            val pct = if (totalCount > 0) (presentCount.toFloat() / totalCount * 100).toInt() else 94 // seed default

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("My Attendance Percentage", fontWeight = FontWeight.Bold)
                        Text("Minimum threshold is 75% for exam clearance", fontSize = 12.sp, color = Color.Gray)
                    }
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            progress = pct.toFloat() / 100f,
                            color = if (pct >= 75) Color.Green else Color.Red,
                            modifier = Modifier.size(54.dp)
                        )
                        Text("$pct%", fontWeight = FontWeight.Black, fontSize = 11.sp)
                    }
                }
            }
        }

        item {
            Text("My Registered Courses", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        }

        if (myCourses.isEmpty()) {
            item {
                EmptyState(message = "No enrolled courses found.")
            }
        } else {
            items(myCourses) { course ->
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(course.name, fontWeight = FontWeight.Bold)
                        Text("Code: ${course.code} | Credits: ${course.creditHours} Hrs", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            OutlinedButton(onClick = { selectedCourseForNotes = course }) {
                                Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Study Materials")
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal: Join Virtual Live Class
    if (activeLiveSession != null) {
        val s = activeLiveSession!!
        AlertDialog(
            onDismissRequest = { activeLiveSession = null },
            title = { Text("Join Live Stream") },
            text = {
                Column {
                    Text("You are connecting to virtual lecture classroom.")
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Link: ${s.meetingLink}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            },
            confirmButton = {
                Button(onClick = { activeLiveSession = null }) { Text("Disconnect") }
            }
        )
    }

    // Modal: Scan Classroom QR View
    if (showScanOverlay) {
        QRAttendanceScanner(
            onScanSuccess = { content ->
                showScanOverlay = false
                vm.scanAttendanceQrAndMark(content, student.id) { ok, msg ->
                    scanMessage = msg
                }
            },
            onCancel = { showScanOverlay = false }
        )
    }

    // Modal: Scan Message
    if (scanMessage != null) {
        AlertDialog(
            onDismissRequest = { scanMessage = null },
            title = { Text("QR Scanner Status") },
            text = { Text(scanMessage!!) },
            confirmButton = { Button(onClick = { scanMessage = null }) { Text("Acknowledged") } }
        )
    }

    // Modal: Lecture study files list
    if (selectedCourseForNotes != null) {
        val course = selectedCourseForNotes!!
        AlertDialog(
            onDismissRequest = { selectedCourseForNotes = null },
            title = { Text("Course Study Folders: ${course.code}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Select notes document to download and open:")
                    
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { /* download mock */ }
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color.Red)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Lecture_1_Introduction.pdf", fontWeight = FontWeight.Bold)
                                Text("Size: 4.2 MB | Format: PDF document", fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { /* download mock */ }
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FileOpen, contentDescription = null, tint = Color.Blue)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Midterm_Review_Syllabus.docx", fontWeight = FontWeight.Bold)
                                Text("Size: 1.1 MB | Format: Microsoft Word", fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            },
            confirmButton = { Button(onClick = { selectedCourseForNotes = null }) { Text("Close Folder") } }
        )
    }
}


// --- Reusable Stats Component ---
@Composable
fun DashboardStatsCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    val contentColor = if (color == MaterialTheme.colorScheme.primaryContainer) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else if (color == MaterialTheme.colorScheme.secondaryContainer) {
        Color(0xFF1D1B20)
    } else if (color == MaterialTheme.colorScheme.errorContainer) {
        MaterialTheme.colorScheme.onErrorContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    val borderColor = if (color == MaterialTheme.colorScheme.primaryContainer) {
        Color(0xFFD0BCFF)
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        colors = CardDefaults.cardColors(containerColor = color, contentColor = contentColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    ),
                    color = contentColor.copy(alpha = 0.7f)
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor.copy(alpha = 0.8f),
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-0.5).sp
                ),
                color = contentColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.TrendingUp,
                    contentDescription = null,
                    tint = contentColor.copy(alpha = 0.6f),
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = "Active tracking",
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor.copy(alpha = 0.6f)
                )
            }
        }
    }
}

// --- Reusable Empty State Component ---
@Composable
fun EmptyState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Inbox,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = Color.LightGray
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun LegendChip(color: Color, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, RoundedCornerShape(2.dp))
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
    }
}
