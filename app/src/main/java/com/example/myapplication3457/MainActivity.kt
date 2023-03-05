package com.example.myapplication3457

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.*
import android.os.Bundle
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
import androidx.core.view.setPadding
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.RequestOptions
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import jp.wasabeef.glide.transformations.RoundedCornersTransformation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.sql.Time
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.Period
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity() {

    lateinit var mainLay: ConstraintLayout
    lateinit var chooseLay: ConstraintLayout
    lateinit var helloText: TextView
    lateinit var editCardText: TextView
    lateinit var scrollLay: LinearLayout
    lateinit var editCardLay: ConstraintLayout
    lateinit var editCardImg: ImageView
    lateinit var chooseLayout: ConstraintLayout
    lateinit var countTimer: TextView
    lateinit var streakText: TextView

    var widthScreen: Int = 0
    var heightScreen: Int = 0

    private lateinit var cardDao: CardDAO
    var isCheckEditCard = false

    val Int.dpToPx: Int
        get() = (this * Resources.getSystem().displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        widthScreen = this.resources.displayMetrics.widthPixels
        heightScreen = this.resources.displayMetrics.heightPixels

        mainLay = findViewById(R.id.mainLayout)
        chooseLay = findViewById(R.id.chooseLayout)
        helloText = findViewById(R.id.helloText)
        scrollLay = findViewById(R.id.scrollLayout)
        editCardLay = findViewById(R.id.editCardsLayout)
        editCardText = findViewById(R.id.editCardText)
        editCardImg = findViewById(R.id.editCardImg)
        countTimer = findViewById(R.id.counterText)
        streakText = findViewById(R.id.streakText)

        editCardLay.setOnClickListener{
            if (!isCheckEditCard) {

                editCardLay.animate()
                    .scaleY(0.9f)
                    .scaleX(0.9f)
                    .alpha(0.5f)
                    .setDuration(100)
                    .withEndAction { Runnable {

                        it.setBackgroundResource(R.drawable.xml_card_checked)
                        editCardText.setTextColor(Color.WHITE)
                        editCardImg.setImageResource(R.drawable.ico_edit2)

                        editCardLay.animate()
                            .scaleY(1f)
                            .scaleX(1f)
                            .alpha(1f)
                            .setDuration(100)
                            .start()
                    }.run() }
                    .start()

                AddCard()
                isCheckEditCard = !isCheckEditCard
            } else {

                editCardLay.animate()
                    .scaleY(0.9f)
                    .scaleX(0.9f)
                    .alpha(0.5f)
                    .setDuration(100)
                    .withEndAction { Runnable {

                        it.setBackgroundResource(R.drawable.xml_card_uncheck)
                        editCardText.setTextColor(Color.parseColor("#749FFB"))
                        editCardImg.setImageResource(R.drawable.ico_edit)

                        editCardLay.animate()
                            .scaleY(1f)
                            .scaleX(1f)
                            .alpha(1f)
                            .setDuration(100)
                            .start()
                    }.run() }
                    .start()

                AddCard()
                isCheckEditCard = !isCheckEditCard
            }

        }


        ChangeWelcomeText()

//        DefaultValuesDB()

        ShowCards()

        ShowCounterSession()

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

    fun ShowCounterSession() {

        lateinit var stats: Statistic

        Observable.fromCallable {

            // main
            var db = AppDatabase.getAppDatabase(applicationContext)

            cardDao = db!!.cardDao()

            stats = cardDao.getStats().first()

            if (stats.lastDayOfUse == "none") {
                cardDao.updateStatistic(stats.copy(lastDayOfUse = LocalDate.now().format(DateTimeFormatter.ofPattern("ddMMyyyy")).toString()))
            }

            val from = LocalDate.parse(stats.lastDayOfUse, DateTimeFormatter.ofPattern("ddMMyyyy"))
            // get today's date
            val today = LocalDate.now()
            // calculate the period between those two
            var period = Period.between(from, today)
            // and print it in a human-readable way

            if (period.days > 1 && period.months >= 0 && period.years >= 0) {

                cardDao.updateStatistic(stats.copy(streak = 0))

            }

        }.doOnError({Log.e("ERROR", it.message.toString())})
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnComplete {

                // Translate mills to hour and minutes

                var hour1 = TimeUnit.MILLISECONDS.toHours(stats.counter)
                var min1 = TimeUnit.MILLISECONDS.toMinutes(stats.counter) % 60

                var min2 = min1.toString()
                var hour2 = hour1.toString()

                if (min1 < 10) {
                    min2 = "0$min1"
                }
                if (hour1 < 10) {
                    hour2 = "0$hour1"
                }
                if (hour1 > 0) {
                    countTimer.setText("${hour2}h ${min2}m")
                } else {
                    countTimer.setText("${min2}m")
                }

                streakText.setText(stats.streak.toString() + "d")

            }
            .subscribe()
    }

    fun ShowCards() {

        var img: Int? = null
        var name: String? = null
        var timer: String? = null
        var id: Int? = null
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
                    id = card.id
                    img = application.resources.getIdentifier(
                        card.background,
                        "drawable",
                        applicationContext.packageName
                    )
                    name = card.cardName
                    timer = card.timer
                    CreateCard(id!!, img!!, name!!, timer!!)
                }
            }
            .subscribe()
    }

    fun DefaultValuesDB() {

        // Create Sample Cards

        Observable.fromCallable {

            // main
            var db = AppDatabase.getAppDatabase(applicationContext)

            cardDao = db!!.cardDao()

            cardDao.insertStatistic(Statistic(id = 0, counter = 0, streak = 0, lastDayOfUse = "04032023"))

//            cardDao.deleteAllCards()

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

        }.doOnError({Log.e("ERROR", it.message.toString())})
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnComplete {

            }
            .subscribe()
    }

    fun AddCard() {
        var lastLayout: LinearLayout = getViews(scrollLay).last() as LinearLayout

        (lastLayout.layoutParams as LinearLayout.LayoutParams).topMargin = 8.dpToPx
        (lastLayout.layoutParams as LinearLayout.LayoutParams).bottomMargin = 8.dpToPx

        if (!isCheckEditCard) {

            if (lastLayout.childCount < 2) { // Если в последнем LinearLayout еще есть место для карточки то

                var cardsLayout = LinearLayout(this)

                scrollLay.addView(
                    cardsLayout,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                (cardsLayout.layoutParams as LinearLayout.LayoutParams).topMargin = 8.dpToPx
                (cardsLayout.layoutParams as LinearLayout.LayoutParams).bottomMargin = 16.dpToPx

                // Create Card
                var addCard = ConstraintLayout(this)

                addCard.id = ConstraintLayout.generateViewId()

                addCard.setBackgroundResource(R.drawable.xml_15_broders_add_card)

                // Add Card

                addCard.alpha = 0f
                cardsLayout.addView(addCard)

//                (addCard.layoutParams as LinearLayout.LayoutParams).width = (widthScreen / 2) - 32.dpToPx
                (addCard.layoutParams as LinearLayout.LayoutParams).height = 130.dpToPx
                (addCard.layoutParams as LinearLayout.LayoutParams).weight = 1f
                (addCard.layoutParams as LinearLayout.LayoutParams).leftMargin = 8.dpToPx
                (addCard.layoutParams as LinearLayout.LayoutParams).rightMargin = 8.dpToPx
                addCard.setPadding(15.dpToPx)
                cardsLayout.requestLayout()

                // OnClick Linstener
                addCard.setOnClickListener {

                    //

                }

                // add ico
                var backgroundCard = ImageView(this)

                backgroundCard.id = ImageView.generateViewId()
                backgroundCard.setImageResource(R.drawable.ico_add)

                addCard.addView(
                    backgroundCard,
                    ConstraintLayout.LayoutParams.MATCH_PARENT,
                    ConstraintLayout.LayoutParams.WRAP_CONTENT
                )

                if (cardsLayout.childCount == 1) {
                    addCard.translationX = -150.dpToPx.toFloat()
                    addCard.animate()
                        .translationX(0f)
                        .alpha(1f)
                        .setDuration(200)
                        .start()
                } else {
                    addCard.translationX = 150.dpToPx.toFloat()
                    addCard.animate()
                        .translationX(0f)
                        .alpha(1f)
                        .setDuration(200)
                        .start()
                }


            } else { // Если нету места создать новую

                // Создаем Layout для хранения карточек
                var cardsLayout = LinearLayout(this)

                scrollLay.addView(
                    cardsLayout,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                (cardsLayout.layoutParams as LinearLayout.LayoutParams).topMargin = 8.dpToPx
                (cardsLayout.layoutParams as LinearLayout.LayoutParams).bottomMargin = 16.dpToPx

                // Restart function
                AddCard()
            }
        } else {

            var cardLay = lastLayout.getChildAt(lastLayout.childCount-1)

            if (lastLayout.childCount == 1) {

                cardLay.animate()
                    .translationX(-150.dpToPx.toFloat())
                    .alpha(0f)
                    .setDuration(200)
                    .withEndAction { Runnable {
                        scrollLay.removeView(lastLayout)
                    }.run() }
                    .start()
            } else {
                cardLay.animate()
                    .translationX(150.dpToPx.toFloat())
                    .alpha(0f)
                    .setDuration(200)
                    .withEndAction { Runnable {
                        scrollLay.removeView(lastLayout)
                    }.run() }
                    .start()
            }

        }
    }

    fun CreateCard(id: Int, cardImg: Int?, titleText: String?, timer: String?) {

        var lastLayout: LinearLayout = getViews(scrollLay).last() as LinearLayout

        (lastLayout.layoutParams as LinearLayout.LayoutParams).topMargin = 8.dpToPx
        (lastLayout.layoutParams as LinearLayout.LayoutParams).bottomMargin = 8.dpToPx

        if (lastLayout.childCount < 2) { // Если в последнем LinearLayout еще есть место для карточки то

            // Create Card
            var sampleCard = ConstraintLayout(this)

            sampleCard.id = ConstraintLayout.generateViewId()

            // Add Card

            sampleCard.alpha = 0f

            lastLayout.addView(sampleCard)

            (sampleCard.layoutParams as LinearLayout.LayoutParams).width = 160.dpToPx
            (sampleCard.layoutParams as LinearLayout.LayoutParams).height = 130.dpToPx
            (sampleCard.layoutParams as LinearLayout.LayoutParams).weight = 1f
            (sampleCard.layoutParams as LinearLayout.LayoutParams).leftMargin =  8.dpToPx
            (sampleCard.layoutParams as LinearLayout.LayoutParams).rightMargin = 8.dpToPx
            lastLayout.requestLayout()

            // OnClick Linstener
            sampleCard.setOnClickListener {

                intent = Intent(this, MeditationActivity::class.java)
                intent.putExtra("id", id)

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
            Glide.with(this).load(cardImg!!)
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
            textTitle.text = titleText!!

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

            if (lastLayout.childCount == 1) {
                sampleCard.translationX = -150.dpToPx.toFloat()
                sampleCard.animate()
                    .translationX(0f)
                    .alpha(1f)
                    .setDuration(500)
                    .start()
            } else {
                sampleCard.translationX = 150.dpToPx.toFloat()
                sampleCard.animate()
                    .translationX(0f)
                    .alpha(1f)
                    .setDuration(500)
                    .start()
            }


        } else { // Если нету места создать новую

            // Создаем Layout для хранения карточек
            var cardsLayout = LinearLayout(this)

            scrollLay.addView(cardsLayout, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            (cardsLayout.layoutParams as LinearLayout.LayoutParams).topMargin = 12.dpToPx

            // Restart function
            CreateCard(id, cardImg, titleText, timer)
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