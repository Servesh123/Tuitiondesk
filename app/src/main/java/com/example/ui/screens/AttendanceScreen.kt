package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import com.example.model.AttendanceEntry
import com.example.ui.theme.*
import com.example.viewmodel.TuitionViewModel

@Composable
fun AttendanceScreen(
    viewModel: TuitionViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val selectedBatch by viewModel.selectedBatch.collectAsState()
    val batches = viewModel.batches
    val batchStudents by viewModel.currentBatchAttendance.collectAsState()
    val showAbsentDialog by viewModel.showAbsentAlertDialog.collectAsState()
    val absentList by viewModel.absentAlertList.collectAsState()

    val presentCount = batchStudents.count { it.isPresent }
    val absentCount = batchStudents.count { !it.isPresent }
    val totalCount = batchStudents.size
    val attendancePct = if (totalCount > 0) (presentCount * 100) / totalCount else 0

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            Surface(
                color = SurfaceWhite,
                tonalElevation = 6.dp,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Button(
                        onClick = { viewModel.onSaveAttendanceClicked(context) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (absentCount > 0) AbsentRed else SuccessGreen,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("save_attendance_button")
                    ) {
                        Icon(
                            imageVector = if (absentCount > 0) Icons.Default.NotificationsActive else Icons.Default.Save,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (absentCount > 0) {
                                "Save & Send Absent Alert to $absentCount Parents"
                            } else {
                                "Save Attendance (All $totalCount Present)"
                            },
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(NeutralBg)
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Date Banner and Batch Selection
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = NavyDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint = GoldLight,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Thursday, 22 Oct 2026",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                            Surface(
                                color = Color.White.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text(
                                    text = "DAILY REGISTER",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = GoldLight,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Select Today's Batch",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Horizontal batch chips
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            batches.forEach { batch ->
                                val isSelected = batch == selectedBatch
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.selectBatch(batch) },
                                    label = {
                                        Text(
                                            text = batch,
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = GoldSecondary,
                                        selectedLabelColor = Color.White,
                                        containerColor = Color.White.copy(alpha = 0.1f),
                                        labelColor = Color.White.copy(alpha = 0.8f)
                                    ),
                                    modifier = Modifier.testTag("batch_chip_${batch.take(8)}")
                                )
                            }
                        }
                    }
                }
            }

            // Attendance KPI and Quick Actions
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "$selectedBatch",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = NavyPrimary
                                    )
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "$presentCount Present",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = SuccessGreen
                                        )
                                    )
                                    Text(
                                        text = "  •  ",
                                        style = MaterialTheme.typography.titleMedium.copy(color = TextSecondary)
                                    )
                                    Text(
                                        text = "$absentCount Absent",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (absentCount > 0) AbsentRed else TextSecondary
                                        )
                                    )
                                    Text(
                                        text = "  ($attendancePct%)",
                                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                                    )
                                }
                            }

                            FilledTonalButton(
                                onClick = { viewModel.markAllPresent() },
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = SuccessGreenLight,
                                    contentColor = SuccessGreen
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("mark_all_present_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DoneAll,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "All Present",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Progress bar for visual percentage
                        LinearProgressIndicator(
                            progress = { if (totalCount > 0) presentCount.toFloat() / totalCount else 0f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = SuccessGreen,
                            trackColor = AbsentRedLight
                        )
                    }
                }
            }

            // Student Roster with P / A Toggle and Export Actions
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "STUDENT ATTENDANCE ROSTER ($totalCount)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            letterSpacing = 1.sp
                        ),
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilledTonalButton(
                            onClick = {
                                val csv = com.example.util.ReportExportHelper.generateAttendanceCsv(batchStudents)
                                com.example.util.ReportExportHelper.downloadToDevice(
                                    context,
                                    "Attendance_${selectedBatch.take(8).replace(" ", "_")}.csv",
                                    csv,
                                    "text/csv"
                                )
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("download_attendance_csv")
                        ) {
                            Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("CSV", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        FilledTonalButton(
                            onClick = {
                                val csv = com.example.util.ReportExportHelper.generateAttendanceCsv(batchStudents)
                                val file = com.example.util.ReportExportHelper.saveContentToFile(
                                    context,
                                    "Attendance_${selectedBatch.take(8).replace(" ", "_")}.csv",
                                    csv
                                )
                                com.example.util.ReportExportHelper.shareFile(context, file, "text/csv", "Share Attendance Register")
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("share_attendance_csv")
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Share", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            items(batchStudents, key = { it.studentId }) { student ->
                StudentAttendanceRow(
                    student = student,
                    onToggle = { viewModel.toggleAttendance(student.studentId) }
                )
            }
        }
    }

    // Modal: Absent Parent Alert Confirmation & Broadcast
    if (showAbsentDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.setShowAbsentAlertDialog(false) },
            icon = {
                Icon(
                    imageVector = Icons.Default.WarningAmber,
                    contentDescription = null,
                    tint = AbsentRed,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Send Absent Alert to Parents?",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column {
                    Text(
                        text = "${absentList.size} student(s) marked absent in $selectedBatch today:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        color = SurfaceVariantSlate,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            absentList.forEach { absentee ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "${absentee.studentName} (${absentee.rollNo})",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                        Text(
                                            text = "Parent: ${absentee.parentName} (${absentee.parentPhone})",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontSize = 11.sp,
                                                color = TextSecondary
                                            )
                                        )
                                    }

                                    IconButton(
                                        onClick = { viewModel.onSendSingleAbsentWhatsApp(context, absentee) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Chat,
                                            contentDescription = "Send WhatsApp",
                                            tint = WhatsAppDark,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        color = AbsentRedLight,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "WhatsApp Alert message explains the importance of board exam regularity and requests parent verification.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = AbsentRed,
                                fontSize = 11.sp
                            ),
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.onConfirmSendAbsentAlerts(context) },
                    colors = ButtonDefaults.buttonColors(containerColor = AbsentRed),
                    modifier = Modifier.testTag("confirm_send_absent_alerts")
                ) {
                    Text("Alert All Absent Parents")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.setShowAbsentAlertDialog(false) }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun StudentAttendanceRow(
    student: AttendanceEntry,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("attendance_row_${student.studentId}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Avatar circle with initial
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (student.isPresent) NavyPrimary else AbsentRed),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = student.studentName.take(1),
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = student.studentName,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    Text(
                        text = "Roll: ${student.rollNo} • Parent: ${student.parentPhone}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            // Tactile P / A Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // PRESENT Button
                FilledIconButton(
                    onClick = { if (!student.isPresent) onToggle() },
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = if (student.isPresent) SuccessGreen else SurfaceVariantSlate,
                        contentColor = if (student.isPresent) Color.White else TextSecondary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("p_btn_${student.studentId}")
                ) {
                    Text(
                        text = "P",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp
                        )
                    )
                }

                // ABSENT Button
                FilledIconButton(
                    onClick = { if (student.isPresent) onToggle() },
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = if (!student.isPresent) AbsentRed else SurfaceVariantSlate,
                        contentColor = if (!student.isPresent) Color.White else TextSecondary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("a_btn_${student.studentId}")
                ) {
                    Text(
                        text = "A",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp
                        )
                    )
                }
            }
        }
    }
}
