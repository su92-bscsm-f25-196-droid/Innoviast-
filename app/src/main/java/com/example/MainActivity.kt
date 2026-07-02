package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.UserEntity
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppContainer()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContainer() {
    val context = LocalContext.current
    
    // ViewModels initialization
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(context))
    val lmsViewModel: LmsViewModel = viewModel(factory = LmsViewModelFactory(context))
    val aiViewModel: AiAssistantViewModel = viewModel()

    val authState by authViewModel.authState.collectAsState()
    
    when (val state = authState) {
        is AuthState.Idle, is AuthState.Error -> {
            LoginScreen(authViewModel = authViewModel)
        }
        is AuthState.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Authenticating EduSphere Session...", fontWeight = FontWeight.SemiBold)
                }
            }
        }
        is AuthState.Authenticated -> {
            MainTabsScaffold(
                currentUser = state.user,
                authViewModel = authViewModel,
                lmsViewModel = lmsViewModel,
                aiViewModel = aiViewModel
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTabsScaffold(
    currentUser: UserEntity,
    authViewModel: AuthViewModel,
    lmsViewModel: LmsViewModel,
    aiViewModel: AiAssistantViewModel
) {
    var activeTabIndex by remember { mutableStateOf(0) } // 0: Home, 1: Academics, 2: Chat, 3: AI, 4: Records
    var showProfileModal by remember { mutableStateOf(false) }

    // Search query states
    var searchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    // Profiler inputs
    var profName by remember { mutableStateOf(currentUser.name) }
    var profEmail by remember { mutableStateOf(currentUser.email) }
    var profPass by remember { mutableStateOf("") }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    if (searchActive) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { 
                                searchQuery = it 
                                lmsViewModel.updateSearchQuery(it)
                            },
                            placeholder = { Text("Search courses/users...", fontSize = 14.sp) },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .testTag("global_search_bar"),
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            trailingIcon = {
                                IconButton(onClick = { 
                                    searchActive = false 
                                    searchQuery = ""
                                    lmsViewModel.updateSearchQuery("")
                                }) {
                                    Icon(Icons.Default.Close, contentDescription = null)
                                }
                            }
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (currentUser.role) {
                                        "ADMIN" -> Icons.Default.AdminPanelSettings
                                        "HOD" -> Icons.Default.CorporateFare
                                        "TEACHER" -> Icons.Default.CastForEducation
                                        else -> Icons.Default.Person
                                    },
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = when (currentUser.role) {
                                        "ADMIN" -> "System Administrator"
                                        "HOD" -> "Head of Department"
                                        "TEACHER" -> "Faculty Member"
                                        else -> "Academic Student"
                                    },
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = when (activeTabIndex) {
                                        0 -> "Dashboard Overview"
                                        1 -> "Academic Hub"
                                        2 -> "Live Chatroom"
                                        3 -> "AI Assistant"
                                        else -> "System Reports"
                                    },
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = (-0.5).sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                },
                actions = {
                    if (!searchActive) {
                        IconButton(onClick = { searchActive = true }) {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    IconButton(onClick = { /* Simulated notification click */ }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(
                        onClick = { showProfileModal = true },
                        modifier = Modifier.testTag("top_profile_avatar_btn")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = currentUser.name.firstOrNull()?.uppercase().toString(),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontSize = 13.sp
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.testTag("main_navigation_bar"),
                windowInsets = WindowInsets.navigationBars
            ) {
                // Home Dashboard Tab
                NavigationBarItem(
                    selected = activeTabIndex == 0,
                    onClick = { activeTabIndex = 0 },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = null) },
                    label = { Text("Home", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier.testTag("nav_tab_home")
                )

                // Academics Multi-Module Tab
                NavigationBarItem(
                    selected = activeTabIndex == 1,
                    onClick = { activeTabIndex = 1 },
                    icon = { Icon(Icons.Default.MenuBook, contentDescription = null) },
                    label = { Text("Academics", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier.testTag("nav_tab_academics")
                )

                // Live Chat Tab
                NavigationBarItem(
                    selected = activeTabIndex == 2,
                    onClick = { activeTabIndex = 2 },
                    icon = { Icon(Icons.Default.Forum, contentDescription = null) },
                    label = { Text("Messenger", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier.testTag("nav_tab_messenger")
                )

                // Educational AI Companion Tab
                NavigationBarItem(
                    selected = activeTabIndex == 3,
                    onClick = { activeTabIndex = 3 },
                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = null) },
                    label = { Text("EduSphere AI", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier.testTag("nav_tab_ai")
                )

                // Digital Records Tab
                NavigationBarItem(
                    selected = activeTabIndex == 4,
                    onClick = { activeTabIndex = 4 },
                    icon = { Icon(Icons.Default.WorkspacePremium, contentDescription = null) },
                    label = { Text("Records", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier.testTag("nav_tab_records")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (activeTabIndex) {
                0 -> DashboardManagerScreen(currentUser = currentUser, lmsViewModel = lmsViewModel)
                1 -> AcademicsSubNavigator(currentUser = currentUser, lmsViewModel = lmsViewModel)
                2 -> ChatScreen(currentUser = currentUser, vm = lmsViewModel)
                3 -> AiAssistantScreen(aiViewModel = aiViewModel)
                4 -> ReportsScreen(currentUser = currentUser, vm = lmsViewModel)
            }
        }
    }

    // Modal: User Profiler Settings Drawer / Dialogue
    if (showProfileModal) {
        AlertDialog(
            onDismissRequest = { showProfileModal = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ManageAccounts, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("My ERP Credentials Profile")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("User ID: ${currentUser.id} | Access: ${currentUser.role}", fontWeight = FontWeight.Bold, color = Color.Gray)
                    Divider()
                    
                    OutlinedTextField(
                        value = profName,
                        onValueChange = { profName = it },
                        label = { Text("Full Name") },
                        modifier = Modifier.fillMaxWidth().testTag("profile_name_edit")
                    )

                    OutlinedTextField(
                        value = profEmail,
                        onValueChange = { profEmail = it },
                        label = { Text("Institutional Email") },
                        modifier = Modifier.fillMaxWidth().testTag("profile_email_edit")
                    )

                    OutlinedTextField(
                        value = profPass,
                        onValueChange = { profPass = it },
                        label = { Text("Update Secret Password") },
                        placeholder = { Text("Leave blank to keep existing") },
                        modifier = Modifier.fillMaxWidth().testTag("profile_pass_edit")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val passToUpdate = if (profPass.isNotBlank()) profPass else null
                        authViewModel.updateProfile(profName, profEmail, passToUpdate)
                        showProfileModal = false
                        profPass = ""
                    },
                    modifier = Modifier.testTag("profile_save_btn")
                ) {
                    Text("Save Modifications")
                }
            },
            dismissButton = {
                Row {
                    TextButton(
                        onClick = {
                            showProfileModal = false
                            authViewModel.logout()
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.Red),
                        modifier = Modifier.testTag("logout_btn_trigger")
                    ) {
                        Icon(Icons.Default.Logout, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Log Out")
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    TextButton(onClick = { showProfileModal = false }) {
                        Text("Dismiss")
                    }
                }
            }
        )
    }
}

// --- Sub Navigator for Academics Tab ---
@Composable
fun AcademicsSubNavigator(currentUser: UserEntity, lmsViewModel: LmsViewModel) {
    var activeSubTab by remember { mutableStateOf("TIMETABLE") } // TIMETABLE, ASSIGNMENTS, QUIZZES, EXAMS, FINANCE, PLANNER

    Column(modifier = Modifier.fillMaxSize()) {
        ScrollableTabRow(
            selectedTabIndex = when (activeSubTab) {
                "TIMETABLE" -> 0
                "ASSIGNMENTS" -> 1
                "QUIZZES" -> 2
                "EXAMS" -> 3
                "FINANCE" -> 4
                "PLANNER" -> 5
                else -> 0
            },
            edgePadding = 16.dp,
            modifier = Modifier.fillMaxWidth().testTag("academics_sub_tabs")
        ) {
            Tab(selected = activeSubTab == "TIMETABLE", onClick = { activeSubTab = "TIMETABLE" }) {
                Text("Schedule", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold)
            }
            Tab(selected = activeSubTab == "ASSIGNMENTS", onClick = { activeSubTab = "ASSIGNMENTS" }) {
                Text("Assignments", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold)
            }
            Tab(selected = activeSubTab == "QUIZZES", onClick = { activeSubTab = "QUIZZES" }) {
                Text("Quizzes", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold)
            }
            Tab(selected = activeSubTab == "EXAMS", onClick = { activeSubTab = "EXAMS" }) {
                Text("Exams", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold)
            }
            Tab(selected = activeSubTab == "FINANCE", onClick = { activeSubTab = "FINANCE" }) {
                Text("Finance Dues", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold)
            }
            Tab(selected = activeSubTab == "PLANNER", onClick = { activeSubTab = "PLANNER" }) {
                Text("Planner", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold)
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            when (activeSubTab) {
                "TIMETABLE" -> TimetableScreen(currentUser, lmsViewModel)
                "ASSIGNMENTS" -> AssignmentScreen(currentUser, lmsViewModel)
                "QUIZZES" -> QuizScreen(currentUser, lmsViewModel)
                "EXAMS" -> ExaminationScreen(currentUser, lmsViewModel)
                "FINANCE" -> FinanceScreen(currentUser, lmsViewModel)
                "PLANNER" -> AcademicPlannerScreen(currentUser, lmsViewModel)
            }
        }
    }
}

class AuthViewModelFactory(private val context: android.content.Context) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return AuthViewModel(context.applicationContext as android.app.Application) as T
    }
}

class LmsViewModelFactory(private val context: android.content.Context) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return LmsViewModel(context.applicationContext as android.app.Application) as T
    }
}
