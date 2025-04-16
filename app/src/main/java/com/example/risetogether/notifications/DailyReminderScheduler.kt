package com.example.risetogether.notifications


import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

object DailyReminderScheduler {

    fun scheduleDailyReminder(context: Context) {
        val workRequest = PeriodicWorkRequestBuilder<ReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(1, TimeUnit.MINUTES) // For test propose edit for just -1 day before the task
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "DailyTaskReminder",
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
    }
}
