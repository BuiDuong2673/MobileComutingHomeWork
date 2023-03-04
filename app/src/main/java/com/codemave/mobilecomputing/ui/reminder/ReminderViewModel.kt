package com.codemave.mobilecomputing.ui.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Build
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.work.*
import com.codemave.mobilecomputing.Graph
import com.codemave.mobilecomputing.PhoneBossApplication
import com.codemave.mobilecomputing.R
import com.codemave.mobilecomputing.data.entity.Category
import com.codemave.mobilecomputing.data.entity.Reminder
import com.codemave.mobilecomputing.data.repository.CategoryRepository
import com.codemave.mobilecomputing.data.repository.ReminderRepository
import com.codemave.mobilecomputing.ui.MainActivity
import com.codemave.mobilecomputing.ui.theme.Yellow
import com.codemave.mobilecomputing.util.NotificationWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit

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
            setReminderTimeNotification(reminder)
            reminderRepository.addReminder(reminder)
        }
    }

    suspend fun deleteReminder(reminder: Reminder): Int {
        return reminderRepository.deleteReminder(reminder)
    }

    suspend fun editReminder(reminder: Reminder) {
        createEditReminderNotification(reminder)
        setReminderTimeNotification(reminder)
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

private fun calculateTimeDelay(dateString: String): Long {
    val format = SimpleDateFormat("MMMM dd, yyyy hh:mm a", Locale.ENGLISH)
    val date = format.parse(dateString)
    return date.time - System.currentTimeMillis()
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

private fun createReminderMadeNotification(reminder: Reminder) {
    val notificationId = 1

    val intentToast = Intent("trigger_toast")
    val pendingIntent = PendingIntent.getBroadcast(Graph.appContext, 0, intentToast, PendingIntent.FLAG_MUTABLE)

    val builder = NotificationCompat.Builder(Graph.appContext, "CHANNEL_ID")
        .setSmallIcon(R.drawable.ic_add_noti)
        .setContentTitle("New Reminder Made")
        .setContentText("You have created ${reminder.reminderTitle} on ${reminder.reminderTime}")
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
    with (NotificationManagerCompat.from(Graph.appContext)) {
        notify (notificationId, builder.build())
    }
}

private fun createReminderNotification(reminder: Reminder) {
    val notificationId = 2
    val builder = NotificationCompat.Builder(Graph.appContext, "CHANNEL_ID")
        .setSmallIcon(R.drawable.ic_success_noti)
        .setContentTitle("Reminder: ${reminder.reminderTitle}")
        .setContentText("Remember an event on ${reminder.reminderTime}\nMessage: ${reminder.reminderMessage}")
        .setPriority(NotificationCompat.PRIORITY_HIGH)
    with (NotificationManagerCompat.from(Graph.appContext)) {
        notify(notificationId, builder.build())
    }
}

private fun createErrorNotification() {
    val notificationId = 3
    val builder = NotificationCompat.Builder(Graph.appContext, "CHANNEL_ID")
        .setSmallIcon(R.drawable.ic_error_noti)
        .setContentTitle("Error")
        .setContentText("There is some error in the process.") // TODO
        .setPriority(NotificationCompat.PRIORITY_HIGH)
    with (NotificationManagerCompat.from(Graph.appContext)) {
        notify(notificationId, builder.build())
    }
}

private fun createEditReminderNotification(reminder: Reminder) {
    val notificationId = 4
    val builder = NotificationCompat.Builder(Graph.appContext, "CHANNEL_ID")
        .setSmallIcon(R.drawable.ic_edit_noti)
        .setContentTitle("Edit Successfully")
        .setContentText("Your modification have been saved.")
        .setPriority(NotificationCompat.PRIORITY_HIGH)
    with (NotificationManagerCompat.from(Graph.appContext)) {
        notify (notificationId, builder.build())
    }
}

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

data class ReminderViewState(
    val categories: List<Category> = emptyList()
)