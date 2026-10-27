package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.model.FeeRecord
import com.example.ui.theme.*
import com.example.util.ReportExportHelper
import com.example.viewmodel.TuitionViewModel

@Composable
fun ExportShareDialog(
    viewModel: TuitionViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val fees by viewModel.fees.collectAsState()
    val attendance by viewModel.attendance.collectAsState()
    val testScores by viewModel.testScores.collectAsState()
    val leaderboard by viewModel.leaderboard.collectAsState()
    val latestTest = testScores.lastOrNull()

    var selectedReceiptFee by remember { mutableStateOf<FeeRecord?>(fees.firstOrNull()) }
    var previewText by remember { mutableStateOf<String?>(null) }
    var previewTitle by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(NavyPrimary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        tint = NavyPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "In-App Export & Share Hub",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Download files to device & share anywhere",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Section 1: Fee Ledger CSV
                ExportItemCard(
                    title = "Monthly Fee Ledger (CSV)",
                    subtitle = "${fees.size} students (${fees.count { it.isPaid }} Paid, ${fees.count { !it.isPaid }} Overdue)",
                    icon = Icons.Default.AccountBalanceWallet,
                    iconTint = NavyPrimary,
                    onDownload = {
                        val csv = ReportExportHelper.generateFeesCsv(fees)
                        ReportExportHelper.downloadToDevice(context, "TuitionDesk_Fees_October_2026.csv", csv, "text/csv")
                    },
                    onShare = {
                        val csv = ReportExportHelper.generateFeesCsv(fees)
                        val file = ReportExportHelper.saveContentToFile(context, "TuitionDesk_Fees_October_2026.csv", csv)
                        ReportExportHelper.shareFile(context, file, "text/csv", "Share Fee Ledger CSV")
                    },
                    onPreview = {
                        previewTitle = "Monthly Fee Ledger"
                        previewText = ReportExportHelper.generateFeesCsv(fees)
                    }
                )

                // Section 2: Attendance Register CSV
                ExportItemCard(
                    title = "Attendance Register (CSV)",
                    subtitle = "22 Oct 2026 • ${attendance.size} student logs recorded",
                    icon = Icons.Default.FactCheck,
                    iconTint = SuccessGreen,
                    onDownload = {
                        val csv = ReportExportHelper.generateAttendanceCsv(attendance)
                        ReportExportHelper.downloadToDevice(context, "TuitionDesk_Attendance_22Oct2026.csv", csv, "text/csv")
                    },
                    onShare = {
                        val csv = ReportExportHelper.generateAttendanceCsv(attendance)
                        val file = ReportExportHelper.saveContentToFile(context, "TuitionDesk_Attendance_22Oct2026.csv", csv)
                        ReportExportHelper.shareFile(context, file, "text/csv", "Share Attendance Register CSV")
                    },
                    onPreview = {
                        previewTitle = "Attendance Register"
                        previewText = ReportExportHelper.generateAttendanceCsv(attendance)
                    }
                )

                // Section 3: Test Leaderboard CSV
                if (latestTest != null) {
                    ExportItemCard(
                        title = "Test Scorecards (CSV)",
                        subtitle = "${latestTest.title} (${leaderboard.size} ranks)",
                        icon = Icons.Default.Assessment,
                        iconTint = GoldSecondary,
                        onDownload = {
                            val csv = ReportExportHelper.generateTestLeaderboardCsv(latestTest, leaderboard)
                            ReportExportHelper.downloadToDevice(context, "TuitionDesk_Test_Results.csv", csv, "text/csv")
                        },
                        onShare = {
                            val csv = ReportExportHelper.generateTestLeaderboardCsv(latestTest, leaderboard)
                            val file = ReportExportHelper.saveContentToFile(context, "TuitionDesk_Test_Results.csv", csv)
                            ReportExportHelper.shareFile(context, file, "text/csv", "Share Test Scores CSV")
                        },
                        onPreview = {
                            previewTitle = "Test Scorecards"
                            previewText = ReportExportHelper.generateTestLeaderboardCsv(latestTest, leaderboard)
                        }
                    )
                }

                // Section 4: Official Fee Payment Receipt
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceVariantSlate)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = null,
                                tint = UpiPurple,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Official Fee Payment Receipt",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Select Student for Receipt:",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Student selection row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            fees.take(3).forEach { fee ->
                                val isSelected = fee.id == selectedReceiptFee?.id
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedReceiptFee = fee },
                                    label = {
                                        Text(
                                            text = fee.studentName.split(" ").first(),
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp)
                                        )
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val fee = selectedReceiptFee ?: fees.first()
                                    val receipt = ReportExportHelper.generateFeeReceiptText(viewModel.centreInfo.value, fee)
                                    ReportExportHelper.downloadToDevice(context, "Receipt_${fee.studentName.replace(" ", "_")}.txt", receipt, "text/plain")
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Download", fontSize = 11.sp)
                            }

                            Button(
                                onClick = {
                                    val fee = selectedReceiptFee ?: fees.first()
                                    val receipt = ReportExportHelper.generateFeeReceiptText(viewModel.centreInfo.value, fee)
                                    val file = ReportExportHelper.saveContentToFile(context, "Receipt_${fee.studentName.replace(" ", "_")}.txt", receipt)
                                    ReportExportHelper.shareFile(context, file, "text/plain", "Share Official Fee Receipt")
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = UpiPurple),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Share Slip", fontSize = 11.sp)
                            }
                        }
                    }
                }

                // Section 5: Share App Button
                FilledTonalButton(
                    onClick = { ReportExportHelper.shareTuitionDeskApp(context) },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = GoldContainer,
                        contentColor = GoldSecondary
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.AppShortcut, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Share TuitionDesk App with Teachers", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
            ) {
                Text("Close")
            }
        }
    )

    // Sub-dialog: File Preview Modal
    if (previewText != null) {
        AlertDialog(
            onDismissRequest = { previewText = null },
            title = {
                Text(
                    text = "$previewTitle Preview",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Surface(
                    color = SurfaceVariantSlate,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = previewText!!,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            fontSize = 10.sp
                        ),
                        modifier = Modifier
                            .padding(10.dp)
                            .verticalScroll(rememberScrollState())
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val file = ReportExportHelper.saveContentToFile(context, "${previewTitle.replace(" ", "_")}.csv", previewText!!)
                        ReportExportHelper.shareFile(context, file, "text/csv", "Share $previewTitle")
                        previewText = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                ) {
                    Text("Share File")
                }
            },
            dismissButton = {
                TextButton(onClick = { previewText = null }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun ExportItemCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    onDownload: () -> Unit,
    onShare: () -> Unit,
    onPreview: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(iconTint.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedButton(
                    onClick = onDownload,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Download", fontSize = 11.sp)
                }

                Button(
                    onClick = onShare,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Share", fontSize = 11.sp)
                }

                IconButton(
                    onClick = onPreview,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(imageVector = Icons.Default.Visibility, contentDescription = "Preview", tint = TextSecondary, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
