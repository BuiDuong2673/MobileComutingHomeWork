package com.codemave.mobilecomputing.data.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.codemave.mobilecomputing.data.entity.Reminder
import kotlinx.coroutines.flow.Flow

@Dao
abstract class ReminderDao {
    @Query("""
        SELECT reminders.* FROM reminders
        INNER JOIN categories ON reminders.reminder_category_id = categories.id
        WHERE reminder_category_id = :categoryId
    """)
    abstract fun reminderFromCategory(categoryId: Long): Flow<List<ReminderToCategory>>

    @Query("""SELECT * FROM reminders WHERE id = :reminderId""")
    abstract fun reminder(reminderId: Long): Reminder?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(entity: Reminder): Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun update (entity: Reminder)

    @Delete
    abstract suspend fun delete(entity: Reminder): Int
}