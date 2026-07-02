package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.viewmodel.AiAssistantViewModel
import com.example.ui.viewmodel.AiMessage
import com.example.ui.viewmodel.LmsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ==========================================
// 1. CHAT & MESSAGING CENTER
// ==========================================
@Composable
fun ChatScreen(currentUser: UserEntity, vm: LmsViewModel) {
    val messages by vm.chatMessages.collectAsState()
    val users by vm.users.collectAsState()
    
    var selectedChannel by remember { mutableStateOf<String>("CS-101") } // Group channel defaults to CS-101
    var chatInput by remember { mutableStateOf("") }
    
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Filter relevant messages based on channel
    val channelMessages = messages.filter { msg ->
        if (selectedChannel == "CS-101") {
            msg.isGroup && msg.groupId == "CS-101"
        } else {
            // Direct chat with chosen teacher
            !msg.isGroup && (
                (msg.senderId == currentUser.id && msg.recipientId == "cs_teacher") ||
                (msg.senderId == "cs_teacher" && msg.recipientId == currentUser.id)
            )
        }
    }

    // Auto-scroll on new messages
    LaunchedEffect(channelMessages.size) {
        if (channelMessages.isNotEmpty()) {
            listState.animateScrollToItem(channelMessages.size - 1)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Channel Selector Row
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedChannel == "CS-101",
                onClick = { selectedChannel = "CS-101" },
                label = { Text("CS-101 Database Group") },
                modifier = Modifier.weight(1f).testTag("chat_channel_group")
            )

            FilterChip(
                selected = selectedChannel == "DIRECT",
                onClick = { selectedChannel = "DIRECT" },
                label = { Text("Direct: Teacher Charles") },
                modifier = Modifier.weight(1f).testTag("chat_channel_direct")
            )
        }

        // Messages Box list
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
        ) {
            if (channelMessages.isEmpty()) {
                EmptyState(message = "Say hello! Chat room starting.")
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize().padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(channelMessages) { msg ->
                        val isMe = msg.senderId == currentUser.id
                        val align = if (isMe) Alignment.End else Alignment.Start
                        val bubbleColor = if (isMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                        val textColor = if (isMe) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = align
                        ) {
                            if (!isMe) {
                                Text(
                                    text = msg.senderName,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                                )
                            }
                            
                            Box(
                                modifier = Modifier
                                    .clip(
                                        RoundedCornerShape(
                                            topStart = 16.dp,
                                            topEnd = 16.dp,
                                            bottomStart = if (isMe) 16.dp else 0.dp,
                                            bottomEnd = if (isMe) 0.dp else 16.dp
                                        )
                                    )
                                    .background(bubbleColor)
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                                    .widthIn(max = 240.dp)
                            ) {
                                Text(msg.message, color = textColor, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Message input bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = chatInput,
                onValueChange = { chatInput = it },
                placeholder = { Text("Write message...") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    if (chatInput.isNotBlank()) {
                        val recip = if (selectedChannel == "CS-101") null else "cs_teacher"
                        vm.sendChatMessage(
                            senderId = currentUser.id,
                            senderName = currentUser.name,
                            recipientId = recip,
                            isGroup = selectedChannel == "CS-101",
                            groupId = if (selectedChannel == "CS-101") "CS-101" else null,
                            msgText = chatInput
                        )
                        chatInput = ""
                    }
                }),
                modifier = Modifier.weight(1f).testTag("chat_input_text"),
                shape = RoundedCornerShape(24.dp)
            )

            FloatingActionButton(
                onClick = {
                    if (chatInput.isNotBlank()) {
                        val recip = if (selectedChannel == "CS-101") null else "cs_teacher"
                        vm.sendChatMessage(
                            senderId = currentUser.id,
                            senderName = currentUser.name,
                            recipientId = recip,
                            isGroup = selectedChannel == "CS-101",
                            groupId = if (selectedChannel == "CS-101") "CS-101" else null,
                            msgText = chatInput
                        )
                        chatInput = ""
                    }
                },
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp).testTag("chat_send_button")
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White)
            }
        }
    }
}


// ==========================================
// 2. AI EDUCATIONAL ASSISTANT COMPANION
// ==========================================
@Composable
fun AiAssistantScreen(aiViewModel: AiAssistantViewModel) {
    val messages by aiViewModel.messages.collectAsState()
    val isThinking by aiViewModel.isThinking.collectAsState()
    
    var textInput by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    
    val listState = rememberLazyListState()
    
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("EduSphere AI Assistant", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            
            IconButton(onClick = { aiViewModel.clearHistory() }) {
                Icon(Icons.Default.Refresh, contentDescription = "Clear History", tint = Color.Gray)
            }
        }

        // Quick-action chips list
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            AssistChip(
                onClick = { aiViewModel.triggerQuickAction("EXPLAIN_TOPIC", "Room Persistence Transactions") },
                label = { Text("Explain Room") },
                modifier = Modifier.weight(1f).testTag("ai_chip_explain")
            )
            AssistChip(
                onClick = { aiViewModel.triggerQuickAction("STUDY_PLAN", "Advanced Database Spring semester") },
                label = { Text("Study Guide") },
                modifier = Modifier.weight(1f).testTag("ai_chip_study")
            )
            AssistChip(
                onClick = { aiViewModel.triggerQuickAction("SOLVE_MATH", "x^2 - 5x + 6 = 0") },
                label = { Text("Solve Equation") },
                modifier = Modifier.weight(1f).testTag("ai_chip_solve")
            )
        }

        // Chat logs
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f))
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize().padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages) { msg ->
                    val isUser = msg.sender == "USER"
                    val bColor = if (isUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                    val borderM = if (isUser) Modifier else Modifier.border(1.dp, Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = bColor),
                            modifier = borderM.widthIn(max = 280.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (isUser) Icons.Default.Person else Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = if (isUser) Color.Gray else MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isUser) "Me" else "EduSphere AI",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = msg.text,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }

                if (isThinking) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                            modifier = Modifier.widthIn(max = 200.dp)
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("AI is thinking...", fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Text input row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = textInput,
                onValueChange = { textInput = it },
                placeholder = { Text("Ask anything...") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    if (textInput.isNotBlank()) {
                        aiViewModel.sendMessage(textInput)
                        textInput = ""
                        keyboardController?.hide()
                    }
                }),
                modifier = Modifier.weight(1f).testTag("ai_chat_input"),
                shape = RoundedCornerShape(24.dp)
            )

            FloatingActionButton(
                onClick = {
                    if (textInput.isNotBlank()) {
                        aiViewModel.sendMessage(textInput)
                        textInput = ""
                        keyboardController?.hide()
                    }
                },
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp).testTag("ai_send_button")
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White)
            }
        }
    }
}
