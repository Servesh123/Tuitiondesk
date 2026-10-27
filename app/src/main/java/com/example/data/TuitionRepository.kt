package com.example.data

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.data.local.*
import com.example.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TuitionRepository(
    val database: TuitionDeskDatabase? = null,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
) {

    // Institute & Teacher Profile
    private val _centreInfo = MutableStateFlow(TuitionCentre())
    val centreInfo: StateFlow<TuitionCentre> = _centreInfo.asStateFlow()
    val currentCentreInfo: TuitionCentre get() = _centreInfo.value

    // Batches
    val batches = listOf(
        "Class 10 - Maths (5:00 PM)",
        "Class 12 - Physics (6:30 PM)",
        "Class 9 - Science (4:00 PM)",
        "JEE/NEET Foundation (7:30 PM)"
    )

    // Students State (Clean initial state, synchronized with Room)
    private val _students = MutableStateFlow<List<Student>>(emptyList())
    val students: StateFlow<List<Student>> = _students.asStateFlow()

    // Fees State (Clean initial state, synchronized with Room)
    private val _fees = MutableStateFlow<List<FeeRecord>>(emptyList())
    val fees: StateFlow<List<FeeRecord>> = _fees.asStateFlow()

    // Attendance State: mapping batch to list of attendance entries
    private val _attendance = MutableStateFlow<List<AttendanceEntry>>(emptyList())
    val attendance: StateFlow<List<AttendanceEntry>> = _attendance.asStateFlow()

    // Selected batch for attendance
    private val _selectedBatch = MutableStateFlow(batches[0])
    val selectedBatch: StateFlow<String> = _selectedBatch.asStateFlow()

    // Selected student for Parent View
    private val _selectedStudentId = MutableStateFlow("S01")
    val selectedStudentId: StateFlow<String> = _selectedStudentId.asStateFlow()

    // Enquiries State
    private val _enquiries = MutableStateFlow<List<ReferralEnquiry>>(emptyList())
    val enquiries: StateFlow<List<ReferralEnquiry>> = _enquiries.asStateFlow()

    // Tests & Leaderboard State
    private val _testScores = MutableStateFlow<List<TestScoreItem>>(initialTests())
    val testScores: StateFlow<List<TestScoreItem>> = _testScores.asStateFlow()

    private val _leaderboard = MutableStateFlow<List<StudentRank>>(initialLeaderboard())
    val leaderboard: StateFlow<List<StudentRank>> = _leaderboard.asStateFlow()

    // Homework State
    private val _homeworks = MutableStateFlow<List<HomeworkItem>>(emptyList())
    val homeworks: StateFlow<List<HomeworkItem>> = _homeworks.asStateFlow()

    init {
        // If a Room database instance is provided, synchronize flows continuously
        if (database != null) {
            scope.launch {
                syncWithRoom()
            }
        }
    }

    private suspend fun syncWithRoom() {
        val db = database ?: return
        // Collect Students updates from Room
        scope.launch {
            db.studentDao().getAllStudents().collect { studentEntities ->
                _students.value = studentEntities.map { it.toDomain() }
                // If selectedStudentId is not in students, point to first available
                if (_students.value.none { it.id == _selectedStudentId.value } && _students.value.isNotEmpty()) {
                    _selectedStudentId.value = _students.value.first().id
                }
            }
        }

        // Collect Fee updates from Room
        scope.launch {
            db.feeDao().getAllFees().collect { feeEntities ->
                _fees.value = feeEntities.map { it.toDomain() }
            }
        }

        // Collect Attendance updates from Room
        scope.launch {
            db.attendanceDao().getAttendanceForDate("22 Oct 2026").collect { logEntities ->
                _attendance.value = logEntities.map { it.toDomain() }
            }
        }

        // Collect Institute Profile updates from Room
        scope.launch {
            db.centreProfileDao().getProfile().collect { profileEntity ->
                if (profileEntity != null) {
                    _centreInfo.value = profileEntity.toDomain()
                }
            }
        }
    }

    fun selectBatch(batch: String) {
        _selectedBatch.value = batch
    }

    fun selectStudent(studentId: String) {
        _selectedStudentId.value = studentId
    }

    // --- Student CRUD Operations ---
    fun addStudent(student: Student, initialFee: Int = 2500) {
        database?.let { db ->
            scope.launch {
                db.studentDao().insertStudent(StudentEntity.fromDomain(student))
                if (initialFee > 0) {
                    val feeRecord = FeeRecord(
                        id = "F-${System.currentTimeMillis() % 100000}",
                        studentId = student.id,
                        studentName = student.name,
                        rollNo = student.rollNo,
                        batchName = student.batchName,
                        amountDue = initialFee,
                        dueDate = "10th of Month",
                        daysLate = 0,
                        isPaid = false,
                        parentPhone = student.parentPhone
                    )
                    db.feeDao().insertFee(FeeEntity.fromDomain(feeRecord))
                }
                val log = AttendanceLogEntity(
                    studentId = student.id,
                    studentName = student.name,
                    rollNo = student.rollNo,
                    batchId = student.batchId,
                    batchName = student.batchName,
                    parentPhone = student.parentPhone,
                    parentName = student.parentName,
                    isPresent = true,
                    date = "22 Oct 2026"
                )
                db.attendanceDao().insertAttendanceLog(log)
            }
        } ?: run {
            _students.update { it + student }
            if (initialFee > 0) {
                val feeRecord = FeeRecord(
                    id = "F-${System.currentTimeMillis() % 100000}",
                    studentId = student.id,
                    studentName = student.name,
                    rollNo = student.rollNo,
                    batchName = student.batchName,
                    amountDue = initialFee,
                    dueDate = "10th of Month",
                    daysLate = 0,
                    isPaid = false,
                    parentPhone = student.parentPhone
                )
                _fees.update { it + feeRecord }
            }
        }
    }

    fun updateStudent(student: Student) {
        database?.let { db ->
            scope.launch {
                db.studentDao().insertStudent(StudentEntity.fromDomain(student))
            }
        } ?: run {
            _students.update { list -> list.map { if (it.id == student.id) student else it } }
        }
    }

    fun deleteStudent(studentId: String) {
        database?.let { db ->
            scope.launch {
                db.studentDao().deleteStudentById(studentId)
                val studentFees = _fees.value.filter { it.studentId == studentId }
                studentFees.forEach { db.feeDao().deleteFeeById(it.id) }
            }
        } ?: run {
            _students.update { list -> list.filter { it.id != studentId } }
            _fees.update { list -> list.filter { it.studentId != studentId } }
            _attendance.update { list -> list.filter { it.studentId != studentId } }
        }
    }

    // --- Fee CRUD Operations ---
    fun addFeeRecord(fee: FeeRecord) {
        database?.let { db ->
            scope.launch {
                db.feeDao().insertFee(FeeEntity.fromDomain(fee))
            }
        } ?: run {
            _fees.update { listOf(fee) + it }
        }
    }

    fun deleteFeeRecord(feeId: String) {
        database?.let { db ->
            scope.launch {
                db.feeDao().deleteFeeById(feeId)
            }
        } ?: run {
            _fees.update { list -> list.filter { it.id != feeId } }
        }
    }

    // --- Institute Profile Persistence ---
    fun updateCentreProfile(centre: TuitionCentre) {
        _centreInfo.value = centre
        database?.let { db ->
            scope.launch {
                db.centreProfileDao().saveProfile(CentreProfileEntity.fromDomain(centre))
            }
        }
    }

    // --- Demo Data & Data Reset ---
    fun loadDemoData() {
        val sampleStudents = initialStudents()
        val sampleFees = initialFees()
        val sampleAttendance = initialAttendance()
        val sampleEnquiries = initialEnquiries()
        database?.let { db ->
            scope.launch {
                db.studentDao().insertStudents(sampleStudents.map { StudentEntity.fromDomain(it) })
                db.feeDao().insertFees(sampleFees.map { FeeEntity.fromDomain(it) })
                db.attendanceDao().insertAttendanceLogs(sampleAttendance.map { AttendanceLogEntity.fromDomain(it, "22 Oct 2026") })
            }
        } ?: run {
            _students.value = sampleStudents
            _fees.value = sampleFees
            _attendance.value = sampleAttendance
        }
        _enquiries.value = sampleEnquiries
        _testScores.value = initialTests()
        _leaderboard.value = initialLeaderboard()
        _homeworks.value = initialHomeworks()
    }

    fun clearAllData() {
        database?.let { db ->
            scope.launch {
                db.studentDao().deleteAllStudents()
                db.feeDao().deleteAllFees()
                db.attendanceDao().deleteAllAttendance()
            }
        } ?: run {
            _students.value = emptyList()
            _fees.value = emptyList()
            _attendance.value = emptyList()
        }
        _enquiries.value = emptyList()
        _homeworks.value = emptyList()
    }


    fun toggleAttendance(studentId: String) {
        val currentEntry = _attendance.value.find { it.studentId == studentId } ?: return
        val newStatus = !currentEntry.isPresent

        _attendance.update { list ->
            list.map {
                if (it.studentId == studentId) it.copy(isPresent = newStatus) else it
            }
        }

        // Persist change to Room Database
        database?.let { db ->
            scope.launch {
                db.attendanceDao().updateStudentAttendance(studentId, "22 Oct 2026", newStatus)
            }
        }
    }

    fun markAllPresentForSelectedBatch() {
        val currentBatch = _selectedBatch.value
        _attendance.update { list ->
            list.map {
                if (it.batchName == currentBatch) it.copy(isPresent = true) else it
            }
        }

        // Persist change to Room Database
        database?.let { db ->
            scope.launch {
                db.attendanceDao().markAllPresentInBatch(currentBatch, "22 Oct 2026")
            }
        }
    }

    fun markFeePaid(feeId: String, mode: String = "UPI") {
        _fees.update { list ->
            list.map { fee ->
                if (fee.id == feeId) {
                    fee.copy(
                        isPaid = true,
                        paidDate = "22 Oct 2026",
                        paymentMode = mode
                    )
                } else fee
            }
        }

        // Persist change to Room Database
        database?.let { db ->
            scope.launch {
                db.feeDao().updateFeePaymentStatus(feeId, true, "22 Oct 2026", mode)
            }
        }
    }

    fun toggleHomework(homeworkId: String) {
        _homeworks.update { list ->
            list.map { hw ->
                if (hw.id == homeworkId) hw.copy(isCompleted = !hw.isCompleted) else hw
            }
        }
    }

    fun updateEnquiryStatus(enquiryId: String, newStatus: LeadStatus) {
        _enquiries.update { list ->
            list.map {
                if (it.id == enquiryId) it.copy(status = newStatus) else it
            }
        }
    }

    fun addEnquiry(studentName: String, gradeClass: String, parentName: String, parentPhone: String, subject: String, referredBy: String) {
        val newLead = ReferralEnquiry(
            id = "ENQ-${System.currentTimeMillis() % 10000}",
            studentName = studentName,
            gradeClass = gradeClass,
            parentName = parentName,
            parentPhone = parentPhone,
            subject = subject,
            status = LeadStatus.NEW,
            date = "Today",
            referredBy = if (referredBy.isBlank()) "Direct Walk-in" else referredBy,
            notes = "Inquired about batch timings and fee structure."
        )
        _enquiries.update { listOf(newLead) + it }
    }

    // --- Intent & WhatsApp Communication Helpers (delegating to WhatsAppIntentHelper and WhatsApp Business Cloud API) ---

    fun sendSingleWhatsAppFeeReminder(context: Context, fee: FeeRecord, onStatus: ((String) -> Unit)? = null) {
        if (com.example.network.whatsapp.WhatsAppBusinessApiClient.isApiConfigured()) {
            scope.launch {
                onStatus?.invoke("Sending via WhatsApp Cloud API...")
                val result = com.example.network.whatsapp.WhatsAppBusinessApiClient.sendFeeReminderAutomated(fee, currentCentreInfo)
                result.onSuccess { resp ->
                    val msgId = resp.messages?.firstOrNull()?.id ?: "OK"
                    val msg = "Automated reminder sent via WhatsApp Cloud API! (ID: $msgId)"
                    onStatus?.invoke(msg)
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        Toast.makeText(context, "✅ $msg", Toast.LENGTH_LONG).show()
                    }
                }.onFailure { err ->
                    val fallbackMsg = "Cloud API failed (${err.message}). Opening WhatsApp App..."
                    onStatus?.invoke(fallbackMsg)
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        Toast.makeText(context, fallbackMsg, Toast.LENGTH_SHORT).show()
                        com.example.util.WhatsAppIntentHelper.sendFeeReminder(context, fee, currentCentreInfo)
                    }
                }
            }
        } else {
            com.example.util.WhatsAppIntentHelper.sendFeeReminder(context, fee, currentCentreInfo)
        }
    }

    fun sendAllPendingFeesWhatsApp(context: Context, onStatus: ((String) -> Unit)? = null): Int {
        val pending = _fees.value.filter { !it.isPaid }
        if (pending.isEmpty()) {
            Toast.makeText(context, "No pending fees!", Toast.LENGTH_SHORT).show()
            return 0
        }

        if (com.example.network.whatsapp.WhatsAppBusinessApiClient.isApiConfigured()) {
            scope.launch {
                onStatus?.invoke("Dispatching ${pending.size} reminders via WhatsApp Cloud API...")
                val results = com.example.network.whatsapp.WhatsAppBusinessApiClient.sendBatchFeeReminders(pending, currentCentreInfo)
                val successCount = results.count { it.second.isSuccess }
                val msg = "Dispatched $successCount of ${pending.size} fee reminders via WhatsApp Cloud API!"
                onStatus?.invoke(msg)
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    Toast.makeText(context, "✅ $msg", Toast.LENGTH_LONG).show()
                }
            }
        } else {
            com.example.util.WhatsAppIntentHelper.sendBatchFeeReminders(context, pending, currentCentreInfo)
        }
        return pending.size
    }

    fun saveAttendanceAndSendAbsentAlerts(context: Context, batchName: String, onStatus: ((String) -> Unit)? = null): List<AttendanceEntry> {
        val absentees = _attendance.value.filter { it.batchName == batchName && !it.isPresent }
        if (absentees.isEmpty()) {
            Toast.makeText(context, "All students present! Attendance saved successfully.", Toast.LENGTH_LONG).show()
            return emptyList()
        }

        if (com.example.network.whatsapp.WhatsAppBusinessApiClient.isApiConfigured()) {
            scope.launch {
                onStatus?.invoke("Dispatching ${absentees.size} absent alerts via WhatsApp Cloud API...")
                val results = com.example.network.whatsapp.WhatsAppBusinessApiClient.sendBatchAbsentAlerts(absentees, currentCentreInfo)
                val successCount = results.count { it.second.isSuccess }
                val msg = "Dispatched $successCount of ${absentees.size} absent alerts via WhatsApp Cloud API!"
                onStatus?.invoke(msg)
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    Toast.makeText(context, "✅ $msg", Toast.LENGTH_LONG).show()
                }
            }
        } else {
            com.example.util.WhatsAppIntentHelper.sendBatchAbsentAlerts(context, absentees, batchName, currentCentreInfo)
        }
        return absentees
    }

    fun sendSingleAbsentWhatsApp(context: Context, entry: AttendanceEntry, onStatus: ((String) -> Unit)? = null) {
        if (com.example.network.whatsapp.WhatsAppBusinessApiClient.isApiConfigured()) {
            scope.launch {
                onStatus?.invoke("Sending absent alert via WhatsApp Cloud API...")
                val result = com.example.network.whatsapp.WhatsAppBusinessApiClient.sendAbsentAlertAutomated(entry, currentCentreInfo)
                result.onSuccess { resp ->
                    val msgId = resp.messages?.firstOrNull()?.id ?: "OK"
                    val msg = "Absence alert delivered to ${entry.parentName} via WhatsApp API! (ID: $msgId)"
                    onStatus?.invoke(msg)
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        Toast.makeText(context, "✅ $msg", Toast.LENGTH_LONG).show()
                    }
                }.onFailure { err ->
                    val fallbackMsg = "Cloud API failed (${err.message}). Opening WhatsApp App..."
                    onStatus?.invoke(fallbackMsg)
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        Toast.makeText(context, fallbackMsg, Toast.LENGTH_SHORT).show()
                        com.example.util.WhatsAppIntentHelper.sendAbsentAlert(context, entry, currentCentreInfo)
                    }
                }
            }
        } else {
            com.example.util.WhatsAppIntentHelper.sendAbsentAlert(context, entry, currentCentreInfo)
        }
    }

    fun shareTestReportWhatsApp(context: Context, test: TestScoreItem) {
        com.example.util.WhatsAppIntentHelper.sendTestScorecard(context, test, currentCentreInfo)
    }

    fun launchUpiPayment(context: Context, studentName: String, amount: Int) {
        val request = com.example.util.UpiPaymentRequest(
            payeeVpa = currentCentreInfo.upiId,
            payeeName = currentCentreInfo.name,
            amount = amount.toDouble(),
            transactionNote = "Tuition Fee - $studentName"
        )
        com.example.util.UpiPaymentHelper.launchUpiPayment(context, request)
    }


    // --- Initial Mock Data Providers ---

    private fun initialStudents(): List<Student> = listOf(
        Student("S01", "Aarav Sharma", "10A-04", "B1", "Class 10 - Maths (5:00 PM)", "Mr. Manoj Sharma", "+91 98234 56789", 88, 2500),
        Student("S02", "Rohan Verma", "10A-11", "B1", "Class 10 - Maths (5:00 PM)", "Mrs. Sunita Verma", "+91 97123 45670", 74, 2500),
        Student("S03", "Ananya Gupta", "12P-02", "B2", "Class 12 - Physics (6:30 PM)", "Dr. A. Gupta", "+91 98989 12345", 92, 3200),
        Student("S04", "Devansh Joshi", "09S-07", "B3", "Class 9 - Science (4:00 PM)", "Mr. B. Joshi", "+91 96543 21098", 85, 2200),
        Student("S05", "Priya Patel", "10A-01", "B1", "Class 10 - Maths (5:00 PM)", "Mr. K. Patel", "+91 99887 76655", 96, 0),
        Student("S06", "Kabir Singh", "12P-09", "B2", "Class 12 - Physics (6:30 PM)", "Mr. G. Singh", "+91 98711 22334", 78, 3200),
        Student("S07", "Ishita Sen", "09S-14", "B3", "Class 9 - Science (4:00 PM)", "Mrs. S. Sen", "+91 91234 56780", 94, 0),
        Student("S08", "Manish Tiwari", "10A-18", "B1", "Class 10 - Maths (5:00 PM)", "Mr. R. Tiwari", "+91 93456 78901", 82, 2500)
    )

    private fun initialFees(): List<FeeRecord> = listOf(
        FeeRecord(
            id = "F01",
            studentId = "S01",
            studentName = "Aarav Sharma",
            rollNo = "10A-04",
            batchName = "Class 10 - Maths (5:00 PM)",
            amountDue = 2500,
            dueDate = "10 Oct 2026",
            daysLate = 12,
            isPaid = false,
            parentPhone = "+91 98234 56789"
        ),
        FeeRecord(
            id = "F02",
            studentId = "S02",
            studentName = "Rohan Verma",
            rollNo = "10A-11",
            batchName = "Class 10 - Maths (5:00 PM)",
            amountDue = 2500,
            dueDate = "15 Oct 2026",
            daysLate = 7,
            isPaid = false,
            parentPhone = "+91 97123 45670"
        ),
        FeeRecord(
            id = "F03",
            studentId = "S03",
            studentName = "Ananya Gupta",
            rollNo = "12P-02",
            batchName = "Class 12 - Physics (6:30 PM)",
            amountDue = 3200,
            dueDate = "05 Oct 2026",
            daysLate = 17,
            isPaid = false,
            parentPhone = "+91 98989 12345"
        ),
        FeeRecord(
            id = "F04",
            studentId = "S04",
            studentName = "Devansh Joshi",
            rollNo = "09S-07",
            batchName = "Class 9 - Science (4:00 PM)",
            amountDue = 2200,
            dueDate = "18 Oct 2026",
            daysLate = 4,
            isPaid = false,
            parentPhone = "+91 96543 21098"
        ),
        FeeRecord(
            id = "F05",
            studentId = "S05",
            studentName = "Priya Patel",
            rollNo = "10A-01",
            batchName = "Class 10 - Maths (5:00 PM)",
            amountDue = 2500,
            dueDate = "10 Oct 2026",
            daysLate = 0,
            isPaid = true,
            paidDate = "08 Oct 2026",
            paymentMode = "UPI",
            parentPhone = "+91 99887 76655"
        ),
        FeeRecord(
            id = "F06",
            studentId = "S06",
            studentName = "Kabir Singh",
            rollNo = "12P-09",
            batchName = "Class 12 - Physics (6:30 PM)",
            amountDue = 3200,
            dueDate = "12 Oct 2026",
            daysLate = 10,
            isPaid = false,
            parentPhone = "+91 98711 22334"
        ),
        FeeRecord(
            id = "F07",
            studentId = "S07",
            studentName = "Ishita Sen",
            rollNo = "09S-14",
            batchName = "Class 9 - Science (4:00 PM)",
            amountDue = 2200,
            dueDate = "10 Oct 2026",
            daysLate = 0,
            isPaid = true,
            paidDate = "10 Oct 2026",
            paymentMode = "Cash",
            parentPhone = "+91 91234 56780"
        ),
        FeeRecord(
            id = "F08",
            studentId = "S08",
            studentName = "Manish Tiwari",
            rollNo = "10A-18",
            batchName = "Class 10 - Maths (5:00 PM)",
            amountDue = 2500,
            dueDate = "20 Oct 2026",
            daysLate = 2,
            isPaid = false,
            parentPhone = "+91 93456 78901"
        )
    )

    private fun initialAttendance(): List<AttendanceEntry> = listOf(
        AttendanceEntry("S01", "Aarav Sharma", "10A-04", "B1", "Class 10 - Maths (5:00 PM)", "+91 98234 56789", "Mr. Manoj Sharma", true),
        AttendanceEntry("S02", "Rohan Verma", "10A-11", "B1", "Class 10 - Maths (5:00 PM)", "+91 97123 45670", "Mrs. Sunita Verma", false),
        AttendanceEntry("S05", "Priya Patel", "10A-01", "B1", "Class 10 - Maths (5:00 PM)", "+91 99887 76655", "Mr. K. Patel", true),
        AttendanceEntry("S08", "Manish Tiwari", "10A-18", "B1", "Class 10 - Maths (5:00 PM)", "+91 93456 78901", "Mr. R. Tiwari", true),
        AttendanceEntry("S09", "Simran Kaur", "10A-05", "B1", "Class 10 - Maths (5:00 PM)", "+91 98111 22233", "Mr. H. Singh", true),
        AttendanceEntry("S10", "Aditya Mehra", "10A-12", "B1", "Class 10 - Maths (5:00 PM)", "+91 98222 33344", "Mrs. P. Mehra", false),
        AttendanceEntry("S11", "Tanvi Deshmukh", "10A-07", "B1", "Class 10 - Maths (5:00 PM)", "+91 98333 44455", "Mr. V. Deshmukh", true),
        AttendanceEntry("S12", "Harsh Vardhan", "10A-19", "B1", "Class 10 - Maths (5:00 PM)", "+91 98444 55566", "Mr. S. Vardhan", true),

        // Class 12 Batch
        AttendanceEntry("S03", "Ananya Gupta", "12P-02", "B2", "Class 12 - Physics (6:30 PM)", "+91 98989 12345", "Dr. A. Gupta", true),
        AttendanceEntry("S06", "Kabir Singh", "12P-09", "B2", "Class 12 - Physics (6:30 PM)", "+91 98711 22334", "Mr. G. Singh", false),
        AttendanceEntry("S13", "Siddharth Roy", "12P-04", "B2", "Class 12 - Physics (6:30 PM)", "+91 98555 66677", "Mrs. M. Roy", true),
        AttendanceEntry("S14", "Meera Nair", "12P-08", "B2", "Class 12 - Physics (6:30 PM)", "+91 98666 77788", "Mr. R. Nair", true),

        // Class 9 Batch
        AttendanceEntry("S04", "Devansh Joshi", "09S-07", "B3", "Class 9 - Science (4:00 PM)", "+91 96543 21098", "Mr. B. Joshi", true),
        AttendanceEntry("S07", "Ishita Sen", "09S-14", "B3", "Class 9 - Science (4:00 PM)", "+91 91234 56780", "Mrs. S. Sen", true),
        AttendanceEntry("S15", "Kunal Agarwal", "09S-03", "B3", "Class 9 - Science (4:00 PM)", "+91 98777 88899", "Mr. P. Agarwal", false)
    )

    private fun initialTests(): List<TestScoreItem> = listOf(
        TestScoreItem(1, "Test 1: Trigonometry Basics", "Maths", "08 Sep 2026", 34, 50, 31, 47, 6, 24),
        TestScoreItem(2, "Test 2: Quadratic Equations", "Maths", "15 Sep 2026", 38, 50, 33, 49, 4, 24),
        TestScoreItem(3, "Test 3: Triangles & Similarity", "Maths", "22 Sep 2026", 41, 50, 35, 48, 3, 24),
        TestScoreItem(4, "Test 4: Coordinate Geometry", "Maths", "29 Sep 2026", 37, 50, 34, 50, 5, 24),
        TestScoreItem(5, "Test 5: Arithmetic Progressions", "Maths", "06 Oct 2026", 44, 50, 36, 49, 2, 24),
        TestScoreItem(6, "Test 6: Full Term Pre-Boards", "Maths", "14 Oct 2026", 48, 50, 38, 50, 2, 24)
    )

    private fun initialLeaderboard(): List<StudentRank> = listOf(
        StudentRank(1, "Priya Patel", "10A-01", 50, 50, 100),
        StudentRank(2, "Aarav Sharma", "10A-04", 48, 50, 96),
        StudentRank(3, "Simran Kaur", "10A-05", 46, 50, 92),
        StudentRank(4, "Tanvi Deshmukh", "10A-07", 43, 50, 86),
        StudentRank(5, "Harsh Vardhan", "10A-19", 40, 50, 80),
        StudentRank(6, "Rohan Verma", "10A-11", 37, 50, 74),
        StudentRank(7, "Manish Tiwari", "10A-18", 35, 50, 70),
        StudentRank(8, "Aditya Mehra", "10A-12", 32, 50, 64)
    )

    private fun initialHomeworks(): List<HomeworkItem> = listOf(
        HomeworkItem("HW1", "S01", "Mathematics", "Trigonometric Identities (NCERT Ex 8.4 Q5 - 10 parts)", "Tomorrow (5:00 PM)", false, "Write detailed step-by-step proofs in practice register."),
        HomeworkItem("HW2", "S01", "Science", "Ray Optics Numerical Worksheet (Q1 to Q15)", "Yesterday", true, "Checked by teacher. Score: 14/15. Great accuracy!"),
        HomeworkItem("HW3", "S01", "Mathematics", "Quadratic Equations Formula Sheet & 10 Word Problems", "18 Oct 2026", true, "Verified in class."),
        HomeworkItem("HW4", "S01", "English", "Letter to the Editor & Analytical Paragraph", "25 Oct 2026", false, "Prepare for internal marks assessment.")
    )

    private fun initialEnquiries(): List<ReferralEnquiry> = listOf(
        ReferralEnquiry(
            id = "ENQ-101",
            studentName = "Aryan Malhotra",
            gradeClass = "Class 10 CBSE",
            parentName = "Sunil Malhotra",
            parentPhone = "+91 98112 34567",
            subject = "Maths & Science (Board Prep)",
            status = LeadStatus.DEMO_BOOKED,
            date = "Yesterday",
            referredBy = "Priya Patel's Father",
            notes = "Attending demo on Saturday 5 PM. Keen on board exam result track record."
        ),
        ReferralEnquiry(
            id = "ENQ-102",
            studentName = "Kavya Rawat",
            gradeClass = "Class 12 Boards",
            parentName = "Neelam Rawat",
            parentPhone = "+91 97234 56789",
            subject = "Physics & Chemistry",
            status = LeadStatus.NEW,
            date = "Today, 10:30 AM",
            referredBy = "Aarav Sharma's Mother",
            notes = "Needs urgent help in Ray Optics and Organic Chemistry mechanisms."
        ),
        ReferralEnquiry(
            id = "ENQ-103",
            studentName = "Yuvraj Choudhary",
            gradeClass = "Class 9 Foundation",
            parentName = "Vikram Choudhary",
            parentPhone = "+91 99100 22334",
            subject = "Maths & Science",
            status = LeadStatus.FOLLOW_UP,
            date = "2 days ago",
            referredBy = "School Flyer / Banner",
            notes = "Demo given. Liked teaching style, waiting for fee discount confirmation."
        ),
        ReferralEnquiry(
            id = "ENQ-104",
            studentName = "Diya Sengupta",
            gradeClass = "Class 10 CBSE",
            parentName = "Amit Sengupta",
            parentPhone = "+91 98450 11223",
            subject = "Mathematics Only",
            status = LeadStatus.ENROLLED,
            date = "3 days ago",
            referredBy = "Dr. Gupta (Class 12 Parent)",
            notes = "Enrolled and fee paid. Joined evening batch."
        )
    )
}
