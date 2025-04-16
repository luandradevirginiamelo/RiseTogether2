package com.example.risetogether

import android.content.Context
import android.util.Log
import com.google.auth.oauth2.GoogleCredentials
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject

object FcmV1Sender {

    fun sendNotification(context: Context, fcmToken: String, title: String, body: String) {
        // 🚀 Executa em background (IO thread)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Lê o JSON da conta de serviço do Firebase
                val inputStream = context.assets.open("risetogether-75090-firebase-adminsdk-fbsvc-3b0a4c9608.json")
                val credentials = GoogleCredentials.fromStream(inputStream)
                    .createScoped(listOf("https://www.googleapis.com/auth/firebase.messaging"))
                credentials.refreshIfExpired()
                val accessToken = credentials.accessToken.tokenValue

                // Monta o JSON da requisição
                val json = JSONObject().apply {
                    put("message", JSONObject().apply {
                        put("token", fcmToken)
                        put("notification", JSONObject().apply {
                            put("title", title)
                            put("body", body)
                        })
                    })
                }

                val bodyReq = RequestBody.create(
                    "application/json".toMediaTypeOrNull(),
                    json.toString()
                )

                val request = Request.Builder()
                    .url("https://fcm.googleapis.com/v1/projects/risetogether-75090/messages:send")
                    .addHeader("Authorization", "Bearer $accessToken")
                    .addHeader("Content-Type", "application/json; UTF-8")
                    .post(bodyReq)
                    .build()

                val client = OkHttpClient()
                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    Log.d("FCM-V1", "✅ Notification SENT!")
                } else {
                    Log.e("FCM-V1", "❌ Erro: ${response.code} - ${response.message}")
                }

            } catch (e: Exception) {
                Log.e("FCM-V1", "❌ Erro ao enviar push notification", e)
                Log.d("FCM-V1", "Token do paciente: $fcmToken")
                Log.d("FCM-V1", "Context válido? ${context != null}")
            }
        }
    }
}
