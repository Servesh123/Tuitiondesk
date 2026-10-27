package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Student
import com.example.ui.theme.*
import com.example.viewmodel.TuitionViewModel

@Composable
fun StudentsScreen(
    viewModel: TuitionViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allStudents by viewModel.students.collectAsState()
    val filteredStudents by viewModel.filteredStudents.collectAsState()
    val searchQuery by viewModel.studentSearchQuery.collectAsState()
    val selectedBatchFilter by viewModel.studentBatchFilter.collectAsState()
    val batches = viewModel.batches

    val showAddDialog by viewModel.showAddStudentDialog.collectAsState()
    var studentToDelete by remember { mutableStateOf<Student?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.setShowAddStudentDialog(true) },
                containerColor = NavyPrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("add_student_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = "Add Student")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Student", fontWeight = FontWeight.Bold)
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
            contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Search and Filter Bar
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.setStudentSearchQuery(it) },
                            placeholder = { Text("Search by student, roll no, or parent phone...") },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = null, tint = NavyPrimary)
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.setStudentSearchQuery("") }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                                    }
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("student_search_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Batch Filter Chips
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = selectedBatchFilter == null,
                                onClick = { viewModel.setStudentBatchFilter(null) },
                                label = { Text("All Batches (${allStudents.size})") }
                            )
                            batches.forEach { batch ->
                                val count = allStudents.count { it.batchName == batch }
                                FilterChip(
                                    selected = selectedBatchFilter == batch,
                                    onClick = {
                                        viewModel.setStudentBatchFilter(if (selectedBatchFilter == batch) null else batch)
                                    },
                                    label = { Text("${batch.substringBefore(" (")} ($count)") }
                                )
                            }
                        }
                    }
                }
            }

            // Empty State Handling
            if (allStudents.isEmpty()) {
                item {
                    EmptyStudentsState(
                        onAddStudent = { viewModel.setShowAddStudentDialog(true) },
                        onLoadDemo = { viewModel.loadDemoData() }
                    )
                }
            } else if (filteredStudents.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
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
                                imageVector = Icons.Default.SearchOff,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No matching students found",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Try adjusting your search query or batch filter.",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            TextButton(onClick = {
                                viewModel.setStudentSearchQuery("")
                                viewModel.setStudentBatchFilter(null)
                            }) {
                                Text("Clear Filters")
                            }
                        }
                    }
                }
            } else {
                // List of Enrolled Students
                items(filteredStudents, key = { it.id }) { student ->
                    StudentItemCard(
                        student = student,
                        onDeleteClick = { studentToDelete = student },
                        onWhatsAppClick = {
                            val cleanPhone = student.parentPhone.replace(Regex("[^0-9]"), "")
                            val formattedPhone = if (cleanPhone.length == 10) "91$cleanPhone" else cleanPhone
                            try {
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    data = Uri.parse("https://wa.me/$formattedPhone?text=Hello%20${Uri.encode(student.parentName)},%20regarding%20${Uri.encode(student.name)}'s%20tuition%20classes.")
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Could not open WhatsApp", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onCallClick = {
                            try {
                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:${student.parentPhone}")
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Could not open dialer", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }
        }
    }

    // Add Student Dialog
    if (showAddDialog) {
        AddStudentDialog(
            batches = batches,
            onDismiss = { viewModel.setShowAddStudentDialog(false) },
            onConfirm = { name, rollNo, batch, parentName, phone, fee ->
                viewModel.addStudent(name, rollNo, batch, parentName, phone, fee)
                Toast.makeText(context, "Student added successfully!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Delete Student Confirmation Dialog
    studentToDelete?.let { student ->
        AlertDialog(
            onDismissRequest = { studentToDelete = null },
            title = { Text("Delete Student?") },
            text = { Text("Are you sure you want to remove ${student.name} (${student.rollNo})? This will also remove their associated fee records.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteStudent(student.id)
                        studentToDelete = null
                        Toast.makeText(context, "Student removed", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AbsentRed)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { studentToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun StudentItemCard(
    student: Student,
    onDeleteClick: () -> Unit,
    onWhatsAppClick: () -> Unit,
    onCallClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("student_card_${student.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Avatar with Initials
                val initials = student.name.split(" ")
                    .mapNotNull { it.firstOrNull()?.toString() }
                    .take(2)
                    .joinToString("")
                    .ifEmpty { "S" }

                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color(student.avatarColorHex)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = student.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = NeutralBg,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "Roll: ${student.rollNo}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = student.batchName.substringBefore(" ("),
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Delete Action
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete",
                        tint = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = NeutralBg, thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))

            // Parent Info & Contact
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Parent: ${student.parentName}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                    )
                    Text(
                        text = student.parentPhone,
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledTonalIconButton(
                        onClick = onWhatsAppClick,
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = WhatsAppLight,
                            contentColor = WhatsAppDark
                        ),
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = "WhatsApp Parent",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    FilledTonalIconButton(
                        onClick = onCallClick,
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = NavyPrimary
                        ),
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Call Parent",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Badges: Attendance & Pending Fee
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Attendance Badge
                val attColor = when {
                    student.attendancePct >= 80 -> SuccessGreen
                    student.attendancePct >= 70 -> GoldSecondary
                    else -> AbsentRed
                }
                Surface(
                    color = attColor.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FactCheck,
                            contentDescription = null,
                            tint = attColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Attendance: ${student.attendancePct}%",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = attColor,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                // Fee Status Badge
                val isPaid = student.pendingFee == 0
                val feeBg = if (isPaid) SuccessGreen.copy(alpha = 0.12f) else AbsentRed.copy(alpha = 0.12f)
                val feeColor = if (isPaid) SuccessGreen else AbsentRed

                Surface(
                    color = feeBg,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (isPaid) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = feeColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isPaid) "Fees Cleared" else "Due: ₹${student.pendingFee}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = feeColor,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyStudentsState(
    onAddStudent: () -> Unit,
    onLoadDemo: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.GroupAdd,
                    contentDescription = null,
                    tint = NavyPrimary,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "No students added yet",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = NavyPrimary
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Add your students to start tracking daily attendance, recording fee payments, and sending WhatsApp alerts to parents.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextSecondary,
                    lineHeight = 20.sp
                ),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onAddStudent,
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("empty_add_student_button")
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Your First Student", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = onLoadDemo,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("load_demo_data_button")
            ) {
                Icon(Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Load Sample Demo Data")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStudentDialog(
    batches: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (name: String, rollNo: String, batch: String, parentName: String, phone: String, fee: Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var rollNo by remember { mutableStateOf("") }
    var selectedBatch by remember { mutableStateOf(batches.firstOrNull() ?: "General Batch") }
    var parentName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var feeString by remember { mutableStateOf("2500") }
    var batchDropdownExpanded by remember { mutableStateOf(false) }

    val isValid = name.isNotBlank() && phone.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add New Student",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = NavyPrimary)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(androidx.compose.foundation.rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Student Full Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_student_name_input")
                )

                OutlinedTextField(
                    value = rollNo,
                    onValueChange = { rollNo = it },
                    label = { Text("Roll Number / ID (e.g. 10A-05)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Batch Selector Dropdown
                ExposedDropdownMenuBox(
                    expanded = batchDropdownExpanded,
                    onExpandedChange = { batchDropdownExpanded = !batchDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedBatch,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Class / Batch") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = batchDropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = batchDropdownExpanded,
                        onDismissRequest = { batchDropdownExpanded = false }
                    ) {
                        batches.forEach { batch ->
                            DropdownMenuItem(
                                text = { Text(batch) },
                                onClick = {
                                    selectedBatch = batch
                                    batchDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = parentName,
                    onValueChange = { parentName = it },
                    label = { Text("Parent / Guardian Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Parent WhatsApp Number *") },
                    placeholder = { Text("+91 98765 43210") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_student_phone_input")
                )

                OutlinedTextField(
                    value = feeString,
                    onValueChange = { feeString = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Monthly Tuition Fee (₹)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val fee = feeString.toIntOrNull() ?: 2500
                    val roll = rollNo.ifBlank { "S-${(10..99).random()}" }
                    val pName = parentName.ifBlank { "Parent of $name" }
                    onConfirm(name.trim(), roll.trim(), selectedBatch, pName.trim(), phone.trim(), fee)
                },
                enabled = isValid,
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
            ) {
                Text("Save Student")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
