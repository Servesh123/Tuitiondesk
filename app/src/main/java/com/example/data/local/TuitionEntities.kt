package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.model.AttendanceEntry
import com.example.model.FeeRecord
import com.example.model.Student

/**
 * Room Entity representing enrolled students in the tuition centre.
 */
@Entity(
    tableName = "students",
    indices = [Index(value = ["rollNo"], unique = false), Index(value = ["batchId"])]
)
data class StudentEntity(
    @PrimaryKey val id: String,
    val name: String,
    val rollNo: String,
    val batchId: String,
    val batchName: String,
    val parentName: String,
    val parentPhone: String,
    val attendancePct: Int,
    val pendingFee: Int,
    val avatarColorHex: Long = 0xFF2563EB
) {
    fun toDomain(): Student = Student(
        id = id,
        name = name,
        rollNo = rollNo,
        batchId = batchId,
        batchName = batchName,
        parentName = parentName,
        parentPhone = parentPhone,
        attendancePct = attendancePct,
        pendingFee = pendingFee,
        avatarColorHex = avatarColorHex
    )

    companion object {
        fun fromDomain(student: Student): StudentEntity = StudentEntity(
            id = student.id,
            name = student.name,
            rollNo = student.rollNo,
            batchId = student.batchId,
            batchName = student.batchName,
            parentName = student.parentName,
            parentPhone = student.parentPhone,
            attendancePct = student.attendancePct,
            pendingFee = student.pendingFee,
            avatarColorHex = student.avatarColorHex
        )
    }
}

/**
 * Room Entity representing monthly fee records and payment statuses.
 */
@Entity(
    tableName = "fee_records",
    indices = [Index(value = ["studentId"]), Index(value = ["isPaid"])]
)
data class FeeEntity(
    @PrimaryKey val id: String,
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
) {
    fun toDomain(): FeeRecord = FeeRecord(
        id = id,
        studentId = studentId,
        studentName = studentName,
        rollNo = rollNo,
        batchName = batchName,
        amountDue = amountDue,
        dueDate = dueDate,
        daysLate = daysLate,
        isPaid = isPaid,
        paidDate = paidDate,
        paymentMode = paymentMode,
        parentPhone = parentPhone,
        month = month
    )

    companion object {
        fun fromDomain(record: FeeRecord): FeeEntity = FeeEntity(
            id = record.id,
            studentId = record.studentId,
            studentName = record.studentName,
            rollNo = record.rollNo,
            batchName = record.batchName,
            amountDue = record.amountDue,
            dueDate = record.dueDate,
            daysLate = record.daysLate,
            isPaid = record.isPaid,
            paidDate = record.paidDate,
            paymentMode = record.paymentMode,
            parentPhone = record.parentPhone,
            month = record.month
        )
    }
}

/**
 * Room Entity representing daily attendance logs with timestamps and statuses.
 */
@Entity(
    tableName = "attendance_logs",
    indices = [
        Index(value = ["studentId", "date"], unique = true),
        Index(value = ["batchName", "date"])
    ]
)
data class AttendanceLogEntity(
    @PrimaryKey(autoGenerate = true) val logId: Long = 0L,
    val studentId: String,
    val studentName: String,
    val rollNo: String,
    val batchId: String,
    val batchName: String,
    val parentPhone: String,
    val parentName: String,
    val isPresent: Boolean,
    val date: String = "22 Oct 2026",
    val recordedTimestamp: Long = System.currentTimeMillis()
) {
    fun toDomain(): AttendanceEntry = AttendanceEntry(
        studentId = studentId,
        studentName = studentName,
        rollNo = rollNo,
        batchId = batchId,
        batchName = batchName,
        parentPhone = parentPhone,
        parentName = parentName,
        isPresent = isPresent
    )

    companion object {
        fun fromDomain(entry: AttendanceEntry, date: String = "22 Oct 2026"): AttendanceLogEntity = AttendanceLogEntity(
            studentId = entry.studentId,
            studentName = entry.studentName,
            rollNo = entry.rollNo,
            batchId = entry.batchId,
            batchName = entry.batchName,
            parentPhone = entry.parentPhone,
            parentName = entry.parentName,
            isPresent = entry.isPresent,
            date = date
        )
    }
}
