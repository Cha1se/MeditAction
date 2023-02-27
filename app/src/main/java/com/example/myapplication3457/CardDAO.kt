package com.example.myapplication3457

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface CardDAO {

    @Query("SELECT * FROM Card")
    fun getCards(): List<Card>

    @Query("SELECT * FROM Music")
    fun getMusic(): List<Music>

    @Query("SELECT * FROM Statistic")
    fun getStats(): List<Statistic>

    @Insert
    fun insertCard(card: Card)

    @Insert
    fun insertMusic(music: Music)

    @Insert
    fun insertCounter(statistic: Statistic)

    @Insert
    fun insertStreak(statistic: Statistic)

    @Update
    fun updateCardName(cardName: Card)

    @Update
    fun updateBackground(background: Card)

    @Update
    fun updateMusic(music: Music)

    @Update
    fun updateCounter(statistic: Statistic)

    @Update
    fun updateStreak(statistic: Statistic)

}