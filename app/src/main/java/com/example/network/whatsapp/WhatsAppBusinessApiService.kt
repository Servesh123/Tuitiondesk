package com.example.network.whatsapp

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Retrofit Service Interface targeting Meta WhatsApp Business Cloud API (Graph API).
 */
interface WhatsAppBusinessApiService {

    @POST("{phoneNumberId}/messages")
    suspend fun sendMessage(
        @Header("Authorization") authHeader: String,
        @Path("phoneNumberId") phoneNumberId: String,
        @Body request: WhatsAppMessageRequest
    ): Response<WhatsAppMessageResponse>
}
