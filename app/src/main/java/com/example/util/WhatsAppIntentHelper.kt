package com.example.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.model.AttendanceEntry
import com.example.model.FeeRecord
import com.example.model.TestScoreItem
import com.example.model.TuitionCentre

/**
 * Utility helper to construct and launch WhatsApp Intent URLs and deep links
 * for automated tuition fees reminders, daily attendance absence alerts,
 * and weekly test scorecards.
 */
object WhatsAppIntentHelper {

    private const val DEFAULT_COUNTRY_CODE = "91" // India

    /**
     * Sanitizes and normalizes phone numbers into international E.164 digits format.
     * E.g., "+91 98765-43210" -> "919876543210", "9876543210" -> "919876543210".
     */
    fun cleanAndFormatPhoneNumber(rawPhone: String, defaultCountryCode: String = DEFAULT_COUNTRY_CODE): String {
        val digitsOnly = rawPhone.replace(Regex("[^0-9]"), "")
        return when {
            digitsOnly.length == 10 -> "$defaultCountryCode$digitsOnly"
            digitsOnly.length == 11 && digitsOnly.startsWith("0") -> "$defaultCountryCode${digitsOnly.substring(1)}"
            digitsOnly.length == 12 && digitsOnly.startsWith(defaultCountryCode) -> digitsOnly
            else -> digitsOnly
        }
    }

    /**
     * Constructs the official standard WhatsApp click-to-chat web URL.
     * Format: https://api.whatsapp.com/send?phone=<phone>&text=<encoded_text>
     */
    fun constructWhatsAppUrl(phoneNumber: String, message: String): Uri {
        val formattedPhone = cleanAndFormatPhoneNumber(phoneNumber)
        val encodedMessage = Uri.encode(message)
        val urlString = if (formattedPhone.isNotEmpty()) {
            "https://api.whatsapp.com/send?phone=$formattedPhone&text=$encodedMessage"
        } else {
            "https://api.whatsapp.com/send?text=$encodedMessage"
        }
        return Uri.parse(urlString)
    }

    /**
     * Constructs the direct WhatsApp custom URI scheme.
     * Format: whatsapp://send?phone=<phone>&text=<encoded_text>
     */
    fun constructWhatsAppCustomScheme(phoneNumber: String, message: String): Uri {
        val formattedPhone = cleanAndFormatPhoneNumber(phoneNumber)
        val encodedMessage = Uri.encode(message)
        val urlString = if (formattedPhone.isNotEmpty()) {
            "whatsapp://send?phone=$formattedPhone&text=$encodedMessage"
        } else {
            "whatsapp://send?text=$encodedMessage"
        }
        return Uri.parse(urlString)
    }

