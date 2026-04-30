package com.optimeter.app.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.optimeter.app.R

/**
 * A WorkManager Worker that builds and displays a local push notification
 * reminding the user to submit their utility meter readings.
 *
 * After sending the notification it re-schedules itself for the next month
 * so the reminder chain keeps repeating.
 */
class ReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val CHANNEL_ID = "optimeter_reminder_channel"
        const val NOTIFICATION_ID = 1001
        const val KEY_DAY_OF_MONTH = "day_of_month"
    }

    override suspend fun doWork(): Result {
        createNotificationChannel()
        showNotification()

        // Re-schedule for the next month so the chain keeps going.
        // dayOfMonth == -1 means this was a test fire — don't re-schedule.
        val dayOfMonth = inputData.getInt(KEY_DAY_OF_MONTH, 25)
        if (dayOfMonth > 0) {
            NotificationScheduler.scheduleMonthlyReminder(applicationContext, dayOfMonth)
        }

        return Result.success()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = applicationContext.getString(R.string.notif_channel_name)
            val descriptionText = applicationContext.getString(R.string.notif_channel_desc)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun showNotification() {
        val title = applicationContext.getString(R.string.notif_reminder_title)
        val body = applicationContext.getString(R.string.notif_reminder_body)

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(applicationContext)
                .notify(NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            // POST_NOTIFICATIONS permission not granted — silently ignore
        }
    }
}
