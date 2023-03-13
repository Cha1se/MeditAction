package com.example.myapplication3457

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

@Database(entities = [Card::class, Music::class, Statistic::class], version = 1, exportSchema = true)
abstract class AppDatabase: RoomDatabase() {
    abstract fun cardDao(): CardDAO

    companion object {
        private var INSTANCE: AppDatabase? = null

        fun getAppDatabase(context: Context): AppDatabase? {
            if (INSTANCE == null) {
                synchronized(AppDatabase::class) {
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "meditation_database").addCallback(object : Callback() {

                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)

                            Observable.fromCallable {

                                var db = AppDatabase.getAppDatabase(context)

                                var cardDao = db!!.cardDao()

                                cardDao.insertStatistic(Statistic(id = 0, counter = 0, streak = 0, lastDayOfUse = "none"))

                                cardDao.insertCard(
                                    Card(
                                        id = 0,
                                        background = "img_back_meditation",
                                        cardName = "Relax",
                                        timer = "00:15:00"
                                    )
                                )

                                cardDao.insertCard(
                                    Card(
                                        id = 0,
                                        background = "img_sleep_sample_card",
                                        cardName = "Sleep",
                                        timer = "00:30:00"
                                    )
                                )

                                cardDao.insertCard(
                                    Card(
                                        id = 0,
                                        background = "img_stress_sample_card",
                                        cardName = "Stress",
                                        timer = "00:20:00"
                                    )
                                )

                                cardDao.insertCard(
                                    Card(
                                        id = 0,
                                        background = "img_timer_sample_card",
                                        cardName = "Timer",
                                        timer = "00:20:00"
                                    )
                                )

                            }.doOnError({ Log.e("ERROR", it.message.toString())})
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe()

                        }

                        }).build()
                }
            }
            return INSTANCE
        }

        fun destroyDatabase() {
            INSTANCE = null
        }
    }

}