    /**
     * Constructs an Intent targeting WhatsApp specifically, using both official
     * https API endpoints and custom scheme fallbacks.
     */
    fun createWhatsAppIntent(phoneNumber: String, message: String): Intent {
        val uri = constructWhatsAppUrl(phoneNumber, message)
        return Intent(Intent.ACTION_VIEW, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    /**
     * Main launcher utility function:
     * 1. Attempts to open WhatsApp directly using the deep-link URL.
     * 2. Falls back to Android system chooser (Share sheet) if WhatsApp is not directly available.
     * 3. Falls back to copying message to clipboard with a clear user notice if all fails.
     */
    fun launchWhatsAppMessage(
        context: Context,
        phoneNumber: String,
        message: String,
        chooserTitle: String = "Send via WhatsApp"
    ) {
        val whatsappIntent = createWhatsAppIntent(phoneNumber, message)

        try {
            // Check if there is an activity to handle the intent
            context.startActivity(whatsappIntent)
        } catch (e: Exception) {
            // Fallback 1: Try custom scheme whatsapp://
            try {
                val customSchemeIntent = Intent(Intent.ACTION_VIEW, constructWhatsAppCustomScheme(phoneNumber, message)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(customSchemeIntent)
            } catch (e2: Exception) {
                // Fallback 2: Launch system share sheet so user can select any messaging app
                launchFallbackShareSheet(context, message, chooserTitle)
            }
        }
    }

    /**
     * Launches a general plain-text share chooser (used for batch broadcasts to multiple parents).
     */
    fun launchFallbackShareSheet(context: Context, message: String, title: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val chooser = Intent.createChooser(shareIntent, title).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(chooser)
        } catch (e: Exception) {
            // Fallback 3: Copy to clipboard
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("TuitionDesk Message", message)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "Message copied to clipboard! Paste in WhatsApp.", Toast.LENGTH_LONG).show()
        }
    }

    // -------------------------------------------------------------------------
    // Automated Message Template Builders
    // -------------------------------------------------------------------------

    /**
     * Builds a polite, professional fee reminder message for a single student's parent,
     * including exact dues, days late, UPI ID, and direct payment link.
     */
    fun buildFeeReminderMessage(
        centre: TuitionCentre,
        fee: FeeRecord
    ): String {
        return """
            Namaste ${fee.studentName}'s Parent 🙏,
            This is a friendly reminder from *${centre.name}*.
            
            Fee for *${fee.month}* of *₹${fee.amountDue}* was due on ${fee.dueDate} (${fee.daysLate} days overdue).
            
            Kindly clear the dues via UPI to keep the learning uninterrupted:
            📲 Coaching UPI ID: *${centre.upiId}*
            
            Or click to pay directly:
            https://upiqr.in/pay?pa=${centre.upiId}&pn=${Uri.encode(centre.name)}&am=${fee.amountDue}
            
            Thank you,
            ${centre.ownerName}
            📞 ${centre.phone}
        """.trimIndent()
    }

    /**
     * Builds a batch broadcast fee reminder summary for all pending parents.
     */
    fun buildBatchFeeReminderMessage(
        centre: TuitionCentre,
        overdueFees: List<FeeRecord>
    ): String {
        val totalPending = overdueFees.sumOf { it.amountDue }
        return """
            📢 *Fee Reminder Notice - ${centre.name}*
            Date: 22 Oct 2026
            
            Total Overdue Students: ${overdueFees.size}
            Total Outstanding Amount: ₹$totalPending
            
            Dear Parents,
            Monthly coaching fees for October 2026 are due. Kindly settle the dues using the details below:
            
            📲 UPI ID: *${centre.upiId}*
            🏦 Payee: *${centre.name}*
            
            Please share the transaction screenshot after payment for the official digital receipt.
            
            Regards,
            ${centre.ownerName}
            📞 ${centre.phone}
        """.trimIndent()
    }

    /**
     * Builds an attendance absence alert message for an absent student's parent.
     */
    fun buildAbsentAlertMessage(
        centre: TuitionCentre,
        entry: AttendanceEntry,
        date: String = "Today (22 Oct 2026)"
    ): String {
        return """
            ⚠️ *Absence Alert - ${centre.name}*
            
            Namaste ${entry.parentName} Ji 🙏,
            
            Your ward *${entry.studentName}* (Roll: ${entry.rollNo}) was marked *ABSENT* in today's class ($date) in *${entry.batchName}*.
            
            Regularity in coaching batches is vital for consistent performance in Board and school examinations.
            
            If this absence was due to illness or pre-informed leave, kindly reply to confirm.
            
            Regards,
            ${centre.ownerName}
            📞 ${centre.phone}
        """.trimIndent()
    }

    /**
     * Builds a batch absent broadcast report for a specific batch.
     */
    fun buildBatchAbsentAlertMessage(
        centre: TuitionCentre,
        absentees: List<AttendanceEntry>,
        batchName: String,
        date: String = "22 Oct 2026"
    ): String {
        val names = absentees.joinToString(", ") { "${it.studentName} (${it.rollNo})" }
        return """
            ⚠️ *Daily Batch Absence Report - ${centre.name}*
            Batch: *$batchName*
            Date: $date
            
            The following students were marked absent today:
            $names
            
            Parents are requested to ensure regular attendance.
            
            Regards,
            ${centre.ownerName}
            📞 ${centre.phone}
        """.trimIndent()
    }

    /**
     * Builds a comprehensive weekly test scorecard message to share with parents.
     */
    fun buildTestReportMessage(
        centre: TuitionCentre,
        test: TestScoreItem
    ): String {
        val percentage = (test.studentScore * 100) / test.maxMarks
        return """
            📊 *Weekly Test Scorecard - ${centre.name}*
            Test: *${test.title}*
            Subject: *${test.subject}*
            Date: ${test.date}
            
            🌟 *Performance Summary:*
            • Student Score: *${test.studentScore}/${test.maxMarks}* ($percentage%)
            • Batch Rank: *#${test.rank}* out of ${test.totalStudents} students
            • Batch Average: ${test.batchAverage}/${test.maxMarks}
            • Highest in Batch: ${test.highestScore}/${test.maxMarks}
            
            Keep striving for excellence in upcoming exams! 🎯
            
            ${centre.ownerName}
            ${centre.phone}
        """.trimIndent()
    }

    // -------------------------------------------------------------------------
    // Convenient All-in-One Dispatchers
    // -------------------------------------------------------------------------

    fun sendFeeReminder(context: Context, fee: FeeRecord, centre: TuitionCentre) {
        val message = buildFeeReminderMessage(centre, fee)
        launchWhatsAppMessage(context, fee.parentPhone, message, "Send Fee Reminder to ${fee.studentName}'s Parent")
    }

    fun sendBatchFeeReminders(context: Context, overdueFees: List<FeeRecord>, centre: TuitionCentre) {
        val message = buildBatchFeeReminderMessage(centre, overdueFees)
        launchFallbackShareSheet(context, message, "Broadcast Fee Reminder to All Parents")
    }

    fun sendAbsentAlert(context: Context, entry: AttendanceEntry, centre: TuitionCentre) {
        val message = buildAbsentAlertMessage(centre, entry)
        launchWhatsAppMessage(context, entry.parentPhone, message, "Send Absent Alert to ${entry.parentName}")
    }

    fun sendBatchAbsentAlerts(context: Context, absentees: List<AttendanceEntry>, batchName: String, centre: TuitionCentre) {
        val message = buildBatchAbsentAlertMessage(centre, absentees, batchName)
        launchFallbackShareSheet(context, message, "Send Absent Alert to ${absentees.size} Parents")
    }

    fun sendTestScorecard(context: Context, test: TestScoreItem, centre: TuitionCentre) {
        val message = buildTestReportMessage(centre, test)
        launchFallbackShareSheet(context, message, "Share ${test.title} Scorecard on WhatsApp")
    }
}
