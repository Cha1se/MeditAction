package com.example.myapplication3457

import android.content.Intent
import android.content.res.Resources
import android.graphics.*
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.res.ResourcesCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.RequestOptions
import jp.wasabeef.glide.transformations.RoundedCornersTransformation
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {

    lateinit var mainLay: ConstraintLayout
    lateinit var chooseLay: ConstraintLayout
    lateinit var helloText: TextView
    lateinit var scrollLay: LinearLayout

    val Int.dpToPx: Int
        get() = (this * Resources.getSystem().displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mainLay = findViewById(R.id.mainLayout)
        chooseLay = findViewById(R.id.chooseLayout)
        helloText = findViewById(R.id.helloText)
        scrollLay = findViewById(R.id.scrollLayout)

        ChangeWelcomeText()

        DefaultCards()

    }

    fun ChangeWelcomeText() {
        var dateFormat = SimpleDateFormat("HH") // Утро - 06-12, день - 12-18, вечер - 18-00, ночь - 00-06
        var date = dateFormat.format(Date())
        if (date.toString().toInt() in 6..12) {
            helloText.setText("Good morning!")
        } else if (date.toString().toInt() in 12..18) {
            helloText.setText("Good afternoon!")
        } else if (date.toString().toInt() in 18..23) {
            helloText.setText("Good evening!")
        } else if (date.toString().toInt() in 1..6) {
            helloText.setText("Good night!")
        } else {
            helloText.setText("Good night!")
        }
    }

    fun DefaultCards() {
        // Create Sample Cards

        CreateCard(R.drawable.img_sample_background_card, "Relax", "00:15:00")
        CreateCard(R.drawable.img_sleep_sample_card, "Sleep", "00:10:00")
        CreateCard(R.drawable.img_stress_sample_card, "Stress", "00:20:00")
        CreateCard(R.drawable.img_timer_sample_card, "Timer", "00:20:00")
    }

    fun CreateCard(cardImg: Int, titleText: String, timer: String) {

        var lastLayout: LinearLayout = getViews(scrollLay).last() as LinearLayout

        (lastLayout.layoutParams as LinearLayout.LayoutParams).topMargin = 8.dpToPx
        (lastLayout.layoutParams as LinearLayout.LayoutParams).bottomMargin = 8.dpToPx

        if (lastLayout.childCount < 2) { // Если в последнем LinearLayout еще есть место для карточки то

            // Create Card

            var sampleCard = ConstraintLayout(this)

            sampleCard.id = ConstraintLayout.generateViewId()

            // Add Card
            lastLayout.addView(sampleCard)

            (sampleCard.layoutParams as LinearLayout.LayoutParams).width = 160.dpToPx
            (sampleCard.layoutParams as LinearLayout.LayoutParams).height = 130.dpToPx
            (sampleCard.layoutParams as LinearLayout.LayoutParams).weight = 1f
            (sampleCard.layoutParams as LinearLayout.LayoutParams).leftMargin = 16.dpToPx
            (sampleCard.layoutParams as LinearLayout.LayoutParams).rightMargin = 16.dpToPx
            lastLayout.requestLayout()

            // OnClick Linstener
            sampleCard.setOnClickListener {

                var formatter = DateTimeFormatter.ISO_LOCAL_TIME
                var time = LocalTime.parse(timer, formatter)
                var timerMills: Long = ((time.hour * 3600000) + (time.minute * 60000) + (time.second * 1000)).toLong()

                intent = Intent(this, MeditationActivity::class.java)
                intent.putExtra("timer", timerMills)
                intent.putExtra("titleName", titleText)

                startActivity(intent)
                overridePendingTransition(androidx.appcompat.R.anim.abc_fade_in, androidx.appcompat.R.anim.abc_fade_out);
            }

            // Image Background on card
            var backgroundCard = ImageView(this)

            backgroundCard.id = ImageView.generateViewId()

            // Rounded corners and CenterCrop image, background image for card
            val multi = MultiTransformation<Bitmap>(
                CenterCrop(),
                RoundedCornersTransformation(16.dpToPx, 0, RoundedCornersTransformation.CornerType.ALL)
            )
            Glide.with(this).load(cardImg)
                .apply(RequestOptions.bitmapTransform(multi))
                .into(backgroundCard)

            sampleCard.addView(backgroundCard, ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT)

            // Create Title Card
            var titleCard = ConstraintLayout(this)

            titleCard.id = ConstraintLayout.generateViewId()

            // Set transparency background color
            titleCard.setBackgroundResource(R.drawable.xml_title15dp)
            titleCard.background.setTint(Color.argb(200, 241, 186, 255))

            sampleCard.addView(titleCard, 90.dpToPx, 40.dpToPx)

            var constraintSetTitle = ConstraintSet()
            constraintSetTitle.clone(sampleCard)
            constraintSetTitle.connect(
                titleCard.id, ConstraintSet.TOP,
                sampleCard.id, ConstraintSet.TOP
            )
            constraintSetTitle.connect(
                titleCard.id, ConstraintSet.LEFT,
                sampleCard.id, ConstraintSet.LEFT
            )
            constraintSetTitle.applyTo(sampleCard)

            // Text in the title
            var textTitle = TextView(this)

            textTitle.id = TextView.generateViewId()
            textTitle.textSize = 20f
            textTitle.typeface = ResourcesCompat.getFont(this, R.font.kumbh_sans_regular)
            textTitle.setTextColor(Color.rgb(255,255,255))
            textTitle.text = titleText

            titleCard.addView(textTitle)

            // Match Parent for center titleCard
            var constraintSetText = ConstraintSet()
            constraintSetText.clone(titleCard)
            constraintSetText.connect(
                textTitle.id, ConstraintSet.TOP,
                titleCard.id, ConstraintSet.TOP
            )
            constraintSetText.connect(
                textTitle.id, ConstraintSet.BOTTOM,
                titleCard.id, ConstraintSet.BOTTOM
            )
            constraintSetText.connect(
                textTitle.id, ConstraintSet.LEFT,
                titleCard.id, ConstraintSet.LEFT
            )
            constraintSetText.connect(
                textTitle.id, ConstraintSet.RIGHT,
                titleCard.id, ConstraintSet.RIGHT
            )
            constraintSetText.applyTo(titleCard)


        } else { // Если нету места создать новую

            // Создаем Layout для хранения карточек
            var cardsLayout = LinearLayout(this)

            scrollLay.addView(cardsLayout, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            (cardsLayout.layoutParams as LinearLayout.LayoutParams).topMargin = 12.dpToPx

            // Restart function
            CreateCard(cardImg, titleText, timer)
        }

    }

    fun getViews(layout: ViewGroup): List<View> {
        val views: MutableList<View> = ArrayList()
        for (i in 0 until layout.childCount) {
            views.add(layout.getChildAt(i))
        }
        return views
    }



}