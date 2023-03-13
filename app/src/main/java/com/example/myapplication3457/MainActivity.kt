package com.example.myapplication3457

import android.app.TimePickerDialog
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.setPadding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.RequestOptions
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import jp.wasabeef.glide.transformations.RoundedCornersTransformation
import java.io.File
import java.io.FileDescriptor
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.util.*
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
    private lateinit var createCardLay: ConstraintLayout
    private lateinit var scrollView: ScrollView

    private lateinit var backButton: ImageView
    private lateinit var cardNameEdit: EditText
    private lateinit var chooseBackCard: ConstraintLayout
    private lateinit var chooseBackCardImg: ImageView
    private lateinit var showCardLayout: ConstraintLayout
    private lateinit var showCardImg: ImageView
    private lateinit var nameCardText: TextView
    private lateinit var timerTextChange: TextView
    private lateinit var changeTimerButton: Button
    private lateinit var saveButton: Button
    private lateinit var musicLay: ConstraintLayout

    private var uriImage: String = ""
    private lateinit var cardDao: CardDAO
    private lateinit var selectImageIntent: ActivityResultLauncher<String>
    
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
        createCardLay = findViewById(R.id.createCardLayout)
        scrollView = findViewById(R.id.scrollView)

        // Create card views
        backButton = findViewById(R.id.backBtn)
        cardNameEdit = findViewById(R.id.cardNameEdit)
        chooseBackCard = findViewById(R.id.chooseBackCard)
        chooseBackCardImg = findViewById(R.id.chooseBackCardImg)
        showCardLayout = findViewById(R.id.showCardLayout)
        showCardImg = findViewById(R.id.showCardImg)
        nameCardText = findViewById(R.id.nameCardText)
        timerTextChange = findViewById(R.id.timerTextChange)
        changeTimerButton = findViewById(R.id.changeTimerButton)
        saveButton = findViewById(R.id.saveButton)
        musicLay = findViewById(R.id.musicLayout)

        editCardLay.setOnClickListener{
            addCard()
            // add scroll to down
            scrollView.postDelayed({
                scrollView.smoothScrollTo(0, scrollView.height)
            }, 200)

        }

        changeWelcomeText()

        showCards()

        showCounterSession()

        selectImageIntent = registerForActivityResult(ActivityResultContracts.GetContent())
        { uri ->
            chooseBackCardImg.setPadding(0)
            showCardImg.setPadding(0)

            val multi = MultiTransformation(
                CenterCrop(),
                RoundedCornersTransformation(15.dpToPx, 0, RoundedCornersTransformation.CornerType.ALL)
            )
            Glide.with(this).load(uri)
                .apply(RequestOptions.bitmapTransform(multi))
                .into(chooseBackCardImg)

            Glide.with(this).load(uri)
                .apply(RequestOptions.bitmapTransform(multi))
                .into(showCardImg)
            uriImage = uri.toString()

        }

    }

    private fun changeWelcomeText() {
        val dateFormat = SimpleDateFormat("HH") // Утро - 06-12, день - 12-18, вечер - 18-00, ночь - 00-06
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

            while (cardDao.getStats().isEmpty()) {

            }

            stats = cardDao.getStats().last()

            if (stats.lastDayOfUse == "none") {
                cardDao.updateStatistic(stats.copy(lastDayOfUse = LocalDate.now().format(DateTimeFormatter.ofPattern("ddMMyyyy")).toString()))

                stats = cardDao.getStats().last()
            }

            val from = LocalDate.parse(stats.lastDayOfUse, DateTimeFormatter.ofPattern("ddMMyyyy"))
            // get today's date
            val today = LocalDate.now()
            // calculate the period between those two
            val period = Period.between(from, today)
            // and print it in a human-readable way

            if (period.days > 1 && period.months >= 0 && period.years >= 0) {
                cardDao.updateStatistic(stats.copy(streak = 0))
            }

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

            // main
            val db = AppDatabase.getAppDatabase(applicationContext)

            cardDao = db!!.cardDao()

            // Add cards in database and update / delete

            // put values in database

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
                    createCard(id = id!!, cardImg = img!!, titleText = name!!, timer = timer!!, cardImgUri = card.background)
                }

            }
            .subscribe()
    }

    private fun defaultValuesDB() {

        // Create Sample Cards

        Observable.fromCallable {

            // main
            val db = AppDatabase.getAppDatabase(applicationContext)

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

        }.doOnError { Log.e("ERROR", it.message.toString()) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
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
                .withEndAction { Runnable {

                    editCardLay.setBackgroundResource(R.drawable.xml_card_checked)
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
                val addCard = ConstraintLayout(this)

                addCard.id = ConstraintLayout.generateViewId()

                addCard.setBackgroundResource(R.drawable.xml_15_broders_add_card)

                // Add Card
                addCard.alpha = 0f
                cardsLayout.addView(addCard)

                (addCard.layoutParams as LinearLayout.LayoutParams).height = 130.dpToPx
                (addCard.layoutParams as LinearLayout.LayoutParams).weight = 1f
                (addCard.layoutParams as LinearLayout.LayoutParams).leftMargin = 8.dpToPx
                (addCard.layoutParams as LinearLayout.LayoutParams).rightMargin = 8.dpToPx
                addCard.setPadding(15.dpToPx)
                cardsLayout.requestLayout()

                // OnClick Linstener
                addCard.setOnClickListener {

                    addCard.animate()
                        .scaleX(0.9f)
                        .scaleY(0.9f)
                        .setDuration(100)
                        .withEndAction { Runnable {
                            addCard.animate()
                                .scaleY(1f)
                                .scaleX(1f)
                                .setDuration(100)
                                .start()
                        }.run() }
                        .start()
                    showcreateCard()

                }

                // add ico
                val backgroundCard = ImageView(this)

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

                scrollLay.requestLayout()
                isCheckEditCard = !isCheckEditCard

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
                .withEndAction { Runnable {

                    editCardLay.setBackgroundResource(R.drawable.xml_card_uncheck)
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

            val cardLay = lastLayout.getChildAt(lastLayout.childCount-1)

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

            scrollLay.requestLayout()
            isCheckEditCard = !isCheckEditCard
        }
    }

    private fun showcreateCard() {

        createCardLay.visibility = ConstraintLayout.VISIBLE

        createCardLay.animate()
            .translationY(0f)
            .setDuration(500)
            .withEndAction { Runnable {
                hideshowCards(true)
            }.run() }
            .start()

        backButton.setOnClickListener {

            hideshowCards(false)

            createCardLay.animate()
                .translationY(900.dpToPx.toFloat())
                .setDuration(500)
                .withEndAction { Runnable {
                    createCardLay.visibility = ConstraintLayout.INVISIBLE
                }.run() }
                .start()

        }

        chooseBackCard.setOnClickListener {

            selectImageIntent.launch("image/*")
            nameCardText.text = cardNameEdit.text.toString()

        }

        changeTimerButton.setOnClickListener {
            val cal = Calendar.getInstance()
            val timeSetListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
                cal.set(Calendar.HOUR_OF_DAY, hour)
                cal.set(Calendar.MINUTE, minute)
                timerTextChange.text = SimpleDateFormat("HH:mm").format(cal.time)
            }
            nameCardText.text = cardNameEdit.text.toString()
            TimePickerDialog(this, timeSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }


        saveButton.setOnClickListener {
            if (uriImage.isNotEmpty() && cardNameEdit.text.toString().trim().isNotEmpty()) {

                // write file

                addCardToDB(
                    createDirectoryAndSaveFile(uriToBitmap(Uri.parse(uriImage))!!, cardNameEdit.text.toString()).toString(),
                    cardNameEdit.text.toString(),
                    timerTextChange.text.toString() + ":00"
                )
            } else {
                Toast.makeText(this, "You have not selected a image or choose name", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uriToBitmap(selectedFileUri: Uri): Bitmap? {
        try {
            val parcelFileDescriptor = contentResolver.openFileDescriptor(selectedFileUri, "r")
            val fileDescriptor: FileDescriptor = parcelFileDescriptor!!.fileDescriptor
            val image = BitmapFactory.decodeFileDescriptor(fileDescriptor)
            parcelFileDescriptor.close()
            return image
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    private fun createDirectoryAndSaveFile(imageToSave: Bitmap, fileName: String): Uri {
        val direct = File(filesDir.absolutePath + "/MeditAction")
        if (!direct.exists()) {
            val wallpaperDirectory = File(filesDir.absolutePath + "/MeditAction/")
            wallpaperDirectory.mkdirs()
        }
        val file = File(filesDir.absolutePath + "/MeditAction/", fileName)
        if (file.exists()) {
            file.delete()
        }
        try {
            val out = FileOutputStream(file)
            imageToSave.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.flush()
            out.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return Uri.fromFile(file)
    }

    private fun addCardToDB(cardImg: String, titleText: String, timer: String?) {

        Observable.fromCallable {

            // main
            val db = AppDatabase.getAppDatabase(applicationContext)

            cardDao = db!!.cardDao() // Я СДЕЛАЛ ЭТУ ХУЙНЮ!!!!!!!!!!!

            cardDao.insertCard(Card(0, titleText, cardImg, timer!!))


        }.doOnError { Log.e("ERROR", it.message.toString()) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnComplete {
                Log.e("LOGG", cardImg)
                hideshowCards(false)
                isCheckEditCard = true
                addCard()
                createCardLay.animate()
                    .translationY(900.dpToPx.toFloat())
                    .setDuration(500)
                    .withEndAction { Runnable {
                        createCardLay.visibility = ConstraintLayout.INVISIBLE
                    }.run() }
                    .start()
                clearLayout()
                showCards()

                // clear lay
                uriImage = ""
                nameCardText.text = ""
                timerTextChange.text = ""

            }
            .subscribe()
    }

    private fun clearLayout() {
        scrollLay.removeViews(1, scrollLay.childCount-1)
        (scrollLay.getChildAt(0) as LinearLayout).removeAllViews()
    }

    private fun hideshowCards(isHide: Boolean) {
        if (isHide) {
            scrollLay.visibility = View.INVISIBLE
            editCardLay.visibility = View.INVISIBLE
            musicLay.visibility = View.INVISIBLE
        } else {
            scrollLay.visibility = View.VISIBLE
            editCardLay.visibility = View.VISIBLE
            musicLay.visibility = View.VISIBLE
        }
    }

    private fun createCard(id: Int, cardImg: Int = 0, titleText: String?, timer: String?, cardImgUri: String = "") {

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
            (sampleCard.layoutParams as LinearLayout.LayoutParams).leftMargin =  8.dpToPx
            (sampleCard.layoutParams as LinearLayout.LayoutParams).rightMargin = 8.dpToPx
            lastLayout.requestLayout()

            // OnClick Linstener
            sampleCard.setOnClickListener {

                intent = Intent(this, MeditationActivity::class.java)
                intent.putExtra("id", id)

                startActivity(intent)
                overridePendingTransition(androidx.appcompat.R.anim.abc_fade_in, androidx.appcompat.R.anim.abc_fade_out)
            }
            
            
            // Image Background on card
            val backgroundCard = ImageView(this)

            backgroundCard.id = ImageView.generateViewId()

            // Rounded corners and CenterCrop image, background image for card
            val multi = MultiTransformation(
                CenterCrop(),
                RoundedCornersTransformation(16.dpToPx, 0, RoundedCornersTransformation.CornerType.ALL)
            )

            Glide.with(this).load(cardImg)
                .apply(RequestOptions.bitmapTransform(multi))
                .error(Uri.parse(cardImgUri))
                .into(backgroundCard)
            
            sampleCard.addView(backgroundCard, ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT)

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
            textTitle.setTextColor(Color.rgb(255,255,255))
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

            scrollLay.addView(cardsLayout, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            (cardsLayout.layoutParams as LinearLayout.LayoutParams).topMargin = 12.dpToPx

            // Restart function
            createCard(id, cardImg, titleText, timer, cardImgUri)
        }

    }

    private fun getViews(layout: ViewGroup): List<View> {
        val views: MutableList<View> = ArrayList()
        for (i in 0 until layout.childCount) {
            views.add(layout.getChildAt(i))
        }
        return views
    }



}