package com.optimeter.app.notification

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Helper object for scheduling and testing the monthly meter-reading reminder.
 *
 * Uses OneTimeWorkRequest (not Periodic) so we can precisely target the Nth day
 * of the next month at 12:00 PM. The worker re-schedules itself after firing.
 */
object NotificationScheduler {

    private const val WORK_NAME = "optimeter_monthly_reminder"

    /**
     * Schedule the reminder to fire on [dayOfMonth] at 12:00 PM local time.
     * If that time has already passed this month, it targets the same day next month.
     */
    fun scheduleMonthlyReminder(context: Context, dayOfMonth: Int) {
        val now = System.currentTimeMillis()
        val target = nextOccurrence(dayOfMonth)
        val delayMs = (target - now).coerceAtLeast(0)

        val inputData = Data.Builder()
            .putInt(ReminderWorker.KEY_DAY_OF_MONTH, dayOfMonth)
            .build()

        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.REPLACE, request)
    }

    /**
     * Fire the notification immediately (for the "Test Notification" button).
     */
    fun sendTestNotification(context: Context) {
        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInputData(
                Data.Builder()
                    .putInt(ReminderWorker.KEY_DAY_OF_MONTH, -1) // -1 = test, skip re-schedule
                    .build()
            )
            .build()

        WorkManager.getInstance(context).enqueue(request)
    }

    /**
     * Cancel any pending monthly reminder.
     */
    fun cancelReminder(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }

    /**
     * Calculate the next occurrence of [dayOfMonth] at 12:00 PM local time.
     */
    private fun nextOccurrence(dayOfMonth: Int): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            // Clamp day to the actual max for the current month
            val maxDay = getActualMaximum(Calendar.DAY_OF_MONTH)
            set(Calendar.DAY_OF_MONTH, dayOfMonth.coerceAtMost(maxDay))
        }

        // If that time is in the past, advance to the next month
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.MONTH, 1)
            // Re-clamp day for the new month
            val maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth.coerceAtMost(maxDay))
        }

        return calendar.timeInMillis
    }
}
