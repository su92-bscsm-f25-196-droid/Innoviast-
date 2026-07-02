package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// --- Glassmorphic Card ---
@Composable
fun GlassmorphismCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
    borderColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
    cornerRadius: Dp = 16.dp,
    elevation: Dp = 8.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .shadow(elevation, RoundedCornerShape(cornerRadius), clip = false)
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(
                        borderColor,
                        borderColor.copy(alpha = 0.05f)
                    )
                ),
                shape = RoundedCornerShape(cornerRadius)
            ),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(cornerRadius)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            content = content
        )
    }
}

// --- Interactive Custom Bar Chart ---
@Composable
fun InteractiveBarChart(
    modifier: Modifier = Modifier,
    barData: List<Pair<String, Float>>, // Label, Value
    barColors: List<Color> = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary),
    yMax: Float = 100f
) {
    var selectedBarIndex by remember { mutableStateOf(-1) }
    
    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clickable {
                    selectedBarIndex = if (selectedBarIndex == -1) 2 else -1 // toggles dummy
                }
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val spacing = 24f
            val barsCount = barData.size
            val barWidth = (canvasWidth - (spacing * (barsCount + 1))) / barsCount
            
            // Draw grids
            val gridLineCount = 4
            for (i in 0..gridLineCount) {
                val y = canvasHeight * (i.toFloat() / gridLineCount)
                drawLine(
                    color = Color.LightGray.copy(alpha = 0.2f),
                    start = Offset(0f, y),
                    end = Offset(canvasWidth, y),
                    strokeWidth = 2f
                )
            }

            // Draw bars
            barData.forEachIndexed { index, (label, value) ->
                val barHeight = (value / yMax) * canvasHeight
                val x = spacing + index * (barWidth + spacing)
                val y = canvasHeight - barHeight
                
                val isSelected = index == selectedBarIndex
                val color = if (isSelected) barColors[1] else barColors[index % barColors.size]
                
                // Draw single rounded bar
                drawRoundRect(
                    color = color,
                    topLeft = Offset(x, y),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(12f, 12f)
                )

                // Optional interactive tooltip
                if (isSelected) {
                    drawRoundRect(
                        color = Color.DarkGray,
                        topLeft = Offset(x - 20f, y - 40f),
                        size = Size(barWidth + 40f, 32f),
                        cornerRadius = CornerRadius(6f, 6f)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            barData.forEach { (label, _) ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// --- Interactive Custom Line Chart ---
@Composable
fun InteractiveLineChart(
    modifier: Modifier = Modifier,
    points: List<Float>,
    labels: List<String>,
    lineColor: Color = MaterialTheme.colorScheme.secondary,
    yMax: Float = 100f
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(150.dp)
    ) {
        val width = size.width
        val height = size.height
        val segmentWidth = width / (points.size - 1)

        val path = Path()
        points.forEachIndexed { index, value ->
            val x = index * segmentWidth
            val y = height - (value / yMax) * height
            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }

        // Draw line shadow/gradient under the line path
        val fillPath = Path().apply {
            addPath(path)
            lineTo(width, height)
            lineTo(0f, height)
            close()
        }
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(lineColor.copy(alpha = 0.3f), Color.Transparent)
            )
        )

        // Draw line
        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 6f, cap = StrokeCap.Round)
        )

        // Draw data circles
        points.forEachIndexed { index, value ->
            val x = index * segmentWidth
            val y = height - (value / yMax) * height
            drawCircle(
                color = lineColor,
                radius = 8f,
                center = Offset(x, y)
            )
            drawCircle(
                color = Color.White,
                radius = 4f,
                center = Offset(x, y)
            )
        }
    }
}

// --- Custom Pie Chart ---
@Composable
fun InteractivePieChart(
    modifier: Modifier = Modifier,
    values: List<Float>,
    colors: List<Color> = listOf(
        Color(0xFF4CAF50), Color(0xFF2196F3), Color(0xFFFFC107),
        Color(0xFFE91E63), Color(0xFF9C27B0)
    )
) {
    val total = values.sum()
    if (total == 0f) return

    Canvas(
        modifier = modifier
            .size(140.dp)
    ) {
        var startAngle = 0f
        values.forEachIndexed { index, value ->
            val sweepAngle = (value / total) * 360f
            drawArc(
                color = colors[index % colors.size],
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                size = size
            )
            startAngle += sweepAngle
        }
    }
}

