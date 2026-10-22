package com.example.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.TuitionRepository
import com.example.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.data.local.TuitionDeskDatabase

enum class TuitionTab(val title: String) {
    FEES("Fees"),
    ATTENDANCE("Attendance"),
    TESTS("Tests"),
    PARENT("Parent View"),
    OWNER("Owner")
}

enum class FeeFilter {
    ALL,
    OVERDUE,
    PAID
}

class TuitionViewModel(
    application: Application
) : AndroidViewModel(application) {

    val repository: TuitionRepository = TuitionRepository(
        database = TuitionDeskDatabase.getDatabase(application)
    )

    private val _currentTab = MutableStateFlow(TuitionTab.FEES)
    val currentTab: StateFlow<TuitionTab> = _currentTab.asStateFlow()

    private val _feeFilter = MutableStateFlow(FeeFilter.ALL)
    val feeFilter: StateFlow<FeeFilter> = _feeFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Dialog & bottom sheet states
    private val _showAllReminderDialog = MutableStateFlow(false)
    val showAllReminderDialog: StateFlow<Boolean> = _showAllReminderDialog.asStateFlow()

    private val _showAbsentAlertDialog = MutableStateFlow(false)
    val showAbsentAlertDialog: StateFlow<Boolean> = _showAbsentAlertDialog.asStateFlow()

    private val _absentAlertList = MutableStateFlow<List<AttendanceEntry>>(emptyList())
    val absentAlertList: StateFlow<List<AttendanceEntry>> = _absentAlertList.asStateFlow()

    private val _showAddLeadDialog = MutableStateFlow(false)
    val showAddLeadDialog: StateFlow<Boolean> = _showAddLeadDialog.asStateFlow()

    private val _showPitchGuideSheet = MutableStateFlow(false)
    val showPitchGuideSheet: StateFlow<Boolean> = _showPitchGuideSheet.asStateFlow()

    private val _showExportShareDialog = MutableStateFlow(false)
    val showExportShareDialog: StateFlow<Boolean> = _showExportShareDialog.asStateFlow()

    private val _showWhatsAppApiSettings = MutableStateFlow(false)
    val showWhatsAppApiSettings: StateFlow<Boolean> = _showWhatsAppApiSettings.asStateFlow()

    // Data streams from repository
    val fees = repository.fees
    val attendance = repository.attendance
    val selectedBatch = repository.selectedBatch
    val selectedStudentId = repository.selectedStudentId
    val testScores = repository.testScores
    val leaderboard = repository.leaderboard
    val homeworks = repository.homeworks
    val enquiries = repository.enquiries
    val batches = repository.batches
    val centreInfo = repository.centreInfo

    // Derived states
    val filteredFees = combine(fees, feeFilter, searchQuery) { list, filter, query ->
        list.filter { fee ->
            val matchesFilter = when (filter) {
                FeeFilter.ALL -> true
                FeeFilter.OVERDUE -> !fee.isPaid && fee.daysLate > 0
                FeeFilter.PAID -> fee.isPaid
            }
            val matchesQuery = query.isBlank() ||
                    fee.studentName.contains(query, ignoreCase = true) ||
                    fee.rollNo.contains(query, ignoreCase = true) ||
                    fee.batchName.contains(query, ignoreCase = true)
            matchesFilter && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalPendingAmount = fees.map { list ->
        list.filter { !it.isPaid }.sumOf { it.amountDue }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalCollectedAmount = fees.map { list ->
        list.filter { it.isPaid }.sumOf { it.amountDue }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val overdueCount = fees.map { list ->
        list.count { !it.isPaid && it.daysLate > 0 }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val currentBatchAttendance = combine(attendance, selectedBatch) { list, batch ->
        list.filter { it.batchName == batch }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedStudentFee = combine(fees, selectedStudentId) { list, studentId ->
        list.find { it.studentId == studentId } ?: list.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun setTab(tab: TuitionTab) {
        _currentTab.value = tab
    }

    fun setFeeFilter(filter: FeeFilter) {
        _feeFilter.value = filter
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectBatch(batch: String) {
        repository.selectBatch(batch)
    }

    fun selectStudent(studentId: String) {
        repository.selectStudent(studentId)
    }

    fun toggleAttendance(studentId: String) {
        repository.toggleAttendance(studentId)
    }

    fun markAllPresent() {
        repository.markAllPresentForSelectedBatch()
    }

    fun markFeePaid(feeId: String, mode: String = "UPI") {
        repository.markFeePaid(feeId, mode)
    }

    fun toggleHomework(homeworkId: String) {
        repository.toggleHomework(homeworkId)
    }

    fun updateEnquiryStatus(enquiryId: String, status: LeadStatus) {
        repository.updateEnquiryStatus(enquiryId, status)
    }

    fun addEnquiry(studentName: String, gradeClass: String, parentName: String, parentPhone: String, subject: String, referredBy: String) {
        repository.addEnquiry(studentName, gradeClass, parentName, parentPhone, subject, referredBy)
        _showAddLeadDialog.value = false
    }

    fun setShowAllReminderDialog(show: Boolean) {
        _showAllReminderDialog.value = show
    }

    fun setShowAbsentAlertDialog(show: Boolean) {
        _showAbsentAlertDialog.value = show
    }

    fun setShowAddLeadDialog(show: Boolean) {
        _showAddLeadDialog.value = show
    }

    fun setShowPitchGuideSheet(show: Boolean) {
        _showPitchGuideSheet.value = show
    }

    fun setShowExportShareDialog(show: Boolean) {
        _showExportShareDialog.value = show
    }

    fun setShowWhatsAppApiSettings(show: Boolean) {
        _showWhatsAppApiSettings.value = show
    }

    // Communication triggers
    fun onSendSingleWhatsAppReminder(context: Context, fee: FeeRecord) {
        repository.sendSingleWhatsAppFeeReminder(context, fee)
    }

    fun onSendAllPendingFeesWhatsApp(context: Context) {
        repository.sendAllPendingFeesWhatsApp(context)
        _showAllReminderDialog.value = false
    }

    fun onSaveAttendanceClicked(context: Context) {
        val absentees = repository.attendance.value.filter {
            it.batchName == selectedBatch.value && !it.isPresent
        }
        if (absentees.isEmpty()) {
            repository.saveAttendanceAndSendAbsentAlerts(context, selectedBatch.value)
        } else {
            _absentAlertList.value = absentees
            _showAbsentAlertDialog.value = true
        }
    }

    fun onConfirmSendAbsentAlerts(context: Context) {
        repository.saveAttendanceAndSendAbsentAlerts(context, selectedBatch.value)
        _showAbsentAlertDialog.value = false
    }

    fun onSendSingleAbsentWhatsApp(context: Context, entry: AttendanceEntry) {
        repository.sendSingleAbsentWhatsApp(context, entry)
    }

    fun onShareTestReportWhatsApp(context: Context, test: TestScoreItem) {
        repository.shareTestReportWhatsApp(context, test)
    }

    fun onPayByUpi(context: Context, studentName: String, amount: Int) {
        repository.launchUpiPayment(context, studentName, amount)
    }
}
