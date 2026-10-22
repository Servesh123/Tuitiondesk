package com.example.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import com.example.model.FeeRecord
import com.example.model.TuitionCentre
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * Data class representing a structured NPCI-compliant UPI payment request.
 */
data class UpiPaymentRequest(
    val payeeVpa: String,
    val payeeName: String,
    val amount: Double,
    val transactionNote: String,
    val transactionRefId: String = "TXN${System.currentTimeMillis() % 100000000}",
    val merchantCode: String = "8299", // 8299: Educational and coaching services
    val currency: String = "INR"
)

/**
 * Information about a detected UPI app installed on the device.
 */
data class UpiAppInfo(
    val packageName: String,
    val appName: String,
    val isInstalled: Boolean
)

/**
 * Utility helper to construct NPCI-compliant UPI payment intent URLs (upi://pay),
 * detect installed UPI client apps, and launch direct in-app payments.
 */
object UpiPaymentHelper {

    private val amountFormatter = DecimalFormat("0.00", DecimalFormatSymbols(Locale.US))

    /**
     * Known popular UPI apps in India.
     */
    val POPULAR_UPI_PACKAGES = mapOf(
        "com.google.android.apps.nbu.paisa.user" to "Google Pay",
        "com.phonepe.app" to "PhonePe",
        "net.one97.paytm" to "Paytm",
        "in.org.npci.upiapp" to "BHIM",
        "com.cred.app" to "CRED",
        "in.amazon.mShop.android.shopping" to "Amazon Pay"
    )

    /**
     * Generates a standard NPCI-compliant UPI intent URL string.
     * Format: upi://pay?pa=<vpa>&pn=<name>&mc=<mc>&tr=<ref>&tn=<note>&am=<amount>&cu=INR
     */
    fun generateUpiUrl(request: UpiPaymentRequest): String {
        val formattedAmount = amountFormatter.format(request.amount)
        val encodedPayeeName = Uri.encode(request.payeeName)
        val encodedNote = Uri.encode(request.transactionNote)

        return "upi://pay?" +
                "pa=${request.payeeVpa}" +
                "&pn=$encodedPayeeName" +
                "&mc=${request.merchantCode}" +
                "&tr=${request.transactionRefId}" +
                "&tn=$encodedNote" +
                "&am=$formattedAmount" +
                "&cu=${request.currency}"
    }

    /**
     * Converts a UpiPaymentRequest into a parsed Android Uri.
     */
    fun generateUpiUri(request: UpiPaymentRequest): Uri {
        return Uri.parse(generateUpiUrl(request))
    }

    /**
     * Convenience builder to create a UPI payment URL for a student's FeeRecord.
     */
    fun generateUpiUrlForFee(
        centre: TuitionCentre,
        fee: FeeRecord,
        transactionRefId: String? = null
    ): String {
        val ref = transactionRefId ?: "FEE${fee.id}_${System.currentTimeMillis() % 100000}"
        val note = "Tuition Fee ${fee.month} - ${fee.studentName} (${fee.rollNo})"
        val request = UpiPaymentRequest(
            payeeVpa = centre.upiId,
            payeeName = centre.name,
            amount = fee.amountDue.toDouble(),
            transactionNote = note,
            transactionRefId = ref
        )
        return generateUpiUrl(request)
    }

    /**
     * Creates an Android Intent configured with the UPI payment request URI.
     * Can optionally target a specific app package (e.g., Google Pay, PhonePe, Paytm).
     */
    fun createUpiIntent(request: UpiPaymentRequest, targetPackage: String? = null): Intent {
        val upiUri = generateUpiUri(request)
        return Intent(Intent.ACTION_VIEW, upiUri).apply {
            if (!targetPackage.isNullOrBlank()) {
                setPackage(targetPackage)
            }
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    /**
     * Queries the device PackageManager to discover which popular UPI payment apps are available.
     */
    fun getInstalledUpiApps(context: Context): List<UpiAppInfo> {
        val pm = context.packageManager
        return POPULAR_UPI_PACKAGES.map { (pkg, name) ->
            val isInstalled = try {
                pm.getPackageInfo(pkg, 0)
                true
            } catch (e: PackageManager.NameNotFoundException) {
                false
            }
            UpiAppInfo(packageName = pkg, appName = name, isInstalled = isInstalled)
        }
    }

    /**
     * Launches the UPI payment intent:
     * 1. Launches via the system app chooser so parent can pick GPay, PhonePe, Paytm, etc.
     * 2. If no UPI app is installed on the device/emulator, copies the UPI ID to clipboard
     *    and alerts the user with fallback instructions.
     */
    fun launchUpiPayment(
        context: Context,
        request: UpiPaymentRequest,
        onFallback: (() -> Unit)? = null
    ): Boolean {
        val intent = createUpiIntent(request)
        val chooser = Intent.createChooser(intent, "Pay ₹${request.amount.toInt()} via UPI").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        return try {
            context.startActivity(chooser)
            true
        } catch (e: Exception) {
            // Fallback: Copy UPI ID and notify parent
            copyUpiDetailsToClipboard(context, request.payeeVpa, request.amount)
            onFallback?.invoke()
            false
        }
    }

    /**
     * Copies the Coaching VPA / UPI ID and amount to clipboard with user feedback.
     */
    fun copyUpiDetailsToClipboard(context: Context, payeeVpa: String, amount: Double) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("UPI Payment ID", payeeVpa)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(
            context,
            "Coaching UPI ID ($payeeVpa) copied to clipboard! Pay ₹${amount.toInt()} in any UPI app.",
            Toast.LENGTH_LONG
        ).show()
    }
}
