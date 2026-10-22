package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.model.HomeworkItem
import com.example.ui.theme.*
import com.example.viewmodel.TuitionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentViewScreen(
    viewModel: TuitionViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val selectedStudentId by viewModel.selectedStudentId.collectAsState()
    val homeworks by viewModel.homeworks.collectAsState()
    val currentFee by viewModel.selectedStudentFee.collectAsState()
    var showUpiSheet by remember { mutableStateOf(false) }

    // Demo student options
    val studentProfiles = listOf(
        Triple("S01", "Aarav Sharma", "Class 10 - Maths & Science"),
        Triple("S05", "Priya Patel", "Class 10 - All Subjects"),
        Triple("S02", "Rohan Verma", "Class 10 - Maths")
    )

    val currentStudent = studentProfiles.find { it.first == selectedStudentId } ?: studentProfiles[0]

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(NeutralBg)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Parent View Switcher & Header
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NavyDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("parent_portal_header_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = Color.White.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(
                                text = "PARENT PORTAL VIEW",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = GoldLight,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        Text(
                            text = "Live Student Sync",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = SuccessGreenLight,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Viewing as Parent of:",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.White.copy(alpha = 0.75f),
                            fontSize = 11.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Switcher chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        studentProfiles.forEach { (id, name, _) ->
                            val isSelected = id == selectedStudentId
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.selectStudent(id) },
                                label = {
                                    Text(
                                        text = name,
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
                                modifier = Modifier.testTag("parent_student_chip_$id")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "${currentStudent.second} • ${currentStudent.third}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
            }
        }

        // FEE DUE CARD with PAY BY UPI BUTTON (Prompt Requirement)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (currentFee?.isPaid == true) SuccessGreenLight else SurfaceWhite
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = if (currentFee?.isPaid == true) SuccessGreen else AbsentRed.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .testTag("fee_due_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (currentFee?.isPaid == true) SuccessGreen else AbsentRed),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (currentFee?.isPaid == true) Icons.Default.CheckCircle else Icons.Default.CurrencyRupee,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (currentFee?.isPaid == true) "Monthly Tuition Fee Paid" else "Monthly Tuition Fee Due",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                )
                                Text(
                                    text = "Batch: ${currentStudent.third}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "₹${currentFee?.amountDue ?: 2500}",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (currentFee?.isPaid == true) SuccessGreen else AbsentRed
                                )
                            )
                            Text(
                                text = if (currentFee?.isPaid == true) "Cleared" else "${currentFee?.daysLate ?: 12} days overdue",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (currentFee?.isPaid == true) SuccessGreen else AbsentRed,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (currentFee?.isPaid == true) {
                        Surface(
                            color = SuccessGreen.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = null,
                                    tint = SuccessGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Payment verified on ${currentFee?.paidDate ?: "Today"}. Receipt #TD-${System.currentTimeMillis() % 100000} sent to parent WhatsApp.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = SuccessGreen,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val fee = currentFee ?: return@OutlinedButton
                                    val receipt = com.example.util.ReportExportHelper.generateFeeReceiptText(viewModel.centreInfo, fee)
                                    com.example.util.ReportExportHelper.downloadToDevice(
                                        context,
                                        "Fee_Receipt_${fee.studentName.replace(" ", "_")}.txt",
                                        receipt,
                                        "text/plain"
                                    )
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Download Slip", fontSize = 11.sp)
                            }

                            Button(
                                onClick = {
                                    val fee = currentFee ?: return@Button
                                    val receipt = com.example.util.ReportExportHelper.generateFeeReceiptText(viewModel.centreInfo, fee)
                                    val file = com.example.util.ReportExportHelper.saveContentToFile(
                                        context,
                                        "Fee_Receipt_${fee.studentName.replace(" ", "_")}.txt",
                                        receipt
                                    )
                                    com.example.util.ReportExportHelper.shareFile(context, file, "text/plain", "Share Fee Receipt Slip")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Share Slip", fontSize = 11.sp)
                            }
                        }
                    } else {
                        // PAY BY UPI BUTTON (Prompt Requirement)
                        Button(
                            onClick = {
                                showUpiSheet = true
                                viewModel.onPayByUpi(context, currentStudent.second, currentFee?.amountDue ?: 2500)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = UpiPurple,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("pay_by_upi_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Payment,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Pay by UPI (GPay / PhonePe / Paytm)",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Instant UPI receipt to ${viewModel.centreInfo.upiId}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextSecondary,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }
            }
        }

        // ATTENDANCE % SECTION (Prompt Requirement)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("parent_attendance_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Class Attendance",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                            Text(
                                text = "Monthly attendance record (October 2026)",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            )
                        }

                        Surface(
                            color = SuccessGreenLight,
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(
                                text = "Regular Attendance",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = SuccessGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Circular attendance badge and progress stats
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(SuccessGreen.copy(alpha = 0.12f))
                                .border(4.dp, SuccessGreen, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "88%",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = SuccessGreen
                                    )
                                )
                                Text(
                                    text = "Present",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.sp,
                                        color = TextSecondary
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Classes Attended",
                                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                                )
                                Text(
                                    text = "22 / 25 classes",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Classes Missed",
                                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                                )
                                Text(
                                    text = "3 classes (Leave informed)",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = AbsentRed
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            LinearProgressIndicator(
                                progress = { 0.88f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = SuccessGreen,
                                trackColor = BorderLight
                            )
                        }
                    }
                }
            }
        }

        // HOMEWORK SECTION (Prompt Requirement)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Homework & Assignments",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    Text(
                        text = "Track completion & teacher feedback",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    )
                }

                Surface(
                    color = GoldContainer,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "${homeworks.count { it.isCompleted }}/${homeworks.size} Done",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = GoldSecondary
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        items(homeworks, key = { it.id }) { item ->
            HomeworkRowCard(
                item = item,
                onToggle = { viewModel.toggleHomework(item.id) }
            )
        }
    }

    // Modal: UPI Payment Assistance & Fallback
    if (showUpiSheet) {
        AlertDialog(
            onDismissRequest = { showUpiSheet = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Payment,
                    contentDescription = null,
                    tint = UpiPurple,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "UPI Payment Details",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column {
                    Text(
                        text = "Pay ₹${currentFee?.amountDue ?: 2500} directly to ${viewModel.centreInfo.name}:",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        color = SurfaceVariantSlate,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "COACHING UPI ID",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.sp,
                                        color = TextSecondary,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = viewModel.centreInfo.upiId,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = NavyPrimary
                                    )
                                )
                            }

                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("UPI ID", viewModel.centreInfo.upiId)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "UPI ID copied to clipboard!", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy UPI ID",
                                    tint = NavyPrimary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val upiRequest = com.example.util.UpiPaymentRequest(
                        payeeVpa = viewModel.centreInfo.upiId,
                        payeeName = viewModel.centreInfo.name,
                        amount = (currentFee?.amountDue ?: 2500).toDouble(),
                        transactionNote = "Tuition Fee - ${currentStudent.second}"
                    )
                    val upiUrl = com.example.util.UpiPaymentHelper.generateUpiUrl(upiRequest)

                    Button(
                        onClick = {
                            com.example.util.UpiPaymentHelper.launchUpiPayment(context, upiRequest)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = UpiPurple),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("launch_upi_intent_button")
                    ) {
                        Icon(imageVector = Icons.Default.Launch, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Open Installed UPI App", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("UPI URL", upiUrl)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "UPI Intent URL copied!", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("copy_upi_url_button")
                    ) {
                        Icon(imageVector = Icons.Default.Link, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy UPI Payment URL", fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Supported Apps: Google Pay, PhonePe, Paytm, BHIM, Cred, Amazon Pay.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.markFeePaid(currentFee?.id ?: "F01", "UPI")
                        showUpiSheet = false
                        Toast.makeText(context, "Payment marked as complete!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                ) {
                    Text("I Have Paid (Confirm)")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUpiSheet = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun HomeworkRowCard(
    item: HomeworkItem,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("homework_card_${item.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Checkbox(
                checked = item.isCompleted,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = SuccessGreen,
                    checkmarkColor = Color.White
                ),
                modifier = Modifier.testTag("hw_checkbox_${item.id}")
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = SurfaceVariantSlate,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = item.subject,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = NavyPrimary,
                                fontSize = 10.sp
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Text(
                        text = "Due: ${item.dueDate}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = if (item.isCompleted) TextSecondary else AbsentRed,
                            fontWeight = if (item.isCompleted) FontWeight.Normal else FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                )

                if (item.teacherNote.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Teacher note: ${item.teacherNote}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }
    }
}
