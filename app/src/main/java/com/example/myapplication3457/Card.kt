package com.example.myapplication3457

import androidx.annotation.Nullable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Card(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val cardName: String,
    val background: String,
    val timer: String
)

@Entity
data class Music(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val music: String
)

@Entity
data class Statistic(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val counter: Long = 0L,
    val streak: Int = 0,
    val lastDayOfUse: String
)