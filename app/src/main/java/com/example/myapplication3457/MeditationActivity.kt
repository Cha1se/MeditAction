package com.example.myapplication3457

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import java.lang.Math.floor
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.TimeUnit


class MeditationActivity : AppCompatActivity() {

    lateinit var backBtn: ImageView
    lateinit var progressBar: ProgressBar
    lateinit var timerText: TextView
    lateinit var titleText: TextView
    lateinit var contentLay: ConstraintLayout

    private lateinit var timer: CountDownTimer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meditation)

        backBtn = findViewById(R.id.backArrow)
        progressBar = findViewById(R.id.progressBar)
        timerText = findViewById(R.id.timer)
        titleText = findViewById(R.id.titileMeditationText)
        contentLay = findViewById(R.id.contentLayout)

        titleText.text = intent.getStringExtra("titleName")

        backBtn.setOnClickListener {
            timer.cancel()
            startActivity(Intent(this, MainActivity::class.java))
            overridePendingTransition(androidx.appcompat.R.anim.abc_fade_in, androidx.appcompat.R.anim.abc_fade_out)
        }

        var timerTime: Long = intent.getLongExtra("timer", 1L)
        MeditationTimer(timerTime)

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
            }
        }
        timer.start()

    }

    override fun onBackPressed() {
        super.onBackPressed()
        timer.cancel()
    }

}