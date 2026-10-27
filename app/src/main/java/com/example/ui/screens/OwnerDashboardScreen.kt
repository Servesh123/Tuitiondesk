package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import com.example.model.LeadStatus
import com.example.model.ReferralEnquiry
import com.example.network.whatsapp.WhatsAppBusinessApiClient
import com.example.ui.components.MetricStatCard
import com.example.ui.theme.*
import com.example.viewmodel.TuitionViewModel

@Composable
fun OwnerDashboardScreen(
    viewModel: TuitionViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val centreInfo by viewModel.centreInfo.collectAsState()
    val totalStudents by viewModel.totalStudentsCount.collectAsState()
    val enquiries by viewModel.enquiries.collectAsState()
    val showAddLead by viewModel.showAddLeadDialog.collectAsState()
    val showPitchSheet by viewModel.showPitchGuideSheet.collectAsState()
    var isPitchPlaybookExpanded by remember { mutableStateOf(false) }

    // Dynamic aggregate metrics directly from Room database
    val totalCollected by viewModel.totalCollectedAmount.collectAsState()
    val totalPending by viewModel.totalPendingAmount.collectAsState()
    val totalProjected = totalCollected + totalPending
    val collectedPct = if (totalProjected > 0) (totalCollected * 100) / totalProjected else 0

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(NeutralBg)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Owner Profile Header
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NavyDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("owner_header_card")
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
                                text = "COACHING DIRECTOR DESK",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = GoldLight,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        Text(
                            text = "$totalStudents Enrolled Students",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White.copy(alpha = 0.85f),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = centreInfo.name.ifBlank { "Vidya Tutorials & Coaching" },
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )

                    Text(
                        text = "${centreInfo.ownerName.ifBlank { "Director" }} • ${centreInfo.location.ifBlank { "Main Campus" }}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 11.sp
                        )
                    )
                }

            }
        }

        // "OWNER KO KAISE DIKHAO" PITCH PLAYBOOK (Direct User Request)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = GoldContainer),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(GoldSecondary)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("owner_pitch_card")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isPitchPlaybookExpanded = !isPitchPlaybookExpanded },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Campaign,
                                contentDescription = null,
                                tint = GoldSecondary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Owner ko kaise dikhao (Pitch Playbook)",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                )
                                Text(
                                    text = "3-step strategy to close coaching clients",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }

                        IconButton(onClick = { isPitchPlaybookExpanded = !isPitchPlaybookExpanded }) {
                            Icon(
                                imageVector = if (isPitchPlaybookExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = "Toggle",
                                tint = GoldSecondary
                            )
                        }
                    }

                    AnimatedVisibility(visible = isPitchPlaybookExpanded) {
                        Column(modifier = Modifier.padding(top = 12.dp)) {
                            // Step 1
                            PitchStepRow(
                                stepNumber = "1",
                                title = "Demo phone pe dikhao",
                                description = "Live WhatsApp broadcast tap karke dikhao. Owner dekhega ki ek click mein 10 parents ko fee reminder aur absent alert chala gaya."
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Step 2
                            PitchStepRow(
                                stepNumber = "2",
                                title = "Pehla mahina FREE pilot do",
                                description = "Bolo: 'Sir, 30 din chalao bina kisi risk ke. Aapki uncollected fee recover hogi tabhi aage baat karenge.'"
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Step 3
                            PitchStepRow(
                                stepNumber = "3",
                                title = "One-time build + Chhota monthly maintenance",
                                description = "Pricing Model: ₹4,999 One-time setup & custom branding + ₹499/month (Cloud backup, WhatsApp templates & support)."
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Quick Action: Copy Pitch Script
                            FilledTonalButton(
                                onClick = {
                                    val script = """
                                        Namaste Sir! Maine coaching institutes ke liye TuitionDesk app banayi hai:
                                        1. Ek click pe sabhi pending fees ka WhatsApp reminder chala jata hai with UPI link.
                                        2. Attendance save karte hi absent bacchon ke parents ko alert milta hai.
                                        3. Tests ke scorecards aur 6 weeks ka bar chart parents ko WhatsApp pe share hota hai.
                                        
                                        Aapke coaching centre ke liye pehla mahina bilkul FREE pilot dunga. Pasand aaye toh one-time build + chhota monthly maintenance!
                                    """.trimIndent()
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Pitch Script", script)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Pitch Script copied! Share with coaching owners.", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = SurfaceWhite,
                                    contentColor = GoldSecondary
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("copy_pitch_script_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Copy Pitch Script (Hinglish)",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }
            }
        }

        // WHATSAPP BUSINESS CLOUD API INTEGRATION CARD
        item {
            val isApiConfigured = WhatsAppBusinessApiClient.isApiConfigured()
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(if (isApiConfigured) WhatsAppDark else GoldSecondary)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("whatsapp_api_status_card")
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
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(if (isApiConfigured) WhatsAppLight else GoldContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudSync,
                                    contentDescription = null,
                                    tint = if (isApiConfigured) WhatsAppDark else GoldSecondary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "WhatsApp Business Cloud API",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                )
                                Text(
                                    text = "Automated Direct Meta Dispatch",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }

                        Surface(
                            color = if (isApiConfigured) SuccessGreenLight else GoldContainer,
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(
                                text = if (isApiConfigured) "API Active" else "Fallback Mode",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isApiConfigured) SuccessGreen else GoldSecondary,
                                    fontSize = 10.sp
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (isApiConfigured) {
                            "Direct HTTP dispatch is active. Fee reminders and absent alerts are sent directly via Meta Cloud API without opening the WhatsApp application."
                        } else {
                            "Deep-link mode active. To enable 100% automated background sending, configure your Meta Developer Access Token and Phone Number ID."
                        },
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.setShowWhatsAppApiSettings(true) },
                            colors = ButtonDefaults.buttonColors(containerColor = WhatsAppDark),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("configure_whatsapp_api_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Configure API",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        OutlinedButton(
                            onClick = { viewModel.setShowWhatsAppApiSettings(true) },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("test_whatsapp_api_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Test",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }

        // FEES COLLECTED VS PENDING (Prompt Requirement)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("fees_collected_vs_pending_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Monthly Fee Collection",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                            Text(
                                text = "Target: ₹1,73,000 for October",
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
                                text = "$collectedPct% Recovered",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SuccessGreen,
                                    fontSize = 11.sp
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Progress Split Bar
                    LinearProgressIndicator(
                        progress = { collectedPct / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp)),
                        color = SuccessGreen,
                        trackColor = AbsentRed
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Collected Box
                        Surface(
                            color = SuccessGreenLight,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(SuccessGreen)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Fees Collected",
                                        style = MaterialTheme.typography.labelSmall.copy(color = SuccessGreen, fontWeight = FontWeight.Bold)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "₹1,42,000",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = SuccessGreen
                                    )
                                )
                                Text(
                                    text = "52 students paid",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 10.sp,
                                        color = TextSecondary
                                    )
                                )
                            }
                        }

                        // Pending Box
                        Surface(
                            color = AbsentRedLight,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(AbsentRed)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Fees Pending",
                                        style = MaterialTheme.typography.labelSmall.copy(color = AbsentRed, fontWeight = FontWeight.Bold)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "₹31,000",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = AbsentRed
                                    )
                                )
                                Text(
                                    text = "12 students late",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 10.sp,
                                        color = TextSecondary
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val report = """
                                    TUITIONDESK MONTHLY AUDIT REPORT
                                    Institute: ${centreInfo.name}
                                    Director: ${centreInfo.ownerName}
                                    Contact: ${centreInfo.phone}
                                    ----------------------------------------
                                    Total Expected Fees   : ₹$totalProjected
                                    Fees Collected        : ₹$totalCollected ($collectedPct%)
                                    Fees Pending          : ₹$totalPending
                                    Total Active Students : $totalStudents
                                    New Referral Leads    : ${enquiries.size} in pipeline
                                    ----------------------------------------
                                    Generated via TuitionDesk Pro
                                """.trimIndent()
                                com.example.util.ReportExportHelper.downloadToDevice(

                                    context,
                                    "TuitionDesk_Audit_Report_Oct2026.txt",
                                    report,
                                    "text/plain"
                                )
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("download_audit_report_button")
                        ) {
                            Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Download Audit", fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                com.example.util.ReportExportHelper.shareTuitionDeskApp(context)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("share_app_owner_button")
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Share App", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // STUDENTS & BATCHES OVERVIEW (Prompt Requirement)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricStatCard(
                    title = "Total Students",
                    value = "64 Active",
                    subtitle = "Across 4 Batches",
                    icon = Icons.Default.Groups,
                    containerColor = SurfaceWhite,
                    contentColor = NavyPrimary,
                    modifier = Modifier.weight(1f)
                )
                MetricStatCard(
                    title = "Avg Attendance",
                    value = "86%",
                    subtitle = "High board readiness",
                    icon = Icons.Default.Checklist,
                    containerColor = SurfaceWhite,
                    contentColor = SuccessGreen,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // NEW REFERRAL ENQUIRIES SECTION (Prompt Requirement)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "New Referral Enquiries",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    Text(
                        text = "${enquiries.size} active prospective students",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    )
                }

                Button(
                    onClick = { viewModel.setShowAddLeadDialog(true) },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("add_enquiry_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Add Lead",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }

        items(enquiries, key = { it.id }) { lead ->
            ReferralEnquiryCard(
                enquiry = lead,
                onStatusChange = { newStatus -> viewModel.updateEnquiryStatus(lead.id, newStatus) },
                onCall = {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${lead.parentPhone}"))
                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Dialer unavailable", Toast.LENGTH_SHORT).show()
                    }
                },
                onWhatsApp = {
                    val cleanPhone = lead.parentPhone.replace(Regex("[^0-9]"), "")
                    val formatted = if (cleanPhone.length == 10) "91$cleanPhone" else cleanPhone
                    val msg = "Namaste ${lead.parentName} Ji, this is regarding ${lead.studentName}'s tuition inquiry at ${centreInfo.name}."
                    val url = "https://api.whatsapp.com/send?phone=$formatted&text=${Uri.encode(msg)}"
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "WhatsApp unavailable", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
    }

    // Modal: Add New Referral Enquiry Dialog
    if (showAddLead) {
        var studentName by remember { mutableStateOf("") }
        var gradeClass by remember { mutableStateOf("Class 10 CBSE") }
        var parentName by remember { mutableStateOf("") }
        var parentPhone by remember { mutableStateOf("") }
        var subject by remember { mutableStateOf("Maths & Science") }
        var referredBy by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { viewModel.setShowAddLeadDialog(false) },
            icon = {
                Icon(
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = null,
                    tint = NavyPrimary,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Add Referral Enquiry",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = studentName,
                        onValueChange = { studentName = it },
                        label = { Text("Student Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = gradeClass,
                        onValueChange = { gradeClass = it },
                        label = { Text("Target Class (e.g. Class 10 CBSE)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = parentName,
                        onValueChange = { parentName = it },
                        label = { Text("Parent Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = parentPhone,
                        onValueChange = { parentPhone = it },
                        label = { Text("Parent Phone (10 digits)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = referredBy,
                        onValueChange = { referredBy = it },
                        label = { Text("Referred By (e.g. Existing Parent / Banner)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (studentName.isNotBlank() && parentPhone.isNotBlank()) {
                            viewModel.addEnquiry(studentName, gradeClass, parentName, parentPhone, subject, referredBy)
                        } else {
                            Toast.makeText(context, "Please enter student name and phone", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                ) {
                    Text("Save Lead")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.setShowAddLeadDialog(false) }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun PitchStepRow(
    stepNumber: String,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(GoldSecondary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stepNumber,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            )
        }
    }
}

@Composable
fun ReferralEnquiryCard(
    enquiry: ReferralEnquiry,
    onStatusChange: (LeadStatus) -> Unit,
    onCall: () -> Unit,
    onWhatsApp: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("enquiry_card_${enquiry.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = enquiry.studentName,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    Text(
                        text = "${enquiry.gradeClass} • ${enquiry.subject}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    )
                }

                // Status chip with color
                val statusColor = when (enquiry.status) {
                    LeadStatus.NEW -> NavyLight
                    LeadStatus.DEMO_BOOKED -> GoldSecondary
                    LeadStatus.FOLLOW_UP -> AbsentRed
                    LeadStatus.ENROLLED -> SuccessGreen
                }

                Surface(
                    color = statusColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = enquiry.status.label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = statusColor,
                            fontSize = 10.sp
                        ),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Parent: ${enquiry.parentName} (${enquiry.parentPhone})",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            )

            Text(
                text = "Referred by: ${enquiry.referredBy} • ${enquiry.notes}",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Action row: Status changer + Call + WhatsApp
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status Switcher chips
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    LeadStatus.values().forEach { status ->
                        val isSelected = status == enquiry.status
                        AssistChip(
                            onClick = { onStatusChange(status) },
                            label = {
                                Text(
                                    text = status.name.take(4),
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp)
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (isSelected) NavyPrimary else SurfaceVariantSlate,
                                labelColor = if (isSelected) Color.White else TextSecondary
                            )
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconButton(
                        onClick = onCall,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Call Parent",
                            tint = NavyPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onWhatsApp,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = "WhatsApp",
                            tint = WhatsAppDark,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
