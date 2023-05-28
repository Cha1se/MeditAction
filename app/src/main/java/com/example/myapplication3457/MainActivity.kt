package com.example.myapplication3457

import android.app.Dialog
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.iterator
import androidx.core.view.setPadding
import androidx.fragment.app.FragmentContainerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.RequestOptions
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import jp.wasabeef.glide.transformations.RoundedCornersTransformation
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {

    private lateinit var mainLay: ConstraintLayout
    private lateinit var chooseLay: ConstraintLayout
    private lateinit var helloText: TextView
    private lateinit var editCardText: TextView
    private lateinit var scrollLay: LinearLayout
    private lateinit var editCardLay: ConstraintLayout
    private lateinit var editCardImg: ImageView
    private lateinit var countTimer: TextView
    private lateinit var streakText: TextView
    private lateinit var scrollView: ScrollView
    private lateinit var musicLay: ConstraintLayout
    private lateinit var aboutBtn: ImageView

    private lateinit var fragmentCreateCard: FragmentContainerView

    private lateinit var cardDao: CardDAO

    private var widthScreen: Int = 0
    private var heightScreen: Int = 0

    private var isCheckEditCard = false

    private val Int.dpToPx: Int
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
        scrollView = findViewById(R.id.scrollView)
        musicLay = findViewById(R.id.musicLayout)
        aboutBtn = findViewById(R.id.about_button)
        fragmentCreateCard = findViewById(R.id.fragmentCreateCard)

        editCardLay.setOnClickListener {
            addCard()
            // add scroll to down
            scrollView.postDelayed({
                scrollView.smoothScrollTo(0, scrollView.height)
            }, 200)
        }

        musicLay.setOnClickListener {
            supportFragmentManager.beginTransaction()
                .setCustomAnimations(
                    androidx.appcompat.R.anim.abc_slide_in_bottom,
                    androidx.appcompat.R.anim.abc_slide_out_bottom
                )
                .replace(R.id.fragmentCreateCard, MusicListFragment())
                .commit()
        }

        aboutBtn.setOnClickListener {

            val dialog = Dialog(this)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.fragment_about)
            dialog.setTitle("About")

            dialog.show()

        }

        changeWelcomeText()
        onFirstStartShowDeaflultCards()
    }

    fun onFirstStartShowDeaflultCards() {
        val isFirstRun = getSharedPreferences("PREFERENCE", MODE_PRIVATE)
            .getBoolean("isFirstRun", true)

        if (isFirstRun) {
            addDeafultCardsToDB()
        } else {
            showCards()
            showCounterSession()
        }

        getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit()
            .putBoolean("isFirstRun", false).commit()
    }

    fun addDeafultCardsToDB() {
        Observable.fromCallable {

            var db = AppDatabase.getAppDatabase(applicationContext)

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

        }.subscribeOn(Schedulers.io())
            .doOnComplete {
                showCards()
                showCounterSession()
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe()
    }


    private fun changeWelcomeText() {
        val dateFormat =
            SimpleDateFormat("HH") // Утро - 06-12, день - 12-18, вечер - 18-00, ночь - 00-06
        val date = dateFormat.format(Date())

        if (date.toString().toInt() in 6..12) {
            helloText.text = "Good morning!"
        } else if (date.toString().toInt() in 12..18) {
            helloText.text = "Good afternoon!"
        } else if (date.toString().toInt() in 18..23) {
            helloText.text = "Good evening!"
        } else if (date.toString().toInt() in 1..6) {
            helloText.text = "Good night!"
        } else {
            helloText.text = "Good night!"
        }
    }

    private fun showCounterSession() {

        lateinit var stats: Statistic

        Observable.fromCallable {

            // main
            val db = AppDatabase.getAppDatabase(applicationContext)

            cardDao = db!!.cardDao()

            stats = cardDao.getStats().last()

            if (stats.lastDayOfUse == "none") {
                cardDao.updateStatistic(
                    stats.copy(
                        lastDayOfUse = LocalDate.now()
                            .format(DateTimeFormatter.ofPattern("ddMMyyyy")).toString()
                    )
                )

                stats = cardDao.getStats().last()
            }

            val from = LocalDate.parse(stats.lastDayOfUse, DateTimeFormatter.ofPattern("ddMMyyyy"))
            // get today's date
            val today = LocalDate.now()
            // calculate the period between those two
            val period = Period.between(from, today)
            // and print it in a human-readable way

            if (period.days >= 2) {
                cardDao.updateStatistic(stats.copy(streak = 0))
                Log.e("TAG", "MeditActivity - Period is big")
                Log.e("TAG", "Streak - ${stats.streak}")
            }
            Log.e("TAG", "Streak - ${stats.streak}")

        }.doOnError { Log.e("ERROR", it.message.toString()) }
            .subscribeOn(Schedulers.io())
            .doOnError { showCounterSession() }
            .observeOn(AndroidSchedulers.mainThread())
            .doOnComplete {

                // Translate mills to hour and minutes

                val hour1 = TimeUnit.MILLISECONDS.toHours(stats.counter)
                val min1 = TimeUnit.MILLISECONDS.toMinutes(stats.counter) % 60

                var min2 = min1.toString()
                var hour2 = hour1.toString()

                if (min1 < 10) {
                    min2 = "0$min1"
                }
                if (hour1 < 10) {
                    hour2 = "0$hour1"
                }
                if (hour1 > 0) {
                    countTimer.text = "${hour2}h ${min2}m"
                } else {
                    countTimer.text = "${min2}m"
                }

                streakText.text = stats.streak.toString() + "d"

            }
            .subscribe()
    }



    private fun showCards() {

        var img: Int?
        var name: String?
        var timer: String?
        var id: Int?
        lateinit var cards: List<Card>

        Observable.fromCallable {

            val db = AppDatabase.getAppDatabase(applicationContext)
            cardDao = db!!.cardDao()
            cards = cardDao.getCards()

        }.doOnError { Log.e("ERROR", it.message.toString()) }
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
                    createCard(
                        id = id!!,
                        cardImg = img!!,
                        titleText = name!!,
                        timer = timer!!,
                        cardImgUri = card.background
                    )
                }

            }
            .subscribe()
    }

    private fun addCard() {

        val lastLayout: LinearLayout = getViews(scrollLay).last() as LinearLayout

        if (!isCheckEditCard) {

            editCardLay.animate()
                .scaleY(0.9f)
                .scaleX(0.9f)
                .alpha(0.5f)
                .setDuration(100)
                .withEndAction {
                    Runnable {

                        editCardLay.setBackgroundResource(R.drawable.xml_card_checked)
                        editCardText.setTextColor(Color.WHITE)
                        editCardImg.setImageResource(R.drawable.ico_edit2)

                        editCardLay.animate()
                            .scaleY(1f)
                            .scaleX(1f)
                            .alpha(1f)
                            .setDuration(100)
                            .start()
                    }.run()
                }
                .start()

            if (lastLayout.childCount < 2) { // Если в последнем LinearLayout еще есть место для карточки то

                val cardsLayout = LinearLayout(this)

                scrollLay.addView(
                    cardsLayout,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                (cardsLayout.layoutParams as LinearLayout.LayoutParams).topMargin = 8.dpToPx
                (cardsLayout.layoutParams as LinearLayout.LayoutParams).bottomMargin = 16.dpToPx

                // Create Card
                val saveCard = ConstraintLayout(this)

                saveCard.id = ConstraintLayout.generateViewId()

                saveCard.setBackgroundResource(R.drawable.xml_15_broders_add_card)

                // Add Card
                saveCard.alpha = 0f
                cardsLayout.addView(saveCard)

                (saveCard.layoutParams as LinearLayout.LayoutParams).height = 130.dpToPx
                (saveCard.layoutParams as LinearLayout.LayoutParams).weight = 1f
                (saveCard.layoutParams as LinearLayout.LayoutParams).leftMargin = 8.dpToPx
                (saveCard.layoutParams as LinearLayout.LayoutParams).rightMargin = 8.dpToPx
                saveCard.setPadding(15.dpToPx)
                cardsLayout.requestLayout()

                // OnClick Linstener
                saveCard.setOnClickListener {

                    // Show Fragment
                    supportFragmentManager.beginTransaction()
                        .setCustomAnimations(
                            androidx.appcompat.R.anim.abc_slide_in_bottom,
                            androidx.appcompat.R.anim.abc_slide_out_bottom
                        )
                        .replace(R.id.fragmentCreateCard, CardCreateFragment())
                        .commit()
                    intent = Intent(this, CardCreateFragment::class.java)
                    intent.putExtra("ID", -1)

                    saveCard.animate()
                        .scaleX(0.9f)
                        .scaleY(0.9f)
                        .setDuration(100)
                        .withEndAction {
                            Runnable {
                                saveCard.animate()
                                    .scaleY(1f)
                                    .scaleX(1f)
                                    .setDuration(100)
                                    .start()
                            }.run()
                        }
                        .start()

                    editCardLay.callOnClick()

                }

                // add ico
                val backgroundCard = ImageView(this)

                backgroundCard.id = ImageView.generateViewId()
                backgroundCard.setImageResource(R.drawable.ico_add)

                saveCard.addView(
                    backgroundCard,
                    ConstraintLayout.LayoutParams.MATCH_PARENT,
                    ConstraintLayout.LayoutParams.WRAP_CONTENT
                )

                if (cardsLayout.childCount == 1) {
                    saveCard.translationX = -150.dpToPx.toFloat()
                    saveCard.animate()
                        .translationX(0f)
                        .alpha(1f)
                        .setDuration(200)
                        .start()
                } else {
                    saveCard.translationX = 150.dpToPx.toFloat()
                    saveCard.animate()
                        .translationX(0f)
                        .alpha(1f)
                        .setDuration(200)
                        .start()
                }

                scrollLay.requestLayout()
                isCheckEditCard = !isCheckEditCard

                val allViews = getViews(scrollLay) as List<LinearLayout>
                allViews.drop(allViews.lastIndex)
                var inter = 0
                for (i in allViews) {
                    for (j in allViews[inter]) {
                        ((j as ConstraintLayout).getChildAt(j.childCount - 1) as ImageView).visibility =
                            View.VISIBLE
                    }
                    inter++
                }

            } else { // Если нету места создать новую

                // Создаем Layout для хранения карточек
                val cardsLayout = LinearLayout(this)

                scrollLay.addView(
                    cardsLayout,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )

                // Restart function
                addCard()
            }
        } else {

            editCardLay.animate()
                .scaleY(0.9f)
                .scaleX(0.9f)
                .alpha(0.5f)
                .setDuration(100)
                .withEndAction {
                    Runnable {

                        editCardLay.setBackgroundResource(R.drawable.xml_card_uncheck)
                        editCardText.setTextColor(Color.parseColor("#749FFB"))
                        editCardImg.setImageResource(R.drawable.ico_edit)

                        editCardLay.animate()
                            .scaleY(1f)
                            .scaleX(1f)
                            .alpha(1f)
                            .setDuration(100)
                            .start()
                    }.run()
                }
                .start()

            val cardLay = lastLayout.getChildAt(lastLayout.childCount - 1)

            if (lastLayout.childCount == 1) {

                cardLay.animate()
                    .translationX(-150.dpToPx.toFloat())
                    .alpha(0f)
                    .setDuration(200)
                    .withEndAction {
                        Runnable {
                            scrollLay.removeView(lastLayout)
                        }.run()
                    }
                    .start()
            } else {
                cardLay.animate()
                    .translationX(150.dpToPx.toFloat())
                    .alpha(0f)
                    .setDuration(200)
                    .withEndAction {
                        Runnable {
                            scrollLay.removeView(lastLayout)
                        }.run()
                    }
                    .start()
            }

            scrollLay.requestLayout()
            isCheckEditCard = !isCheckEditCard

            val allViews = getViews(scrollLay) as List<LinearLayout>
            allViews.drop(allViews.lastIndex)
            var inter = 0
            for (i in allViews) {
                for (j in allViews[inter]) {
                    ((j as ConstraintLayout).getChildAt(j.childCount - 1) as ImageView).visibility =
                        View.INVISIBLE
                }
                inter++
            }

        }
    }

    private fun clearLayout() {
        scrollLay.removeViews(1, scrollLay.childCount - 1)
        (scrollLay.getChildAt(0) as LinearLayout).removeAllViews()
        showCards()
    }

    private fun createCard(
        id: Int,
        cardImg: Int = 0,
        titleText: String?,
        timer: String?,
        cardImgUri: String = ""
    ) {

        val lastLayout: LinearLayout = getViews(scrollLay).last() as LinearLayout

        (lastLayout.layoutParams as LinearLayout.LayoutParams).topMargin = 8.dpToPx
        (lastLayout.layoutParams as LinearLayout.LayoutParams).bottomMargin = 8.dpToPx

        if (lastLayout.childCount < 2) { // Если в последнем LinearLayout еще есть место для карточки то

            // Create Card
            val sampleCard = ConstraintLayout(this)

            sampleCard.id = ConstraintLayout.generateViewId()

            // Add Card

            sampleCard.alpha = 0f

            lastLayout.addView(sampleCard)

            (sampleCard.layoutParams as LinearLayout.LayoutParams).width = 160.dpToPx
            (sampleCard.layoutParams as LinearLayout.LayoutParams).height = 130.dpToPx
            (sampleCard.layoutParams as LinearLayout.LayoutParams).weight = 1f
            (sampleCard.layoutParams as LinearLayout.LayoutParams).leftMargin = 8.dpToPx
            (sampleCard.layoutParams as LinearLayout.LayoutParams).rightMargin = 8.dpToPx
            lastLayout.requestLayout()

            // OnClick Linstener

            // Image Background on card
            val backgroundCard = ImageView(this)
            backgroundCard.id = ImageView.generateViewId()

            // Rounded corners and CenterCrop image, background image for card
            val multi = MultiTransformation(
                CenterCrop(),
                RoundedCornersTransformation(
                    16.dpToPx,
                    0,
                    RoundedCornersTransformation.CornerType.ALL
                )
            )

            Glide.with(this).load(cardImg)
                .apply(RequestOptions.bitmapTransform(multi))
                .apply(RequestOptions.skipMemoryCacheOf(true))
                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                .error(Uri.parse(cardImgUri))
                .into(backgroundCard)

            sampleCard.addView(
                backgroundCard,
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.MATCH_PARENT
            )

            // Create Title Card
            val titleCard = ConstraintLayout(this)
            titleCard.id = ConstraintLayout.generateViewId()

            // Set transparency background color
            titleCard.setBackgroundResource(R.drawable.xml_title15dp)
            titleCard.background.setTint(Color.argb(200, 241, 186, 255))

            sampleCard.addView(titleCard, 90.dpToPx, 40.dpToPx)

            val constraintSetTitle = ConstraintSet()
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
            val textTitle = TextView(this)

            textTitle.id = TextView.generateViewId()
            textTitle.textSize = 20f
            textTitle.typeface = ResourcesCompat.getFont(this, R.font.kumbh_sans_regular)
            textTitle.setTextColor(Color.rgb(255, 255, 255))
            textTitle.text = titleText!!

            titleCard.addView(textTitle)

            // Match Parent for center titleCard
            val constraintSetText = ConstraintSet()
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


            // EditCard

            val editCard = ImageView(this)
            editCard.id = ImageView.generateViewId()
            editCard.setImageResource(R.drawable.ico_edit2)
            editCard.visibility = View.INVISIBLE
            sampleCard.addView(editCard, 50.dpToPx, 50.dpToPx)

            val constraintSetEdit = ConstraintSet()
            constraintSetEdit.clone(sampleCard)
            constraintSetEdit.connect(
                editCard.id, ConstraintSet.TOP,
                sampleCard.id, ConstraintSet.TOP
            )
            constraintSetEdit.connect(
                editCard.id, ConstraintSet.BOTTOM,
                sampleCard.id, ConstraintSet.BOTTOM
            )
            constraintSetEdit.connect(
                editCard.id, ConstraintSet.LEFT,
                sampleCard.id, ConstraintSet.LEFT
            )
            constraintSetEdit.connect(
                editCard.id, ConstraintSet.RIGHT,
                sampleCard.id, ConstraintSet.RIGHT
            )
            constraintSetEdit.applyTo(sampleCard)

            // OnClick
            sampleCard.setOnClickListener {

                if (!isCheckEditCard) {

                    intent = Intent(this, MeditationActivity::class.java)
                    intent.putExtra("id", id)

                    startActivity(intent)
                    overridePendingTransition(
                        androidx.appcompat.R.anim.abc_fade_in,
                        androidx.appcompat.R.anim.abc_fade_out
                    )

                } else {
                    // todo editCard
                    editCard(id)

                }
            }


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


            scrollLay.requestLayout()

        } else { // Если нету места создать новую

            // Создаем Layout для хранения карточек
            val cardsLayout = LinearLayout(this)

            scrollLay.addView(
                cardsLayout,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            (cardsLayout.layoutParams as LinearLayout.LayoutParams).topMargin = 12.dpToPx

            // Restart function
            createCard(id, cardImg, titleText, timer, cardImgUri)
        }

    }

    private fun editCard(id: Int) {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                androidx.appcompat.R.anim.abc_slide_in_bottom,
                androidx.appcompat.R.anim.abc_slide_out_bottom
            )
            .replace(R.id.fragmentCreateCard, CardCreateFragment())
            .commit()
        intent = Intent(this, CardCreateFragment::class.java)
        intent.putExtra("ID", id)

        editCardLay.callOnClick()
    }

    private fun getViews(layout: ViewGroup): List<View> {
        val views: MutableList<View> = ArrayList()
        for (i in 0 until layout.childCount) {
            views.add(layout.getChildAt(i))
        }
        return views
    }

}