package com.example.risetogether

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.risetogether.FcmV1Sender
import com.google.firebase.firestore.FirebaseFirestore

class ReminderWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    override fun doWork(): Result {
        val db = FirebaseFirestore.getInstance()

        // Filtra reminders do dia atual e que não são só de teste
        val today = java.time.LocalDate.now().toString()

        db.collection("AcceptedTaskRequestReminder")
            .whereEqualTo("ReminderSendDate", today)
            .whereEqualTo("isTestReminder", false)
            .get()
            .addOnSuccessListener { docs ->
                for (doc in docs) {
                    val token = "" // aqui você precisa puxar o FCM token do paciente, como no seu código
                    val taskName = doc.getString("AssistTaskName") ?: ""
                    val time = doc.getString("RequestedTime") ?: ""
                    val day = doc.getString("RequestedDay") ?: ""

                    val message = "📌 Reminder: You have \"$taskName\" today ($day) at $time."

                    if (token.isNotEmpty()) {
                        FcmV1Sender.sendNotification(
                            context = applicationContext,
                            fcmToken = token,
                            title = "Daily Task Reminder",
                            body = message
                        )
                    }
                }
            }

        return Result.success()
    }
}


