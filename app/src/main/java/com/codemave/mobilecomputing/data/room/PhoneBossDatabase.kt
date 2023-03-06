package com.codemave.mobilecomputing.data.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.codemave.mobilecomputing.data.entity.Account
import com.codemave.mobilecomputing.data.entity.Category
import com.codemave.mobilecomputing.data.entity.Reminder

/**
 * [RoomDatabase] for this app
 */
@Database(
    entities = [Category::class, Reminder::class, Account::class],
    version = 2,
    exportSchema = false
)

abstract class PhoneBossDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun reminderDao(): ReminderDao
    abstract fun accountDao(): AccountDao
}