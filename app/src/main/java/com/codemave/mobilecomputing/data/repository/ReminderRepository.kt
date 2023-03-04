package com.codemave.mobilecomputing.data.repository

import com.codemave.mobilecomputing.data.entity.Reminder
import com.codemave.mobilecomputing.data.room.ReminderDao
import com.codemave.mobilecomputing.data.room.ReminderToCategory
import kotlinx.coroutines.flow.Flow

class ReminderRepository (
    private val reminderDao: ReminderDao
) {
    /**
     * return a flow containing the list of reminders associated with the category
     * with the given [categoryId]
     */
    fun remindersInCategory(categoryId: Long): Flow<List<ReminderToCategory>> {
        return reminderDao.reminderFromCategory(categoryId)
    }
    /**
     * Add a new [Reminder] to the reminder store
     */
    suspend fun addReminder(reminder: Reminder) = reminderDao.insert(reminder)
    /**
     * Delete a [Reminder] from the reminder store
     */
    suspend fun deleteReminder(reminder: Reminder) = reminderDao.delete(reminder)
    /**
     * Edit a [Reminder] in the reminder store
     */
    suspend fun editReminder(reminder: Reminder) = reminderDao.update(reminder)
}