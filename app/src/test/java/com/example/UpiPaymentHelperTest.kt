package com.example

import android.content.Intent
import com.example.model.FeeRecord
import com.example.model.TuitionCentre
import com.example.util.UpiPaymentHelper
import com.example.util.UpiPaymentRequest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class UpiPaymentHelperTest {

    @Test
    fun testGenerateUpiUrl() {
        val request = UpiPaymentRequest(
            payeeVpa = "vidyatutorials@okhdfcbank",
            payeeName = "Vidya Tutorials & Coaching",
            amount = 2500.0,
            transactionNote = "Tuition Fee October - Aarav Sharma",
            transactionRefId = "TXN998877",
            merchantCode = "8299",
            currency = "INR"
        )

        val url = UpiPaymentHelper.generateUpiUrl(request)

        assertTrue(url.startsWith("upi://pay?"))
        assertTrue(url.contains("pa=vidyatutorials@okhdfcbank"))
        assertTrue(url.contains("pn=Vidya%20Tutorials%20%26%20Coaching"))
        assertTrue(url.contains("am=2500.00"))
        assertTrue(url.contains("cu=INR"))
        assertTrue(url.contains("tr=TXN998877"))
        assertTrue(url.contains("mc=8299"))
        assertTrue(url.contains("tn=Tuition%20Fee%20October%20-%20Aarav%20Sharma"))
    }

    @Test
    fun testGenerateUpiUrlForFee() {
        val centre = TuitionCentre(
            name = "Vidya Tutorials",
            upiId = "coaching@upi"
        )
        val fee = FeeRecord(
            id = "F01",
            studentId = "S01",
            studentName = "Rohan Verma",
            rollNo = "10A-11",
            batchName = "Class 10 - Maths",
            amountDue = 3200,
            dueDate = "15 Oct 2026",
            daysLate = 7,
            isPaid = false,
            parentPhone = "+91 97123 45670",
            month = "October 2026"
        )

        val url = UpiPaymentHelper.generateUpiUrlForFee(centre, fee, "REF12345")

        assertTrue(url.startsWith("upi://pay?"))
        assertTrue(url.contains("pa=coaching@upi"))
        assertTrue(url.contains("am=3200.00"))
        assertTrue(url.contains("tr=REF12345"))
        assertTrue(url.contains("Rohan%20Verma"))
    }

    @Test
    fun testCreateUpiIntent() {
        val request = UpiPaymentRequest(
            payeeVpa = "teacher@upi",
            payeeName = "Sharma Sir",
            amount = 1500.0,
            transactionNote = "Physics Class 12 Fee"
        )

        val intent = UpiPaymentHelper.createUpiIntent(request)

        assertEquals(Intent.ACTION_VIEW, intent.action)
        assertNotNull(intent.data)
        assertEquals("upi", intent.data?.scheme)
        assertEquals("pay", intent.data?.host)
        assertEquals("teacher@upi", intent.data?.getQueryParameter("pa"))
        assertEquals("1500.00", intent.data?.getQueryParameter("am"))
        assertEquals("INR", intent.data?.getQueryParameter("cu"))
    }

    @Test
    fun testCreateTargetedUpiIntent() {
        val request = UpiPaymentRequest(
            payeeVpa = "teacher@upi",
            payeeName = "Sharma Sir",
            amount = 1500.0,
            transactionNote = "Maths Fee"
        )

        val intent = UpiPaymentHelper.createUpiIntent(request, "com.google.android.apps.nbu.paisa.user")

        assertEquals("com.google.android.apps.nbu.paisa.user", intent.`package`)
        assertEquals("upi", intent.data?.scheme)
    }
}
