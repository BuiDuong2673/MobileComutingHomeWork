package com.codemave.mobilecomputing.data.entity

import androidx.room.*

@Entity(
    tableName = "reminders",
    indices = [
        Index("id", unique = true),
        Index("reminder_category_id"),
        Index("creator_id")
    ],
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["reminder_category_id"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Account::class,
            parentColumns = ["id"],
            childColumns = ["creator_id"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        )
    ]
)

data class Reminder (
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val reminderId: Long = 0,
    @ColumnInfo(name = "reminder_title") val reminderTitle: String,
    @ColumnInfo(name = "reminder_category_id") val reminderCategoryId: Long,
    @ColumnInfo(name = "creator_id") val creatorId: Long,
    @ColumnInfo(name = "location_x") val locationX: Double = 65.06,
    @ColumnInfo(name = "location_y") val locationY: Double = 25.47,
    @ColumnInfo(name = "reminder_time") val reminderTime: String,
    @ColumnInfo(name = "creation_time") val creationTime: String,
    @ColumnInfo(name = "reminder_message") val reminderMessage: String,
    @ColumnInfo(name = "reminder_seen") var reminderSeen: Boolean = false,
    @ColumnInfo(name = "send_notification") var sendNotification: Boolean = true
)