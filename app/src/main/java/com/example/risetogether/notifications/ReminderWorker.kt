package com.example.risetogether.notifications

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.risetogether.FcmV1Sender
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ReminderWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    override fun doWork(): Result {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return Result.failure()
        val db = FirebaseFirestore.getInstance()

        db.collection("PatientinTreatment").document(userId).get()
            .addOnSuccessListener { doc ->
                val fcmToken = doc.getString("fcmToken")
                val patientName = doc.getString("CancerPatientName") ?: "Patient"

                val message = "📅 Reminder: $patientName, you may have an assistance task scheduled today. Please check your dashboard."

                if (!fcmToken.isNullOrEmpty()) {
                    FcmV1Sender.sendNotification(
                        context = applicationContext,
                        fcmToken = fcmToken,
                        title = "Daily Task Reminder",
                        body = message
                    )
                }
            }

        return Result.success()
    }
}


