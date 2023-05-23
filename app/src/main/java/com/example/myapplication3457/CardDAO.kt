package com.example.myapplication3457

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update


@Dao
interface CardDAO {

    // Get

    @Query("SELECT * FROM Card")
    fun getCards(): List<Card>

    @Query("SELECT * FROM Music")
    fun getMusic(): List<Music>

    @Query("SELECT * FROM Statistic")
    fun getStats(): List<Statistic>

    @Query("SELECT * FROM Card WHERE id=:id")
    fun getCardById(id: Int): Card


    // Insert

    @Insert
    fun insertCard(card: Card)

    @Insert
    fun insertMusic(music: Music)

    @Insert
    fun insertStatistic(statistic: Statistic)


    // Update

    @Update
    fun updateCard(card: Card)

    @Update
    fun updateMusic(music: Music)

    @Update
    fun updateStatistic(statistic: Statistic)


    // Delete

    @Delete
    fun deleteCard(card: Card)

    @Delete
    fun deleteMusic(music: Music)

    @Delete
    fun deleteStatistic(statistic: Statistic)


    @Query("DELETE FROM Card")
    fun deleteAllCards()

}