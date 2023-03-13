package com.example.myapplication3457

import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import jp.wasabeef.glide.transformations.RoundedCornersTransformation
import java.lang.Math.floor
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.Period
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.TimeUnit


class MeditationActivity : AppCompatActivity() {

    lateinit var backBtn: ImageView
    lateinit var progressBar: ProgressBar
    lateinit var timerText: TextView
    lateinit var titleText: TextView

    val Int.dpToPx: Int
        get() = (this * Resources.getSystem().displayMetrics.density).toInt()

    private var idCard: Int = 0

    lateinit var contentLay: ConstraintLayout
    private lateinit var timer: CountDownTimer
    private lateinit var cardDao: CardDAO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meditation)

        backBtn = findViewById(R.id.backArrow)
        progressBar = findViewById(R.id.progressBar)
        timerText = findViewById(R.id.timer)
        titleText = findViewById(R.id.titileMeditationText)
        contentLay = findViewById(R.id.contentLayout)

        idCard = intent.getIntExtra("id", 0)

        backBtn.setOnClickListener {
            timer.cancel()
            startActivity(Intent(this, MainActivity::class.java))
            overridePendingTransition(androidx.appcompat.R.anim.abc_fade_in, androidx.appcompat.R.anim.abc_fade_out)
        }

        GetValuesInDb()

    }

    fun GetValuesInDb() {
        var img: Int? = null
        var name: String? = null
        var timer: String? = null
        var uriImg: String? = null
        lateinit var cards: List<Card>

        Observable.fromCallable {

            // main
            var db = AppDatabase.getAppDatabase(applicationContext)

            cardDao = db!!.cardDao()

            // Add cards in database and update / delete

            // put values in database
            cards = cardDao.getCards()

        }.doOnError({Log.e("ERROR", it.message.toString())})
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnComplete {
                for (card in cards) {

                    if (idCard == card.id) {

                        img = application.resources.getIdentifier(
                            card.background,
                            "drawable",
                            applicationContext.packageName
                        )
                        name = card.cardName
                        timer = card.timer

                        var formatter = DateTimeFormatter.ISO_LOCAL_TIME
                        var time = LocalTime.parse(timer!!, formatter)
                        var timerMills: Long = ((time.hour * 3600000) + (time.minute * 60000) + (time.second * 1000)).toLong()

                        MeditationTimer(timerMills)

//                        contentLay.setBackgroundResource(img!!)

                        // CenterCrop for background

                        Glide.with(this).load(img!!)
                            .transform(CenterCrop())
                            .error(Uri.parse(card.background))
                            .into(object :
                                CustomTarget<Drawable>() {
                                override fun onLoadCleared(placeholder: Drawable?) {
                                }

                                override fun onResourceReady(
                                    resource: Drawable,
                                    transition: Transition<in Drawable>?
                                ) {
                                    contentLay.background = resource
                                }

                            })

                        titleText.text = name.toString()
                    }

                }
            }
            .subscribe()
    }

    fun MeditationTimer(mills: Long) {
        progressBar.max = mills.toInt()

        lateinit var min2: String
        lateinit var sec2: String
        lateinit var hour2: String

        timer = object: CountDownTimer(mills, 100) {
            override fun onTick(millisUntilFinished: Long) {

                var hour1 = TimeUnit.MILLISECONDS.toHours(millisUntilFinished)
                var min1 = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60
                var sec1 = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60 % 60

                min2 = min1.toString()
                sec2 = sec1.toString()
                hour2 = hour1.toString()

                if (min1 < 10) {
                    min2 = "0$min1"
                }
                if (sec1 < 10) {
                    sec2 = "0$sec1"
                }
                if (hour1 < 10) {
                    hour2 = "0$hour1"
                }
                if (hour1 > 0) {
                    timerText.setText("$hour2:$min2:$sec2")
                } else {
                    timerText.setText("$min2:$sec2")
                }
                progressBar.progress = millisUntilFinished.toInt()
            }

            override fun onFinish() {
                progressBar.progress = 0
                timerText.setText("Good work")

                sessionCounter(mills)

            }
        }
        timer.start()

    }

    override fun onBackPressed() {
        timer.cancel()
        startActivity(Intent(this, MainActivity::class.java))
        overridePendingTransition(androidx.appcompat.R.anim.abc_fade_in, androidx.appcompat.R.anim.abc_fade_out)
    }

    fun sessionCounter(timer: Long) {
        Observable.fromCallable {

            // main
            var db = AppDatabase.getAppDatabase(applicationContext)

            cardDao = db!!.cardDao()

            var stats: Statistic = cardDao.getStats().first()

            val from = LocalDate.parse(stats.lastDayOfUse, DateTimeFormatter.ofPattern("ddMMyyyy"))
            // get today's date
            val today = LocalDate.now()
            // calculate the period between those two
            var period = Period.between(from, today)
            // and print it in a human-readable way

            if (period.days == 1 && period.months == 0 && period.years == 0) {
                cardDao.updateStatistic(stats.copy(streak = stats.streak + 1))
            }

            cardDao.updateStatistic(stats.copy(counter = (stats.counter + timer), lastDayOfUse = today.format(DateTimeFormatter.ofPattern("ddMMyyyy")).toString()))

        }.doOnError({Log.e("ERROR", it.message.toString())})
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe()
    }

}