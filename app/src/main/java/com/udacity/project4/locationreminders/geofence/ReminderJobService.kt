package com.udacity.project4.locationreminders.geofence

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.udacity.project4.R
import com.udacity.project4.locationreminders.ReminderDescriptionActivity
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.savereminder.SaveReminderFragment
import kotlinx.coroutines.runBlocking
import kotlin.random.Random

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
private val TAG = ReminderJobService::class.java.simpleName

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class ReminderJobService : JobService() {
    var jobCancelled = false

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onStartJob(params: JobParameters?): Boolean {
        Log.d(TAG, "Job started")
        doBackgroundWork(params)
        return true
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun doBackgroundWork(params: JobParameters?) {
        Thread {
            kotlin.run {
                if (jobCancelled) {
                    return@Thread
                }

                var localDb = LocalDB.createRemindersDao(applicationContext )
                runBlocking {
                    if (localDb.getReminders().isNotEmpty())
                    {
                        val last = localDb.getReminders().last()
                        showNotification(applicationContext, "\"The ${last.title} place is entered \"")

                    }

                }

                jobFinished(params, true)
            }
        }.start()
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        Log.d(TAG, "Job cancelled before completion")
        jobCancelled = true
        return true
    }

    fun showNotification(context: Context?, message: String) {
        val CHANNEL_ID = "REMINDER_NOTIFICATION_CHANNEL"
        var notificationId = 1589
        notificationId += Random(notificationId).nextInt(1, 30)
        val intent = Intent(this, ReminderDescriptionActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val notificationBuilder = NotificationCompat.Builder(context!!.applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.map)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(message)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(message)
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .addAction(R.drawable.abc_vector_test,"Check The Details",pendingIntent)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.app_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.app_name)
            }
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(notificationId, notificationBuilder.build())
    }

}