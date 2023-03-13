package com.codemave.mobilecomputing.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "accounts",
    indices = [
        Index("user_name", unique = true)
    ]
)

data class Account(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Long = 0,
    @ColumnInfo(name = "user_name") val name: String,
    @ColumnInfo(name = "password") val password: String,
    @ColumnInfo(name = "location_x") var locationX: Double = 65.06,
    @ColumnInfo(name = "location_y") var locationY: Double = 25.47
)
