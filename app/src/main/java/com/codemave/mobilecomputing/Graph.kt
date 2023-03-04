package com.codemave.mobilecomputing

import android.content.Context
import androidx.room.Room
import com.codemave.mobilecomputing.data.repository.AccountRepository
import com.codemave.mobilecomputing.data.repository.CategoryRepository
import com.codemave.mobilecomputing.data.repository.ReminderRepository
import com.codemave.mobilecomputing.data.room.PhoneBossDatabase

/**
 * a simple singleton dependency graph
 * for real app, please use Koin/Dagger/Hilt instead
 */
object Graph {
    lateinit var database: PhoneBossDatabase
        private set

    lateinit var appContext: Context

    val categoryRepository by lazy {
        CategoryRepository(
            categoryDao = database.categoryDao()
        )
    }

    val reminderRepository by lazy {
        ReminderRepository(
            reminderDao = database.reminderDao()
        )
    }

    val accountRepository by lazy {
        AccountRepository(
            accountDao = database.accountDao()
        )
    }

    fun provide (context: Context) {
        appContext = context
        database = Room.databaseBuilder(context, PhoneBossDatabase::class.java, "mcData.db")
            .fallbackToDestructiveMigration() //don't use in production app
            .build()
    }
}