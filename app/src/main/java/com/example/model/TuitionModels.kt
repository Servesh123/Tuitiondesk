package com.example.model

data class Student(
    val id: String,
    val name: String,
    val rollNo: String,
    val batchId: String,
    val batchName: String,
    val parentName: String,
    val parentPhone: String,
    val attendancePct: Int,
    val pendingFee: Int,
    val avatarColorHex: Long = 0xFF2563EB
)

data class FeeRecord(
    val id: String,
    val studentId: String,
    val studentName: String,
    val rollNo: String,
    val batchName: String,
    val amountDue: Int,
    val dueDate: String,
    val daysLate: Int,
    val isPaid: Boolean,
    val paidDate: String? = null,
    val paymentMode: String? = null,
    val parentPhone: String,
    val month: String = "October 2026"
)

data class AttendanceEntry(
    val studentId: String,
    val studentName: String,
    val rollNo: String,
    val batchId: String,
    val batchName: String,
    val parentPhone: String,
    val parentName: String,
    val isPresent: Boolean
)

data class TestScoreItem(
    val testNumber: Int,
    val title: String,
    val subject: String,
    val date: String,
    val studentScore: Int,
    val maxMarks: Int,
    val batchAverage: Int,
    val highestScore: Int,
    val rank: Int,
    val totalStudents: Int
)

data class StudentRank(
    val rank: Int,
    val studentName: String,
    val rollNo: String,
    val score: Int,
    val maxMarks: Int,
    val percentage: Int
)

data class HomeworkItem(
    val id: String,
    val studentId: String,
    val subject: String,
    val title: String,
    val dueDate: String,
    val isCompleted: Boolean,
    val teacherNote: String
)

enum class LeadStatus(val label: String) {
    NEW("New Lead"),
    DEMO_BOOKED("Demo Class Booked"),
    FOLLOW_UP("Follow Up Needed"),
    ENROLLED("Enrolled")
}

data class ReferralEnquiry(
    val id: String,
    val studentName: String,
    val gradeClass: String,
    val parentName: String,
    val parentPhone: String,
    val subject: String,
    val status: LeadStatus,
    val date: String,
    val referredBy: String,
    val notes: String
)

data class TuitionCentre(
    val name: String = "Vidya Tutorials & Coaching",
    val tagline: String = "CBSE & ICSE Excellence Hub",
    val ownerName: String = "Er. Rajesh Sharma (Director)",
    val phone: String = "+91 98765 43210",
    val upiId: String = "vidyatutorials@okhdfcbank",
    val location: String = "Sector 14, Main Market, Delhi NCR"
)
