package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.*
import com.example.util.ReportExportHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ReportExportHelperTest {

    @Test
    fun testGenerateFeesCsv() {
        val fees = listOf(
            FeeRecord(
                id = "F01",
                studentId = "S01",
                studentName = "Aarav Sharma",
                rollNo = "10A-04",
                batchName = "Class 10 - Maths",
                amountDue = 2500,
                dueDate = "10 Oct 2026",
                daysLate = 12,
                isPaid = false,
                parentPhone = "+91 98234 56789"
            ),
            FeeRecord(
                id = "F05",
                studentId = "S05",
                studentName = "Priya Patel",
                rollNo = "10A-01",
                batchName = "Class 10 - Maths",
                amountDue = 2500,
                dueDate = "10 Oct 2026",
                daysLate = 0,
                isPaid = true,
                paidDate = "08 Oct 2026",
                paymentMode = "UPI",
                parentPhone = "+91 99887 76655"
            )
        )

        val csv = ReportExportHelper.generateFeesCsv(fees)
        assertTrue(csv.contains("Student ID,Student Name,Roll No"))
        assertTrue(csv.contains("Aarav Sharma"))
        assertTrue(csv.contains("OVERDUE"))
        assertTrue(csv.contains("Priya Patel"))
        assertTrue(csv.contains("PAID"))
        assertTrue(csv.contains("UPI"))
    }

    @Test
    fun testGenerateAttendanceCsv() {
        val entries = listOf(
            AttendanceEntry("S01", "Aarav Sharma", "10A-04", "B1", "Class 10", "+91 9823456789", "Mr. Manoj", true),
            AttendanceEntry("S02", "Rohan Verma", "10A-11", "B1", "Class 10", "+91 9712345670", "Mrs. Sunita", false)
        )

        val csv = ReportExportHelper.generateAttendanceCsv(entries, "22 Oct 2026")
        assertTrue(csv.contains("Date,Batch Name,Roll No,Student Name,Attendance Status"))
        assertTrue(csv.contains("Aarav Sharma"))
        assertTrue(csv.contains("PRESENT"))
        assertTrue(csv.contains("Rohan Verma"))
        assertTrue(csv.contains("ABSENT"))
    }

    @Test
    fun testGenerateFeeReceiptText() {
        val centre = TuitionCentre(
            name = "Vidya Tutorials",
            tagline = "CBSE Excellence",
            ownerName = "Er. Rajesh Sharma",
            upiId = "vidya@upi"
        )
        val fee = FeeRecord(
            id = "F05",
            studentId = "S05",
            studentName = "Priya Patel",
            rollNo = "10A-01",
            batchName = "Class 10 - Maths",
            amountDue = 2500,
            dueDate = "10 Oct 2026",
            daysLate = 0,
            isPaid = true,
            paidDate = "08 Oct 2026",
            paymentMode = "UPI",
            parentPhone = "+91 99887 76655"
        )

        val receipt = ReportExportHelper.generateFeeReceiptText(centre, fee)
        assertTrue(receipt.contains("TUITIONDESK OFFICIAL FEE RECEIPT"))
        assertTrue(receipt.contains("Vidya Tutorials"))
        assertTrue(receipt.contains("Priya Patel"))
        assertTrue(receipt.contains("₹2500"))
        assertTrue(receipt.contains("CLEARED & CONFIRMED"))
        assertTrue(receipt.contains("vidya@upi"))
    }

    @Test
    fun testSaveContentToFileAndGetUri() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val filename = "test_receipt.txt"
        val content = "Test receipt content"

        val file = ReportExportHelper.saveContentToFile(context, filename, content)
        assertTrue(file.exists())
        assertEquals(content, file.readText())

        val uri = ReportExportHelper.getUriForFile(context, file)
        assertNotNull(uri)
        assertTrue(uri.toString().startsWith("content://"))
    }
}
