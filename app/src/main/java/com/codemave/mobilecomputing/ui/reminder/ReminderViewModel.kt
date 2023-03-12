package com.codemave.mobilecomputing.ui.reminder

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.icu.text.SimpleDateFormat
import android.location.Location
import android.location.Location.distanceBetween
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Looper
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.codemave.mobilecomputing.Graph
import com.codemave.mobilecomputing.R
import com.codemave.mobilecomputing.data.entity.Category
import com.codemave.mobilecomputing.data.entity.Reminder
import com.codemave.mobilecomputing.data.repository.CategoryRepository
import com.codemave.mobilecomputing.data.repository.ReminderRepository
import com.codemave.mobilecomputing.util.NotificationWorker
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit
import androidx.core.app.NotificationManagerCompat.from
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.flow.update

class ReminderViewModel (
    private val reminderRepository: ReminderRepository = Graph.reminderRepository,
    private val categoryRepository: CategoryRepository = Graph.categoryRepository
) : ViewModel() {
    private val _state = MutableStateFlow(ReminderViewState())

    val state: StateFlow<ReminderViewState>
        get() = _state

    suspend fun saveReminder(reminder: Reminder): Long {
        return if (reminder.reminderCategoryId.equals(-1)) {
            0
        } else {
            createReminderMadeNotification(reminder)
            if (reminder.sendNotification) {
                setReminderTimeNotification(reminder)
            }
            reminderRepository.addReminder(reminder)
        }
    }

    suspend fun deleteReminder(reminder: Reminder): Int {
        createDeleteReminderNotification(reminder)
        return reminderRepository.deleteReminder(reminder)
    }

    suspend fun editReminder(reminder: Reminder) {
        createEditReminderNotification(reminder)
        if (reminder.sendNotification) {
            setReminderTimeNotification(reminder)
        }
        reminderRepository.editReminder(reminder)
    }

    init {
        viewModelScope.launch {
            createNotificationChannel(context = Graph.appContext)
            categoryRepository.categories().collect { categories ->
                _state.value = ReminderViewState(categories)
            }
        }
    }
}

/**
 * Create [NotificationChannel]
 * only for API 26+ because NotificationChannel class is new
 */
private fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = "NotificationChannelName"
        val descriptionText = "NotificationChannelDescriptionText"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel("CHANNEL_ID", name, importance).apply {
            description = descriptionText
        }
        // register the channel with the system
        val notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}

/**
 * Remind user again and again when the user does not click the Check icon
 */
private fun createMoreNotification() {
    val notificationId = 1
    val builder = NotificationCompat.Builder(Graph.appContext, "CHANNEL_ID")
        .setSmallIcon(R.drawable.ic_period_noti)
        .setContentTitle("NOTICE")
        .setContentText("You have a reminder recently.\nHave you done that?\nIf yes, click the Check icon to stop receiving related reminder.")
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
    with (NotificationManagerCompat.from(Graph.appContext)) {
        notify (notificationId, builder.build())
    }
}

/**
 * Create Error Notification when something went wrong
 */
private fun createErrorNotification() {
    val notificationId = 2
    val builder = NotificationCompat.Builder(Graph.appContext, "CHANNEL_ID")
        .setSmallIcon(R.drawable.ic_error_noti)
        .setContentTitle("Error")
        .setContentText("It seems like something went wrong.")
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
    with (NotificationManagerCompat.from(Graph.appContext)) {
        notify (notificationId, builder.build())
    }
}

/**
 * Notification when user SAVE REMINDER
 */
private fun createReminderMadeNotification(reminder: Reminder) {
    val notificationId = 3
    val intentToast = Intent("trigger_toast")
    val pendingIntent = PendingIntent.getBroadcast(Graph.appContext, 0, intentToast, PendingIntent.FLAG_MUTABLE)

    val builder = NotificationCompat.Builder(Graph.appContext, "CHANNEL_ID")
        .setSmallIcon(R.drawable.ic_add_noti)
        .setContentTitle("New Reminder Made")
        .setContentText("You have created ${reminder.reminderTitle} on ${reminder.reminderTime}.")
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)
        .setColor(ContextCompat.getColor(Graph.appContext, R.color.orange))
        .setPriority(NotificationCompat.PRIORITY_HIGH)
    with (NotificationManagerCompat.from(Graph.appContext)) {
        notify (notificationId, builder.build())
    }
}

/**
 * Notification when user EDIT REMINDER
 */
