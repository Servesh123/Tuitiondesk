package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun PitchGuideDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = GoldSecondary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Owner ko kaise dikhao?",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Coaching Client Closing Playbook",
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
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Step 1
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceVariantSlate)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "📱", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "1. Demo Phone Pe Dikhao",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = NavyPrimary
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Coaching owner ko bolne ke bajaye phone me app dikhao. 'Send WhatsApp Reminder to All' dabao — jab unke samne instant WhatsApp share khulega with student fee and UPI link, unka 80% mind wahin ban jata hai.",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextPrimary, fontSize = 12.sp)
                        )
                    }
                }

                // Step 2
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = SuccessGreenLight)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🎁", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "2. Pehla Mahina FREE Pilot Do",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SuccessGreen
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "No upfront risk. 'Sir, ek mahina free trial lijiye. Agar aapke institute ki uncollected fees recover na ho aur parents happy na ho, toh mujhe ek rupee bhi mat dijiyega.'",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextPrimary, fontSize = 12.sp)
                        )
                    }
                }

                // Step 3
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = GoldContainer)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "💰", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "3. One-Time Build + Chhota Monthly",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = GoldSecondary
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Pricing Structure:\n• One-time Build: ₹4,999 (custom institute logo, batches, students upload)\n• Monthly Maintenance: ₹499/month (cloud backup, updates, WhatsApp support)\nOwner ke liye 2 students ki 1-month fee se bhi kam cost hai!",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextPrimary, fontSize = 12.sp)
                        )
                    }
                }

                Button(
                    onClick = {
                        val pitchText = """
                            Namaste Sir! 
                            Maine local coaching institutes ke liye TuitionDesk banaya hai:
                            1. 1-Click WhatsApp Fee Reminders with UPI Payment link
                            2. Instant Absent Parents Alert on WhatsApp
                            3. Weekly Test Scores + 6-Tests Performance Bar Chart
                            
                            Sir, main aapko 30 Days ka FREE Pilot de raha hoon. Pasand aaye tabhi baat karenge. Can I show you a 2-minute live demo today?
                        """.trimIndent()
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Pitch Message", pitchText)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Pitch copied to clipboard!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldSecondary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy Owner Pitch Message")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
            ) {
                Text("Got It!")
            }
        }
    )
}
