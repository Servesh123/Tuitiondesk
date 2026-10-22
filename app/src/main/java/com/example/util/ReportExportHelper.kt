package com.example.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.model.*
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * Utility helper to generate, download to device, and share
 * CSV reports, digital fee receipts, attendance rosters, and scorecards.
 */
object ReportExportHelper {

    /**
     * Generates a CSV string of fee records.
     */
    fun generateFeesCsv(fees: List<FeeRecord>): String {
        val sb = StringBuilder()
        sb.append("Student ID,Student Name,Roll No,Batch,Amount Due,Due Date,Days Overdue,Status,Payment Mode,Paid Date,Parent Phone\n")
        fees.forEach { fee ->
            val status = if (fee.isPaid) "PAID" else "OVERDUE"
            val mode = fee.paymentMode ?: "N/A"
            val paidDate = fee.paidDate ?: "N/A"
            sb.append("\"${fee.studentId}\",\"${fee.studentName}\",\"${fee.rollNo}\",\"${fee.batchName}\",${fee.amountDue},\"${fee.dueDate}\",${fee.daysLate},\"$status\",\"$mode\",\"$paidDate\",\"${fee.parentPhone}\"\n")
        }
        return sb.toString()
    }

    /**
     * Generates a CSV string of attendance logs.
     */
    fun generateAttendanceCsv(entries: List<AttendanceEntry>, date: String = "22 Oct 2026"): String {
        val sb = StringBuilder()
        sb.append("Date,Batch Name,Roll No,Student Name,Attendance Status,Parent Name,Parent Phone\n")
        entries.forEach { entry ->
            val status = if (entry.isPresent) "PRESENT" else "ABSENT"
            sb.append("\"$date\",\"${entry.batchName}\",\"${entry.rollNo}\",\"${entry.studentName}\",\"$status\",\"${entry.parentName}\",\"${entry.parentPhone}\"\n")
        }
        return sb.toString()
    }

    /**
     * Generates a CSV string of weekly test rankings.
     */
    fun generateTestLeaderboardCsv(test: TestScoreItem, leaderboard: List<StudentRank>): String {
        val sb = StringBuilder()
        sb.append("Test Title: ${test.title}, Subject: ${test.subject}, Date: ${test.date}, Max Marks: ${test.maxMarks}\n")
        sb.append("Rank,Student Name,Roll No,Score,Max Marks,Percentage\n")
        leaderboard.forEach { rank ->
            sb.append("${rank.rank},\"${rank.studentName}\",\"${rank.rollNo}\",${rank.score},${rank.maxMarks},${rank.percentage}%\n")
        }
        return sb.toString()
    }

    /**
     * Generates an official formatted digital fee payment receipt text.
     */
    fun generateFeeReceiptText(centre: TuitionCentre, fee: FeeRecord): String {
        val receiptId = "TD-${System.currentTimeMillis() % 1000000}"
        val paymentDate = fee.paidDate ?: "22 Oct 2026"
        val mode = fee.paymentMode ?: "UPI"
        return """
            ==================================================
                      TUITIONDESK OFFICIAL FEE RECEIPT
                   ${centre.name}
                   ${centre.tagline}
            ==================================================
            Receipt No   : $receiptId
            Payment Date : $paymentDate
            Tuition Month: ${fee.month}
            --------------------------------------------------
            STUDENT DETAILS:
            Name         : ${fee.studentName}
            Roll No      : ${fee.rollNo}
            Batch        : ${fee.batchName}
            Parent Phone : ${fee.parentPhone}
            --------------------------------------------------
            FEE PARTICULARS:
            Tuition Fee  : ₹${fee.amountDue}
            Late Fee     : ₹0 (Waived)
            Total Paid   : ₹${fee.amountDue}
            Payment Mode : $mode
            Payment Status: CLEARED & CONFIRMED ✅
            --------------------------------------------------
            INSTITUTE DETAILS:
            Director     : ${centre.ownerName}
            UPI ID       : ${centre.upiId}
            Helpline     : ${centre.phone}
            Location     : ${centre.location}
            --------------------------------------------------
            Thank you for timely payment! Learning uninterrupted.
            ==================================================
        """.trimIndent()
    }

    /**
     * Saves a content string to a local cache file prepared for FileProvider sharing.
     */
    fun saveContentToFile(context: Context, filename: String, content: String): File {
        val reportsDir = File(context.cacheDir, "reports")
        if (!reportsDir.exists()) {
            reportsDir.mkdirs()
        }
        val file = File(reportsDir, filename)
        FileOutputStream(file).use { out ->
            out.write(content.toByteArray(Charsets.UTF_8))
        }
        return file
    }

    /**
     * Downloads/exports a report to the device public Downloads directory
     * and shows a toast confirmation.
     */
    fun downloadToDevice(context: Context, filename: String, content: String, mimeType: String = "text/csv"): Uri? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/TuitionDesk")
                }
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { stream ->
                        stream.write(content.toByteArray(Charsets.UTF_8))
                    }
                    Toast.makeText(context, "📥 Downloaded: $filename (Saved in Downloads/TuitionDesk)", Toast.LENGTH_LONG).show()
                    uri
                } else {
                    // Fallback to internal cache file
                    val file = saveContentToFile(context, filename, content)
                    Toast.makeText(context, "📥 File ready: $filename", Toast.LENGTH_SHORT).show()
                    getUriForFile(context, file)
                }
            } else {
                val file = saveContentToFile(context, filename, content)
                Toast.makeText(context, "📥 Downloaded: $filename", Toast.LENGTH_SHORT).show()
                getUriForFile(context, file)
            }
        } catch (e: Exception) {
            val file = saveContentToFile(context, filename, content)
            Toast.makeText(context, "📥 Saved: $filename", Toast.LENGTH_SHORT).show()
            getUriForFile(context, file)
        }
    }

    /**
     * Obtains a secure content:// URI via FileProvider.
     */
    fun getUriForFile(context: Context, file: File): Uri {
        val authority = "${context.packageName}.fileprovider"
        return FileProvider.getUriForFile(context, authority, file)
    }

    /**
     * Shares a generated file using the Android system share sheet.
     */
    fun shareFile(context: Context, file: File, mimeType: String, chooserTitle: String = "Share Report via") {
        try {
            val contentUri = getUriForFile(context, file)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_SUBJECT, file.nameWithoutExtension)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(shareIntent, chooserTitle).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open share chooser: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Opens / views a file in an external viewer app (e.g., CSV reader, Text viewer).
     */
    fun openFile(context: Context, file: File, mimeType: String) {
        try {
            val contentUri = getUriForFile(context, file)
            val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(contentUri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(viewIntent, "Open with").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "No app available to open this file", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * In-App action: Share the TuitionDesk App with other teachers or owners.
     */
    fun shareTuitionDeskApp(context: Context) {
        val shareText = """
            📚 *TuitionDesk - Coaching & Tuition Management App*
            
            Key Features:
            • 1-Click WhatsApp Fee Reminders with UPI Payment links
            • Daily Attendance Register with instant Absent Parent Alerts
            • Weekly Test Scorecards & 6-Week Progress Bar Chart
            • Parent Portal View with Homework & Instant Fee Receipts
            • Owner Revenue Analytics (Collected vs Pending Fees)
            
            Check it out to streamline tuition administration!
        """.trimIndent()

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
            putExtra(Intent.EXTRA_SUBJECT, "TuitionDesk Coaching Management")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val chooser = Intent.createChooser(shareIntent, "Share TuitionDesk App via").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "Share sheet unavailable", Toast.LENGTH_SHORT).show()
        }
    }
}
