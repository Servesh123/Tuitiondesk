package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.network.whatsapp.WhatsAppBusinessApiClient
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun WhatsAppApiSettingsDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var tokenInput by remember { mutableStateOf(WhatsAppBusinessApiClient.getActiveApiToken()) }
    var phoneIdInput by remember { mutableStateOf(WhatsAppBusinessApiClient.getActivePhoneNumberId()) }
    var testPhoneInput by remember { mutableStateOf("+91 98765 43210") }
    var isTesting by remember { mutableStateOf(false) }
    var testResult by remember { mutableStateOf<String?>(null) }

    val isConfigured = WhatsAppBusinessApiClient.isApiConfigured()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(WhatsAppDark.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudSync,
                        contentDescription = null,
                        tint = WhatsAppDark,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "WhatsApp Business Cloud API",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Automated Meta Cloud Integration",
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
                // Connection Status Card
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isConfigured) SuccessGreenLight else GoldContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isConfigured) Icons.Default.CheckCircle else Icons.Default.Info,
                            contentDescription = null,
                            tint = if (isConfigured) SuccessGreen else GoldSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isConfigured) "Cloud API Active & Connected" else "API Credentials Needed",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isConfigured) SuccessGreen else GoldSecondary
                                )
                            )
                            Text(
                                text = if (isConfigured) {
                                    "Automated fee reminders and absent alerts will dispatch directly from the cloud."
                                } else {
                                    "App is currently using 1-tap WhatsApp deep link intents. Configure API below for full background automation."
                                },
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextPrimary,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }

                // Phone Number ID Input
                OutlinedTextField(
                    value = phoneIdInput,
                    onValueChange = {
                        phoneIdInput = it
                        WhatsAppBusinessApiClient.customPhoneNumberId = it
                    },
                    label = { Text("WhatsApp Phone Number ID") },
                    placeholder = { Text("e.g. 106549872134567") },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("whatsapp_phone_id_input")
                )

                // API Token Input
                OutlinedTextField(
                    value = tokenInput,
                    onValueChange = {
                        tokenInput = it
                        WhatsAppBusinessApiClient.customApiToken = it
                    },
                    label = { Text("System User Access Token (Bearer)") },
                    placeholder = { Text("EAAxxxxxx...") },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("whatsapp_token_input")
                )

                // Test Message Dispatcher
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceVariantSlate)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Test Cloud API Dispatch:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = testPhoneInput,
                            onValueChange = { testPhoneInput = it },
                            label = { Text("Recipient Mobile Number") },
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                isTesting = true
                                testResult = null
                                coroutineScope.launch {
                                    val testMsg = "Namaste! This is an automated test notification from TuitionDesk WhatsApp Business API integration."
                                    val result = WhatsAppBusinessApiClient.sendTextMessage(testPhoneInput, testMsg)
                                    isTesting = false
                                    result.onSuccess { resp ->
                                        val id = resp.messages?.firstOrNull()?.id ?: "OK"
                                        testResult = "✅ Sent! Message ID: $id"
                                        Toast.makeText(context, "Test message sent successfully!", Toast.LENGTH_SHORT).show()
                                    }.onFailure { err ->
                                        testResult = "❌ Error: ${err.message}"
                                    }
                                }
                            },
                            enabled = !isTesting,
                            colors = ButtonDefaults.buttonColors(containerColor = WhatsAppDark),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isTesting) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Sending via Meta Cloud...")
                            } else {
                                Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Send Test Message")
                            }
                        }

                        if (testResult != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = testResult!!,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (testResult!!.startsWith("✅")) SuccessGreen else AbsentRed
                                )
                            )
                        }
                    }
                }

                Surface(
                    color = SurfaceVariantSlate,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "💡 How to get permanent credentials:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "1. Visit developers.facebook.com -> My Apps -> WhatsApp -> API Setup\n2. Copy Phone Number ID & Temporary/Permanent Access Token\n3. Save in AI Studio Secrets panel as WHATSAPP_API_TOKEN and WHATSAPP_PHONE_NUMBER_ID for APK builds.",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, color = TextSecondary)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
            ) {
                Text("Save & Close")
            }
        }
    )
}
