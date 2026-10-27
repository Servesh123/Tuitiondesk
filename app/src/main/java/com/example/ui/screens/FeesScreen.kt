package com.example.ui.screens

import android.content.Context
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
import com.example.model.FeeRecord
import com.example.ui.components.MetricStatCard
import com.example.ui.theme.*
import com.example.viewmodel.FeeFilter
import com.example.viewmodel.TuitionViewModel

@Composable
fun FeesScreen(
    viewModel: TuitionViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val fees by viewModel.filteredFees.collectAsState()
    val filter by viewModel.feeFilter.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val totalPending by viewModel.totalPendingAmount.collectAsState()
    val totalCollected by viewModel.totalCollectedAmount.collectAsState()
    val overdueCount by viewModel.overdueCount.collectAsState()
    val showAllDialog by viewModel.showAllReminderDialog.collectAsState()

    var markPaidTarget by remember { mutableStateOf<FeeRecord?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(NeutralBg)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Broadcast Action Button (Prominent Feature)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = WhatsAppDark),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("send_all_reminders_card")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "1-Click WhatsApp Broadcast",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = "Send automated payment reminder to all overdue parents",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { viewModel.setShowAllReminderDialog(true) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = WhatsAppGreen,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("broadcast_whatsapp_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Send WhatsApp Reminder to All ($overdueCount Overdue)",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }

        // Summary Metric Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricStatCard(
                    title = "Pending Dues",
                    value = "₹$totalPending",
                    subtitle = "$overdueCount students late",
                    icon = Icons.Default.Warning,
                    containerColor = AbsentRedLight,
                    contentColor = AbsentRed,
                    modifier = Modifier.weight(1f)
                )
                MetricStatCard(
                    title = "Collected (Oct)",
                    value = "₹$totalCollected",
                    subtitle = "Received & cleared",
                    icon = Icons.Default.CheckCircle,
                    containerColor = SuccessGreenLight,
                    contentColor = SuccessGreen,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Search and Filters
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Search by student, roll no, or batch...") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = TextSecondary)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NavyPrimary,
                    unfocusedBorderColor = BorderLight,
                    focusedContainerColor = SurfaceWhite,
                    unfocusedContainerColor = SurfaceWhite
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("fee_search_field")
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = filter == FeeFilter.ALL,
                    onClick = { viewModel.setFeeFilter(FeeFilter.ALL) },
                    label = { Text("All Students") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NavyPrimary,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.testTag("filter_all")
                )
                FilterChip(
                    selected = filter == FeeFilter.OVERDUE,
                    onClick = { viewModel.setFeeFilter(FeeFilter.OVERDUE) },
                    label = { Text("Overdue ($overdueCount)") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AbsentRed,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.testTag("filter_overdue")
                )
                FilterChip(
                    selected = filter == FeeFilter.PAID,
                    onClick = { viewModel.setFeeFilter(FeeFilter.PAID) },
                    label = { Text("Paid") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SuccessGreen,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.testTag("filter_paid")
                )
            }
        }

        // Quick Export / Download Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "STUDENT FEE ROSTER (${fees.size})",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 1.sp
                    )
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilledTonalButton(
                        onClick = {
                            val csv = com.example.util.ReportExportHelper.generateFeesCsv(fees)
                            com.example.util.ReportExportHelper.downloadToDevice(context, "TuitionDesk_Fees_Ledger.csv", csv, "text/csv")
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier
                            .height(32.dp)
                            .testTag("quick_download_fees_csv")
                    ) {
                        Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("CSV", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    FilledTonalButton(
                        onClick = {
                            val csv = com.example.util.ReportExportHelper.generateFeesCsv(fees)
                            val file = com.example.util.ReportExportHelper.saveContentToFile(context, "TuitionDesk_Fees_Ledger.csv", csv)
                            com.example.util.ReportExportHelper.shareFile(context, file, "text/csv", "Share Fee Ledger CSV")
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier
                            .height(32.dp)
                            .testTag("quick_share_fees_csv")
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Share", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Student Fee Items
        if (fees.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = SuccessGreen,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No fee records matching filter",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = "All accounts are up to date!",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                        )
                    }
                }
            }
        } else {
            items(fees, key = { it.id }) { fee ->
                StudentFeeCard(
                    fee = fee,
                    onSendWhatsApp = { viewModel.onSendSingleWhatsAppReminder(context, fee) },
                    onMarkPaid = { markPaidTarget = fee }
                )
            }
        }
    }

    // Modal Dialog: Send All Reminders Confirmation
    if (showAllDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.setShowAllReminderDialog(false) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Chat,
                    contentDescription = null,
                    tint = WhatsAppDark,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Broadcast Fee Reminder?",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column {
                    Text(
                        text = "This will prepare WhatsApp reminders for all $overdueCount students with overdue fees (Total: ₹$totalPending).",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        color = SurfaceVariantSlate,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Template includes: Student name, exact amount due, days late, and your UPI ID (${viewModel.centreInfo.value.upiId}) with instant 1-tap payment link.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextSecondary,
                                fontSize = 11.sp
                            ),
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.onSendAllPendingFeesWhatsApp(context) },
                    colors = ButtonDefaults.buttonColors(containerColor = WhatsAppDark),
                    modifier = Modifier.testTag("confirm_broadcast_whatsapp")
                ) {
                    Text("Launch WhatsApp")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.setShowAllReminderDialog(false) }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Modal Dialog: Mark as Paid
    if (markPaidTarget != null) {
        val target = markPaidTarget!!
        var selectedMode by remember { mutableStateOf("UPI") }

        AlertDialog(
            onDismissRequest = { markPaidTarget = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = SuccessGreen,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Record Fee Payment",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column {
                    Text(
                        text = "Mark ₹${target.amountDue} as paid for ${target.studentName} (${target.rollNo})?",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Select Payment Mode:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("UPI", "Cash", "Cheque").forEach { mode ->
                            FilterChip(
                                selected = selectedMode == mode,
                                onClick = { selectedMode = mode },
                                label = { Text(mode) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.markFeePaid(target.id, selectedMode)
                        markPaidTarget = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                    modifier = Modifier.testTag("confirm_mark_paid")
                ) {
                    Text("Confirm Payment")
                }
            },
            dismissButton = {
                TextButton(onClick = { markPaidTarget = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun StudentFeeCard(
    fee: FeeRecord,
    onSendWhatsApp: () -> Unit,
    onMarkPaid: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("fee_card_${fee.studentId}")
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = fee.studentName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = SurfaceVariantSlate,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = fee.rollNo,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    color = TextSecondary
                                ),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = fee.batchName,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    )
                }

                // Amount Due
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "₹${fee.amountDue}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (fee.isPaid) SuccessGreen else AbsentRed
                        )
                    )
                    Text(
                        text = fee.month,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            color = TextSecondary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Status Banner / Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (fee.isPaid) {
                    Surface(
                        color = SuccessGreenLight,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = SuccessGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Paid on ${fee.paidDate} via ${fee.paymentMode}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SuccessGreen,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                } else {
                    Surface(
                        color = AbsentRedLight,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = AbsentRed,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${fee.daysLate} days late • Due ${fee.dueDate}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = AbsentRed,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }

                Text(
                    text = "Parent: ${fee.parentPhone}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            if (!fee.isPaid) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onSendWhatsApp,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = WhatsAppGreen,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier
                            .weight(1.3f)
                            .height(40.dp)
                            .testTag("whatsapp_btn_${fee.studentId}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = "WhatsApp",
                            tint = Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "WhatsApp Reminder",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        )
                    }

                    OutlinedButton(
                        onClick = onMarkPaid,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SuccessGreen),
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .testTag("mark_paid_btn_${fee.studentId}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircleOutline,
                            contentDescription = "Mark Paid",
                            tint = SuccessGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Mark Paid",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    OutlinedButton(
                        onClick = {
                            val receipt = com.example.util.ReportExportHelper.generateFeeReceiptText(com.example.model.TuitionCentre(), fee)
                            com.example.util.ReportExportHelper.downloadToDevice(
                                context,
                                "Receipt_${fee.studentName.replace(" ", "_")}.txt",
                                receipt,
                                "text/plain"
                            )
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .testTag("download_receipt_btn_${fee.studentId}")
                    ) {
                        Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Download Slip", fontSize = 11.sp)
                    }

                    Button(
                        onClick = {
                            val receipt = com.example.util.ReportExportHelper.generateFeeReceiptText(com.example.model.TuitionCentre(), fee)
                            val file = com.example.util.ReportExportHelper.saveContentToFile(
                                context,
                                "Receipt_${fee.studentName.replace(" ", "_")}.txt",
                                receipt
                            )
                            com.example.util.ReportExportHelper.shareFile(context, file, "text/plain", "Share Fee Receipt Slip")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .testTag("share_receipt_btn_${fee.studentId}")
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Share Slip", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
