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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserEntity
import com.example.ui.components.CertificatePreview
import com.example.ui.viewmodel.LmsViewModel

@Composable
fun ReportsScreen(currentUser: UserEntity, vm: LmsViewModel) {
    var activeCertificateType by remember { mutableStateOf<String?>(null) }
    var activeCourseName by remember { mutableStateOf("") }

    val certificates = listOf(
        Triple("Bonafide Student Certificate", "Certifies registration and active status in current academic cycle", "BONAFIDE"),
        Triple("Character Diploma Scroll", "Attests peer collaborative ethics, leadership, and moral character", "CHARACTER"),
        Triple("Course Completion Diploma", "Confirms successfully cleared final exams, GPA standings, and assignments", "COURSE_COMPLETION"),
        Triple("Official Fee Clearance Decree", "Decrees zero pending dues, cleared tuition receipts, and liabilities", "FEE_CLEARANCE")
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Academic Records & Certificates Portal",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Generate, preview, and download digitally signed credentials and transcripts",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CardMembership,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Instant Digital Credentials", fontWeight = FontWeight.Bold)
                        Text("Prevents manual wait times. Instantly certified.", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }
        }

        item {
            Text("Available Official Document Previews", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        }

        items(certificates) { (title, subtitle, type) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        activeCourseName = "Advanced Database Systems (CS-101)"
                        activeCertificateType = type
                    }
                    .testTag("cert_card_$type")
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.WorkspacePremium,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(title, fontWeight = FontWeight.Bold)
                        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = Color.Gray
                    )
                }
            }
        }
    }

    // Modal Certificate display
    if (activeCertificateType != null) {
        CertificatePreview(
            studentName = currentUser.name,
            courseTitle = activeCourseName,
            certificateType = activeCertificateType!!,
            onDismiss = { activeCertificateType = null }
        )
    }
}
