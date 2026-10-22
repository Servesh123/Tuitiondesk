package com.example

import com.example.network.whatsapp.WhatsAppBusinessApiClient
import com.example.network.whatsapp.WhatsAppMessageRequest
import com.example.network.whatsapp.WhatsAppTextMessage
import com.example.util.WhatsAppIntentHelper
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class WhatsAppBusinessApiTest {

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    @Before
    fun setup() {
        WhatsAppBusinessApiClient.clearOverrides()
    }

    @Test
    fun testWhatsAppMessageRequestSerialization() {
        val request = WhatsAppMessageRequest(
            to = "919876543210",
            text = WhatsAppTextMessage(body = "Namaste Parent, fee reminder.")
        )
        val adapter = moshi.adapter(WhatsAppMessageRequest::class.java)
        val json = adapter.toJson(request)

        assertTrue(json.contains("\"messaging_product\":\"whatsapp\""))
        assertTrue(json.contains("\"to\":\"919876543210\""))
        assertTrue(json.contains("\"type\":\"text\""))
        assertTrue(json.contains("\"body\":\"Namaste Parent, fee reminder.\""))
    }

    @Test
    fun testPhoneNumberFormatting() {
        assertEquals("919876543210", WhatsAppIntentHelper.cleanAndFormatPhoneNumber("9876543210"))
        assertEquals("919876543210", WhatsAppIntentHelper.cleanAndFormatPhoneNumber("+91 98765-43210"))
        assertEquals("919876543210", WhatsAppIntentHelper.cleanAndFormatPhoneNumber("919876543210"))
        assertEquals("919876543210", WhatsAppIntentHelper.cleanAndFormatPhoneNumber("+919876543210"))
    }

    @Test
    fun testWhatsAppBusinessApiClientOverrides() {
        // Initially without overrides
        WhatsAppBusinessApiClient.clearOverrides()

        // Apply custom valid credentials
        WhatsAppBusinessApiClient.setOverrideCredentials(
            token = "EAAQWERTYUIOP1234567890",
            phoneId = "109876543210987"
        )

        assertTrue(WhatsAppBusinessApiClient.isApiConfigured())
        assertEquals("EAAQWERTYUIOP1234567890", WhatsAppBusinessApiClient.getActiveApiToken())
        assertEquals("109876543210987", WhatsAppBusinessApiClient.getActivePhoneNumberId())

        // Clear overrides
        WhatsAppBusinessApiClient.clearOverrides()
        assertFalse(WhatsAppBusinessApiClient.isApiConfigured())
    }

    @Test
    fun testPlaceholderCredentialsRejected() {
        WhatsAppBusinessApiClient.setOverrideCredentials(
            token = "YOUR_WHATSAPP_BUSINESS_API_TOKEN",
            phoneId = "YOUR_WHATSAPP_PHONE_NUMBER_ID"
        )
        assertFalse(WhatsAppBusinessApiClient.isApiConfigured())
        assertEquals("", WhatsAppBusinessApiClient.getActiveApiToken())
        assertEquals("", WhatsAppBusinessApiClient.getActivePhoneNumberId())
    }
}
