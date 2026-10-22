package com.example.network.whatsapp

import android.util.Log
import com.example.BuildConfig
import com.example.model.AttendanceEntry
import com.example.model.FeeRecord
import com.example.model.TuitionCentre
import com.example.util.WhatsAppIntentHelper
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Singleton client for interacting with Meta WhatsApp Business Cloud API.
 * Automatically dispatches fee payment reminders and daily attendance absence alerts.
 */
object WhatsAppBusinessApiClient {

    private const val TAG = "WhatsAppBusinessApi"
    private const val BASE_URL = "https://graph.facebook.com/v20.0/"

    // In-memory overrides allow users to test API credentials directly from the app
    var customApiToken: String? = null
    var customPhoneNumberId: String? = null

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val apiService: WhatsAppBusinessApiService = retrofit.create(WhatsAppBusinessApiService::class.java)

    /**
     * Resolves the active access token, preferring user-provided overrides,
     * falling back to BuildConfig injected via .env / Secrets panel.
     */
    fun getActiveApiToken(): String {
        val override = customApiToken
        if (!override.isNullOrBlank() && !override.startsWith("YOUR_")) {
            return override.trim()
        }
        val buildConfigToken = try {
            BuildConfig::class.java.getField("WHATSAPP_API_TOKEN").get(null) as? String
        } catch (e: Exception) {
            null
        }
        return if (!buildConfigToken.isNullOrBlank() && !buildConfigToken.startsWith("YOUR_")) {
            buildConfigToken.trim()
        } else {
            ""
        }
    }

    /**
     * Resolves the active WhatsApp Phone Number ID.
     */
    fun getActivePhoneNumberId(): String {
        val override = customPhoneNumberId
        if (!override.isNullOrBlank() && !override.startsWith("YOUR_")) {
            return override.trim()
        }
        val buildConfigPhoneId = try {
            BuildConfig::class.java.getField("WHATSAPP_PHONE_NUMBER_ID").get(null) as? String
        } catch (e: Exception) {
            null
        }
        return if (!buildConfigPhoneId.isNullOrBlank() && !buildConfigPhoneId.startsWith("YOUR_")) {
            buildConfigPhoneId.trim()
        } else {
            ""
        }
    }

    /**
     * Returns true if valid WhatsApp Business Cloud API credentials have been configured.
     */
    fun isApiConfigured(): Boolean {
        val token = getActiveApiToken()
        val phoneId = getActivePhoneNumberId()
        return token.isNotBlank() && phoneId.isNotBlank()
    }

    /**
     * Overrides API credentials at runtime.
     */
    fun setOverrideCredentials(token: String?, phoneId: String?) {
        customApiToken = token
        customPhoneNumberId = phoneId
    }

    /**
     * Clears any active runtime credential overrides.
     */
    fun clearOverrides() {
        customApiToken = null
        customPhoneNumberId = null
    }

    /**
     * Sends an automated text message via the WhatsApp Business Cloud API.
     */
    suspend fun sendTextMessage(
        recipientPhone: String,
        messageBody: String
    ): Result<WhatsAppMessageResponse> {
        val token = getActiveApiToken()
        val phoneId = getActivePhoneNumberId()

        if (token.isBlank() || phoneId.isBlank()) {
            return Result.failure(
                IllegalStateException("WhatsApp Business API credentials missing. Please set WHATSAPP_API_TOKEN and WHATSAPP_PHONE_NUMBER_ID in Secrets panel or Settings.")
            )
        }

        val cleanPhone = WhatsAppIntentHelper.cleanAndFormatPhoneNumber(recipientPhone)
        val request = WhatsAppMessageRequest(
            to = cleanPhone,
            text = WhatsAppTextMessage(previewUrl = true, body = messageBody)
        )

        return try {
            val response = apiService.sendMessage(
                authHeader = "Bearer $token",
                phoneNumberId = phoneId,
                request = request
            )

            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                Log.d(TAG, "Message sent successfully to $cleanPhone. Message ID: ${body.messages?.firstOrNull()?.id}")
                Result.success(body)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Log.e(TAG, "WhatsApp API Error (${response.code()}): $errorBody")
                Result.failure(Exception("WhatsApp Cloud API error (${response.code()}): $errorBody"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "WhatsApp API Exception: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }

    /**
     * Sends an automated fee reminder for a specific student.
     */
    suspend fun sendFeeReminderAutomated(
        fee: FeeRecord,
        centre: TuitionCentre
    ): Result<WhatsAppMessageResponse> {
        val message = WhatsAppIntentHelper.buildFeeReminderMessage(centre, fee)
        return sendTextMessage(fee.parentPhone, message)
    }

    /**
     * Sends an automated daily absence alert for an absent student.
     */
    suspend fun sendAbsentAlertAutomated(
        entry: AttendanceEntry,
        centre: TuitionCentre,
        date: String = "22 Oct 2026"
    ): Result<WhatsAppMessageResponse> {
        val message = WhatsAppIntentHelper.buildAbsentAlertMessage(centre, entry, date)
        return sendTextMessage(entry.parentPhone, message)
    }

    /**
     * Batch dispatches automated WhatsApp fee reminders to all overdue parents.
     */
    suspend fun sendBatchFeeReminders(
        fees: List<FeeRecord>,
        centre: TuitionCentre
    ): List<Pair<FeeRecord, Result<WhatsAppMessageResponse>>> {
        return fees.map { fee ->
            val result = sendFeeReminderAutomated(fee, centre)
            fee to result
        }
    }

    /**
     * Batch dispatches automated WhatsApp absence alerts to all absent students' parents.
     */
    suspend fun sendBatchAbsentAlerts(
        absentees: List<AttendanceEntry>,
        centre: TuitionCentre,
        date: String = "22 Oct 2026"
    ): List<Pair<AttendanceEntry, Result<WhatsAppMessageResponse>>> {
        return absentees.map { entry ->
            val result = sendAbsentAlertAutomated(entry, centre, date)
            entry to result
        }
    }
}
