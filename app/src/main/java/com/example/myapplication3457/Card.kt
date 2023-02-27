package com.example.myapplication3457

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Card(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    val cardName: String,
    val background: String
)

@Entity
data class Music(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    val music: String
)

@Entity
data class Statistic(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    val counter: Int,
    val streak: Int
)