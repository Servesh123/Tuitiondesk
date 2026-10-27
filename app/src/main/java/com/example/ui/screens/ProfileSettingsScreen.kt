package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.model.TuitionCentre
import com.example.network.whatsapp.WhatsAppBusinessApiClient
import com.example.ui.theme.*
import com.example.viewmodel.TuitionViewModel

@Composable
fun ProfileSettingsScreen(
    viewModel: TuitionViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val centreInfo by viewModel.centreInfo.collectAsState()

    var instituteName by remember(centreInfo) { mutableStateOf(centreInfo.name) }
    var tagline by remember(centreInfo) { mutableStateOf(centreInfo.tagline) }
    var ownerName by remember(centreInfo) { mutableStateOf(centreInfo.ownerName) }
    var phone by remember(centreInfo) { mutableStateOf(centreInfo.phone) }
    var upiId by remember(centreInfo) { mutableStateOf(centreInfo.upiId) }
    var location by remember(centreInfo) { mutableStateOf(centreInfo.location) }

    var showResetConfirmDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(NeutralBg)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Institute & Director Details Card (Fully Editable & Room Persisted)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("institute_profile_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(NavyPrimary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = NavyPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Coaching Institute Profile",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = NavyPrimary
                                )
                            )
                            Text(
                                text = "Details appear on WhatsApp reminders, receipts, and UPI requests",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = instituteName,
                        onValueChange = { instituteName = it },
                        label = { Text("Institute / Academy Name") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("institute_name_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = tagline,
                        onValueChange = { tagline = it },
                        label = { Text("Tagline / Boards (e.g. CBSE & ICSE Hub)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = ownerName,
                        onValueChange = { ownerName = it },
                        label = { Text("Teacher / Director Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Contact Number (WhatsApp)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = upiId,
                        onValueChange = { upiId = it },
                        label = { Text("UPI ID for Fee Collection (e.g. name@okhdfcbank)") },
                        placeholder = { Text("tuition@upi") },
                        singleLine = true,
                        trailingIcon = {
                            Icon(Icons.Default.QrCode, contentDescription = null, tint = SuccessGreen)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("upi_id_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Tuition Centre Address / Location") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val updated = TuitionCentre(
                                name = instituteName.trim().ifBlank { "My Tuition Centre" },
                                tagline = tagline.trim(),
                                ownerName = ownerName.trim(),
                                phone = phone.trim(),
                                upiId = upiId.trim(),
                                location = location.trim()
                            )
                            viewModel.updateCentreProfile(updated)
                            Toast.makeText(context, "Institute profile saved persistently!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("save_profile_button")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save Profile Changes", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // WhatsApp Business Cloud API Integration Status
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
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
                                    .background(WhatsAppLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudSync,
                                    contentDescription = null,
                                    tint = WhatsAppDark,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "WhatsApp Cloud API",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                val isConfigured = WhatsAppBusinessApiClient.isApiConfigured()
                                Text(
                                    text = if (isConfigured) "Configured (Automated 1-Click)" else "Using Native WhatsApp App",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = if (isConfigured) SuccessGreen else TextSecondary,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }

                        FilledTonalButton(
                            onClick = { viewModel.setShowWhatsAppApiSettings(true) },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = WhatsAppLight,
                                contentColor = WhatsAppDark
                            )
                        ) {
                            Text("Configure")
                        }
                    }
                }
            }
        }

        // Quick Tools & Portals
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Institute Tools & Reports",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    SettingActionRow(
                        icon = Icons.Default.Assessment,
                        title = "Weekly Test Analysis & Leaderboard",
                        subtitle = "Track test scores, batch ranks, and progress cards",
                        onClick = { viewModel.setShowTestsReportDialog(true) }
                    )

                    HorizontalDivider(color = NeutralBg, modifier = Modifier.padding(vertical = 4.dp))

                    SettingActionRow(
                        icon = Icons.Default.FamilyRestroom,
                        title = "Parent View & UPI Payment Portal",
                        subtitle = "Preview what parents see and pay fees online",
                        onClick = { viewModel.setShowParentPortalDialog(true) }
                    )

                    HorizontalDivider(color = NeutralBg, modifier = Modifier.padding(vertical = 4.dp))

                    SettingActionRow(
                        icon = Icons.Default.Share,
                        title = "Export & Share Institute Reports",
                        subtitle = "Generate WhatsApp report card & summary",
                        onClick = { viewModel.setShowExportShareDialog(true) }
                    )

                    HorizontalDivider(color = NeutralBg, modifier = Modifier.padding(vertical = 4.dp))

                    SettingActionRow(
                        icon = Icons.Default.Lightbulb,
                        title = "Director Pitch Guide Playbook",
                        subtitle = "Growth strategies and retention techniques",
                        onClick = { viewModel.setShowPitchGuideSheet(true) }
                    )
                }
            }
        }

        // Demo Data & Reset Application
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Data Management",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = {
                            viewModel.loadDemoData()
                            Toast.makeText(context, "Sample demo data loaded!", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Load Sample Demo Data")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = { showResetConfirmDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AbsentRed),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reset App to Clean State")
                    }
                }
            }
        }

        // App Information Footer
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "TuitionDesk v1.0.0",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextSecondary,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "Built for Indian Coaching Centres & Private Tutors",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                )
            }
        }
    }

    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = { Text("Reset to Clean State?") },
            text = { Text("This will delete all enrolled students, fee records, and attendance logs. Are you sure?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllData()
                        showResetConfirmDialog = false
                        Toast.makeText(context, "Application reset to clean state", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AbsentRed)
                ) {
                    Text("Reset All Data")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showResetConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SettingActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(NeutralBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = NavyPrimary,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp)
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(18.dp)
        )
    }
}