// --- QR Attendance System - Creator & Scanner View ---
@Composable
fun QRAttendanceScanner(
    onScanSuccess: (String) -> Unit,
    onCancel: () -> Unit
) {
    var isProcessing by remember { mutableStateOf(false) }
    var locationVerified by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "QR Attendance Portal",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Align university lecture QR inside the scanner frame",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.LightGray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Scanner Frame
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .border(3.dp, Color.Green, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                // Moving Red Line scanner laser animation
                val infiniteTransition = rememberInfiniteTransition(label = "laser")
                val laserY by infiniteTransition.animateFloat(
                    initialValue = 0.1f,
                    targetValue = 0.9f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2000, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "laser_y"
                )

                // Renders static QR graphic inside scanner frame representing raw matrix
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Draw random matrix boxes
                    val spacing = 20f
                    for (x in 0..10) {
                        for (y in 0..10) {
                            if ((x + y) % 3 == 0 || (x == 1 && y == 1) || (x == 8 && y == 8) || (x == 1 && y == 8)) {
                                drawRect(
                                    color = Color.DarkGray.copy(alpha = 0.5f),
                                    topLeft = Offset(x * spacing + 20f, y * spacing + 20f),
                                    size = Size(12f, 12f)
                                )
                            }
                        }
                    }
                    
                    // Laser Line
                    drawLine(
                        color = Color.Red,
                        start = Offset(0f, size.height * laserY),
                        end = Offset(size.width, size.height * laserY),
                        strokeWidth = 4f
                    )
                }

                if (isProcessing) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.6f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color.Green)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "GPS",
                        tint = if (locationVerified) Color.Green else Color.Yellow
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (locationVerified) "GPS Verified (+/- 5m)" else "Locating GPS...",
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    isProcessing = true
                    // Simulate GPS wait and scan delay
                    scope.launch {
                        delay(1200)
                        locationVerified = true
                        delay(800)
                        // Trigger mock lecture QR mark
                        onScanSuccess("ATT-CS-101-${System.currentTimeMillis()}")
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Green),
                modifier = Modifier.fillMaxWidth(0.8f).testTag("scan_button_trigger")
            ) {
                Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = Color.Black)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Scan Classroom QR", color = Color.Black)
            }

            TextButton(
                onClick = onCancel,
                modifier = Modifier.padding(top = 12.dp)
            ) {
                Text("Cancel Scan", color = Color.Red)
            }
        }
    }
}

// --- Certificate Visual Previewer Module ---
@Composable
fun CertificatePreview(
    studentName: String,
    courseTitle: String,
    certificateType: String = "COURSE_COMPLETION", // BONAFIDE, CHARACTER, FEE_CLEARANCE, COURSE_COMPLETION
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = onDismiss) {
                Icon(Icons.Default.Share, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Export PDF & Share")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        },
        title = {
            Text(
                text = "Official Academic Certificate",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(4.dp, Color(0xFFC5A059), RoundedCornerShape(12.dp)) // Gold border
                    .background(Color(0xFFFFFDF9)) // Parchment color
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "EDUSPHERE UNIVERSITY",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B365D), // Dark Blue Navy
                        letterSpacing = 2.sp,
                        fontFamily = FontFamily.Serif
                    )
                    Text(
                        text = "ACADEMIC REGISTRAR'S OFFICE",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        fontFamily = FontFamily.Serif
                    )

                    Divider(
                        color = Color(0xFFC5A059),
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .padding(vertical = 12.dp),
                        thickness = 1.dp
                    )

                    Text(
                        text = when (certificateType) {
                            "BONAFIDE" -> "BONAFIDE CERTIFICATE"
                            "CHARACTER" -> "CHARACTER CERTIFICATE"
                            "FEE_CLEARANCE" -> "FEE CLEARANCE DECREE"
                            else -> "CERTIFICATE OF ACHIEVEMENT"
                        },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFC5A059),
                        fontFamily = FontFamily.Serif
                    )

                    Text(
                        text = "This document officially certifies that",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.DarkGray,
                        modifier = Modifier.padding(top = 12.dp),
                        fontFamily = FontFamily.Serif
                    )

                    Text(
                        text = studentName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B365D),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 8.dp),
                        fontFamily = FontFamily.Serif
                    )

                    Text(
                        text = when (certificateType) {
                            "BONAFIDE" -> "is a bonafide student of the Department of Computer Science & Engineering enrolled in semester 4, maintaining exceptional discipline."
                            "CHARACTER" -> "has exhibited exemplary moral character, collaborative peer leadership, and active engagement in campus development activities."
                            "FEE_CLEARANCE" -> "has successfully paid all assigned tuition fees, library, and examination dues, leaving zero outstanding dues on record."
                            else -> "has successfully completed all lecture credits, assignments, and examination modules for the advanced university course:"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.DarkGray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp),
                        fontFamily = FontFamily.Serif
                    )

                    if (certificateType == "COURSE_COMPLETION") {
                        Text(
                            text = courseTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B365D),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 8.dp),
                            fontFamily = FontFamily.Serif
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Dr. Alice Smith",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.DarkGray,
                                fontFamily = FontFamily.Serif
                            )
                            Divider(color = Color.Gray, modifier = Modifier.width(80.dp), thickness = 1.dp)
                            Text(
                                text = "Vice Chancellor",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 8.sp,
                                color = Color.Gray
                            )
                        }

                        // Gold Medal Vector Graphics using Canvas
                        Canvas(modifier = Modifier.size(40.dp)) {
                            drawCircle(
                                color = Color(0xFFC5A059),
                                radius = size.minDimension / 2f
                            )
                            drawCircle(
                                color = Color(0xFFFFD700),
                                radius = size.minDimension / 2.5f
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "2026-07-02",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.DarkGray,
                                fontFamily = FontFamily.Serif
                            )
                            Divider(color = Color.Gray, modifier = Modifier.width(80.dp), thickness = 1.dp)
                            Text(
                                text = "Issue Date",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 8.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    )
}
