package com.example.myapplication3457

import android.content.Intent
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
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
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.time.LocalDate
import java.time.LocalTime
import java.time.Period
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit


class MeditationActivity : AppCompatActivity() {

    private lateinit var backBtn: ImageView
    private lateinit var progressBar: ProgressBar
    private lateinit var timerText: TextView
    private lateinit var titleText: TextView

    private var idCard: Int = 0
    private var mMediaPlayer: MediaPlayer? = null

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
            overridePendingTransition(
                androidx.appcompat.R.anim.abc_fade_in,
                androidx.appcompat.R.anim.abc_fade_out
            )
        }

        getValuesInDb()

    }

    private fun getValuesInDb() {
        var img: Int?
        var name: String?
        var timer: String?
        lateinit var cards: List<Card>

        Observable.fromCallable {

            val db = AppDatabase.getAppDatabase(applicationContext)

            cardDao = db!!.cardDao()

            cards = cardDao.getCards()

        }.subscribeOn(Schedulers.io())
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

                        val formatter = DateTimeFormatter.ISO_LOCAL_TIME
                        val time = LocalTime.parse(timer!!, formatter)
                        val timerMills: Long =
                            ((time.hour * 3600000) + (time.minute * 60000)).toLong()

                        meditationTimer(timerMills)

                        // CenterCrop for background

                        Glide.with(this).load(img!!)
                            .transform(CenterCrop())
                            .apply(RequestOptions.skipMemoryCacheOf(true))
                            .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
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

    private fun meditationTimer(mills: Long) {
        progressBar.max = mills.toInt()

        lateinit var min2: String
        lateinit var sec2: String
        lateinit var hour2: String

        var iter = 5
        progressBar.progress = progressBar.max

        timer = object : CountDownTimer(5000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timerText.text = iter.toString()
                iter--
            }

            override fun onFinish() {

                timerText.animate()
                    .alpha(0f)
                    .scaleX(0f)
                    .scaleY(0f)
                    .setDuration(100)
                    .withEndAction {
                        timerText.animate()
                            .alpha(1f)
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(200)
                            .start()

                        timer = object : CountDownTimer(mills + 900, 100) {
                            override fun onTick(millisUntilFinished: Long) {

                                val hour1 = TimeUnit.MILLISECONDS.toHours(millisUntilFinished)
                                val min1 = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60
                                val sec1 =
                                    TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60 % 60

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
                                    timerText.text = "$hour2:$min2:$sec2"
                                } else {
                                    timerText.text = "$min2:$sec2"
                                }
                                progressBar.progress = millisUntilFinished.toInt()
                            }

                            override fun onFinish() {

                                if (mMediaPlayer == null) {
                                    mMediaPlayer =
                                        MediaPlayer.create(this@MeditationActivity, R.raw.end_sound)
                                    mMediaPlayer!!.isLooping = false
                                    mMediaPlayer!!.start()
                                } else mMediaPlayer!!.start()

                                progressBar.progress = 0

                                timerText.animate()
                                    .scaleY(0f)
                                    .scaleX(0f)
                                    .alpha(0f)
                                    .setDuration(100)
                                    .withEndAction {
                                        timerText.text = "Good work"
                                        timerText.animate()
                                            .scaleY(1f)
                                            .scaleX(1f)
                                            .alpha(1f)
                                            .setDuration(200)
                                            .start()
                                    }
                                    .start()

                                sessionCounter(mills)


                                timer = object : CountDownTimer(5000, 5000) {
                                    override fun onTick(millisUntilFinished: Long) {}

                                    override fun onFinish() {
                                        startActivity(
                                            Intent(
                                                this@MeditationActivity,
                                                MainActivity::class.java
                                            )
                                        )
                                        overridePendingTransition(
                                            androidx.appcompat.R.anim.abc_fade_in,
                                            androidx.appcompat.R.anim.abc_fade_out
                                        )
                                    }
                                }
                                timer.start()

                            }
                        }
                        timer.start()

                    }
                    .start()


            }
        }
        timer.start()

    }

    override fun onStop() {
        super.onStop()
        if (mMediaPlayer != null) {
            mMediaPlayer!!.release()
            mMediaPlayer = null
        }
    }

    private fun sessionCounter(timer: Long) {
        Observable.fromCallable {

            val db = AppDatabase.getAppDatabase(applicationContext)

            cardDao = db!!.cardDao()

            val stats: Statistic = cardDao.getStats().first()

            val from = LocalDate.parse(stats.lastDayOfUse, DateTimeFormatter.ofPattern("ddMMyyyy"))
            val today = LocalDate.now()
            val period = Period.between(from, today)

            cardDao.updateStatistic(
                stats.copy(
                    counter = (stats.counter + timer),
                    lastDayOfUse = today.format(DateTimeFormatter.ofPattern("ddMMyyyy")).toString()
                )
            )

            if ((period.days == 1 && period.months == 0 && period.years == 0) || stats.streak == 0) {
                cardDao.updateStatistic(stats.copy(streak = stats.streak + 1))
                Log.e("TAG", "MeditActivity - streak + 1")
                Log.e("TAG", "Streak - ${stats.streak}")
                Log.e("TAG", "last use - ${from}")
            }


        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe()
    }

}