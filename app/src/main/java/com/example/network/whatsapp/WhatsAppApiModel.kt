package com.example.network.whatsapp

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WhatsAppTextMessage(
    @Json(name = "preview_url") val previewUrl: Boolean = false,
    @Json(name = "body") val body: String
)

@JsonClass(generateAdapter = true)
data class WhatsAppMessageRequest(
    @Json(name = "messaging_product") val messagingProduct: String = "whatsapp",
    @Json(name = "recipient_type") val recipientType: String = "individual",
    @Json(name = "to") val to: String,
    @Json(name = "type") val type: String = "text",
    @Json(name = "text") val text: WhatsAppTextMessage
)

@JsonClass(generateAdapter = true)
data class WhatsAppContact(
    @Json(name = "input") val input: String,
    @Json(name = "wa_id") val waId: String
)

@JsonClass(generateAdapter = true)
data class WhatsAppMessageId(
    @Json(name = "id") val id: String
)

@JsonClass(generateAdapter = true)
data class WhatsAppMessageResponse(
    @Json(name = "messaging_product") val messagingProduct: String? = null,
    @Json(name = "contacts") val contacts: List<WhatsAppContact>? = null,
    @Json(name = "messages") val messages: List<WhatsAppMessageId>? = null
)

@JsonClass(generateAdapter = true)
data class WhatsAppApiError(
    @Json(name = "message") val message: String,
    @Json(name = "type") val type: String? = null,
    @Json(name = "code") val code: Int? = null,
    @Json(name = "error_subcode") val errorSubcode: Int? = null,
    @Json(name = "fbtrace_id") val fbtraceId: String? = null
)

@JsonClass(generateAdapter = true)
data class WhatsAppErrorResponse(
    @Json(name = "error") val error: WhatsAppApiError?
)

/**
 * Result state of a WhatsApp Cloud API automation task.
 */
sealed class WhatsAppApiStatus {
    object Idle : WhatsAppApiStatus()
    object Sending : WhatsAppApiStatus()
    data class Success(val messageId: String, val recipient: String) : WhatsAppApiStatus()
    data class Error(val errorMessage: String, val canFallbackToIntent: Boolean = true) : WhatsAppApiStatus()
}
