package com.example.myapplication3457

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Card::class, Music::class, Statistic::class], version = 1, exportSchema = true)
abstract class AppDatabase: RoomDatabase() {
    abstract  fun cardDao(): CardDAO

    companion object {
        private var INSTANCE: AppDatabase? = null

        fun getAppDatabase(context: Context): AppDatabase? {
            if (INSTANCE == null) {
                synchronized(AppDatabase::class) {
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "card_database").build()
                }
            }
            return INSTANCE
        }

        fun destroyDatabase() {
            INSTANCE = null
        }
    }

}