private fun createEditReminderNotification(reminder: Reminder) {
    val notificationId = 4

    val intentToast = Intent("trigger_toast")
    val pendingIntent = PendingIntent.getBroadcast(Graph.appContext, 0, intentToast, PendingIntent.FLAG_MUTABLE)

    val builder = NotificationCompat.Builder(Graph.appContext, "CHANNEL_ID")
        .setSmallIcon(R.drawable.ic_edit_noti)
        .setColor(ContextCompat.getColor(Graph.appContext, R.color.orange))
        .setContentTitle("Edit Successfully")
        .setContentText("Your modification of the reminder ${reminder.reminderTitle} have been saved.")
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
    with (NotificationManagerCompat.from(Graph.appContext)) {
        notify(notificationId, builder.build())
    }
}

/**
 * Notification when the user DELETE REMINDER
 */
private fun createDeleteReminderNotification(reminder:Reminder) {
    val notificationId = 5
    val intentToast = Intent("trigger_toast")
    val pendingIntent = PendingIntent.getBroadcast(Graph.appContext, 0, intentToast, PendingIntent.FLAG_MUTABLE)
    val builder = NotificationCompat.Builder(Graph.appContext, "CHANNEL_ID")
        .setSmallIcon(R.drawable.ic_delete_noti)
        .setColor(ContextCompat.getColor(Graph.appContext, R.color.orange))
        .setContentTitle("Reminder Deleted")
        .setContentText("You have deleted ${reminder.reminderTitle}.")
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
    with (NotificationManagerCompat.from(Graph.appContext)) {
        notify (notificationId, builder.build())
    }
}

/**
 * Notification when reminder time arrive
 */
private fun createReminderNotification(reminder: Reminder) {
    val notificationId = 6
    val intentToast = Intent("trigger_toast")
    val pendingIntent = PendingIntent.getBroadcast(Graph.appContext, 0, intentToast, PendingIntent.FLAG_MUTABLE)
    val builder = NotificationCompat.Builder(Graph.appContext, "CHANNEL_ID")
        .setSmallIcon(R.drawable.ic_success_noti)
        .setColor(ContextCompat.getColor(Graph.appContext, R.color.orange))
        .setContentTitle("Reminder: ${reminder.reminderTitle}")
        .setContentText("Remember an event on ${reminder.reminderTime}\nMessage: ${reminder.reminderMessage}")
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
    with (NotificationManagerCompat.from(Graph.appContext)) {
        notify (notificationId, builder.build())
    }
}

/**
 * Set the time to send the reminder notification
 */
private fun setReminderTimeNotification(reminder: Reminder) {
    val workManager = WorkManager.getInstance(Graph.appContext)
    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()
    val notificationWorker = OneTimeWorkRequestBuilder<NotificationWorker>()
        .setInitialDelay(calculateTimeDelay(reminder.reminderTime), TimeUnit.MILLISECONDS)
        .setConstraints(constraints)
        .build()
    workManager.enqueue(notificationWorker)
    workManager.getWorkInfoByIdLiveData(notificationWorker.id)
        .observeForever { workInfo ->
            if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                createReminderNotification(reminder)
            } else if (workInfo.state == WorkInfo.State.FAILED) {
                createErrorNotification()
            }
        }
}

/**
 * Notification that repeat every minute
 */
private fun setRecurringReminder() {
    val workManager = WorkManager.getInstance(Graph.appContext)
    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()
    val notificationWorker = OneTimeWorkRequestBuilder<NotificationWorker>()
        .setInitialDelay(1, TimeUnit.MINUTES)
        .setConstraints(constraints)
        .build()
    workManager.enqueue(notificationWorker)

    workManager.getWorkInfoByIdLiveData(notificationWorker.id)
        .observeForever { workInfo ->
            if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                createMoreNotification()
            } else if (workInfo.state == WorkInfo.State.FAILED) {
                createErrorNotification()
            }
        }
}

/**
 * Calculate the time to send reminder notification
 */
private fun calculateTimeDelay(dateString: String): Long {
    val format = SimpleDateFormat("MMMM dd, yyyy hh:mm a", Locale.ENGLISH)
    val date = format.parse(dateString)
    return date.time - System.currentTimeMillis()
}

/**
 * Notification when user location is near the reminder location
 */
fun reminderIsNearNotification(reminder: Reminder) {
    val notificationId = 7
    val builder = NotificationCompat.Builder(Graph.appContext, "CHANNEL_ID")
        .setSmallIcon(R.drawable.ic_success_noti)
        .setContentTitle("You are near the reminder location")
        .setContentText("You are currently near to the reminder ${reminder.reminderTitle}'s location.")
        .setPriority(NotificationCompat.PRIORITY_HIGH)
    with (NotificationManagerCompat.from(Graph.appContext)) {
        notify (notificationId, builder.build())
    }
}


data class ReminderViewState(
    val categories: List<Category> = emptyList(),
    val reminders: List<Reminder> = emptyList()
)