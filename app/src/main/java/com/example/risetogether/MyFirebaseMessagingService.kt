package com.example.risetogether

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.example.risetogether.R
import com.example.risetogether.Patient.ViewNotificationsRemindersScreen // ✅ Corrigido aqui!

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        const val CHANNEL_ID = "default_channel"
        const val CHANNEL_NAME = "Notificações do App"
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val title = remoteMessage.notification?.title ?: "Nova Mensagem"
        val body = remoteMessage.notification?.body ?: "Você recebeu uma nova notificação."

        Log.d("FCM", "🔥 Notificação recebida: $title - $body")

        showNotification(title, body)
    }

    private fun showNotification(title: String, body: String) {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val soundUri = Uri.parse("android.resource://${packageName}/raw/notification_sound")

        // 👉 Intent correta agora!
        val intent = Intent(this, ViewNotificationsRemindersScreen::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                enableVibration(true)
                enableLights(true)
                description = "Canal padrão para notificações do app."
                setSound(
                    soundUri,
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
            }

            if (notificationManager.getNotificationChannel(CHANNEL_ID) == null) {
                notificationManager.createNotificationChannel(channel)
            }
        }

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSound(soundUri)
            .setContentIntent(pendingIntent) // ✅ Agora tá indo pra tela certa
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
