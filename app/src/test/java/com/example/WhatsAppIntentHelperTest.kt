package com.example

import com.example.model.AttendanceEntry
import com.example.model.FeeRecord
import com.example.model.TuitionCentre
import com.example.util.WhatsAppIntentHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class WhatsAppIntentHelperTest {

    @Test
    fun testPhoneNumberFormatting() {
        // Standard 10-digit Indian mobile
        assertEquals("919876543210", WhatsAppIntentHelper.cleanAndFormatPhoneNumber("9876543210"))
        // Formatted with spaces and plus
        assertEquals("919876543210", WhatsAppIntentHelper.cleanAndFormatPhoneNumber("+91 98765 43210"))
        // Formatted with dashes
        assertEquals("919876543210", WhatsAppIntentHelper.cleanAndFormatPhoneNumber("98765-43210"))
        // With leading 0
        assertEquals("919876543210", WhatsAppIntentHelper.cleanAndFormatPhoneNumber("09876543210"))
    }

    @Test
    fun testConstructWhatsAppUrl() {
        val phone = "+91 98234 56789"
        val message = "Hello Aarav's Parent, fee is due!"
        val uri = WhatsAppIntentHelper.constructWhatsAppUrl(phone, message)

        val uriString = uri.toString()
        assertTrue(uriString.startsWith("https://api.whatsapp.com/send?phone=919823456789&text="))
        assertTrue(uriString.contains("Hello%20Aarav"))
    }

    @Test
    fun testConstructWhatsAppCustomScheme() {
        val phone = "9823456789"
        val message = "Absence notice"
        val uri = WhatsAppIntentHelper.constructWhatsAppCustomScheme(phone, message)

        val uriString = uri.toString()
        assertTrue(uriString.startsWith("whatsapp://send?phone=919823456789&text="))
    }

    @Test
    fun testBuildFeeReminderMessageContent() {
        val centre = TuitionCentre(name = "Sharma Coaching", upiId = "sharma@upi")
        val fee = FeeRecord(
            id = "F01",
            studentId = "S01",
            studentName = "Rohan",
            rollNo = "10-A",
            batchName = "Class 10",
            amountDue = 2500,
            dueDate = "10 Oct",
            daysLate = 5,
            isPaid = false,
            parentPhone = "+91 9876543210"
        )
        val msg = WhatsAppIntentHelper.buildFeeReminderMessage(centre, fee)

        assertTrue(msg.contains("Rohan"))
        assertTrue(msg.contains("₹2500"))
        assertTrue(msg.contains("5 days overdue"))
        assertTrue(msg.contains("sharma@upi"))
    }

    @Test
    fun testBuildAbsentAlertMessageContent() {
        val centre = TuitionCentre(name = "Sharma Coaching")
        val entry = AttendanceEntry(
            studentId = "S01",
            studentName = "Priya",
            rollNo = "10-B",
            batchId = "B1",
            batchName = "Class 10 Maths",
            parentPhone = "+91 9876543210",
            parentName = "Mr. Patel",
            isPresent = false
        )
        val msg = WhatsAppIntentHelper.buildAbsentAlertMessage(centre, entry, "22 Oct 2026")

        assertTrue(msg.contains("Priya"))
        assertTrue(msg.contains("ABSENT"))
        assertTrue(msg.contains("Class 10 Maths"))
        assertTrue(msg.contains("Mr. Patel"))
    }
}
