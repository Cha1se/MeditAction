package com.example.myapplication3457

import android.app.TimePickerDialog
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.res.ResourcesCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.engine.DiskCacheStrategy
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
import java.util.*
import kotlin.properties.Delegates

class CardCreateFragment : Fragment() {

    private lateinit var createCardLay: ConstraintLayout
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
    private lateinit var deleteButton: Button

    private lateinit var selectImageIntent: ActivityResultLauncher<String>
    private var uriImage: String = ""

    private val Int.dpToPx: Int
        get() = (this * Resources.getSystem().displayMetrics.density).toInt()

    private var idCard: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        idCard = requireActivity().intent.getIntExtra("ID", 0)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        
        val view = inflater.inflate(R.layout.fragment_card_create, container, false)

        createCardLay = view.findViewById(R.id.createCardLayout)
        backButton = view.findViewById(R.id.backBtn)
        cardNameEdit = view.findViewById(R.id.cardNameEdit)
        chooseBackCard = view.findViewById(R.id.chooseBackCard)
        chooseBackCardImg = view.findViewById(R.id.chooseBackCardImg)
        showCardLayout = view.findViewById(R.id.showCardLayout)
        showCardImg = view.findViewById(R.id.showCardImg)
        nameCardText = view.findViewById(R.id.nameCardText)
        timerTextChange = view.findViewById(R.id.timerTextChange)
        changeTimerButton = view.findViewById(R.id.changeTimerButton)
        saveButton = view.findViewById(R.id.saveButton)
        deleteButton = view.findViewById(R.id.deleteButton)

        selectImageIntent = registerForActivityResult(ActivityResultContracts.GetContent())
        { uri ->
            chooseBackCardImg.setPadding(0,0,0,0)
            showCardImg.setPadding(0,0,0,0)

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

        if (idCard == -1){
            showcreateCard()
        } else {
            showCardInDB(idCard!!)
        }

        return view
    }

    private fun showcreateCard(card: Card = Card(0, "","","")) {

        if (idCard == -1) {

            deleteButton.visibility = View.INVISIBLE

            backButton.setOnClickListener {

                requireActivity().supportFragmentManager.beginTransaction()
                    .setCustomAnimations(
                        androidx.appcompat.R.anim.abc_slide_in_bottom,
                        androidx.appcompat.R.anim.abc_slide_out_bottom
                    )
                    .remove(this@CardCreateFragment)
                    .commit()
                onDestroy()
            }

            chooseBackCard.setOnClickListener {

                selectImageIntent.launch("image/*")
                nameCardText.text = cardNameEdit.text.toString()

            }

            changeTimerButton.setOnClickListener {
                val cal = Calendar.getInstance()
                val timeSetListener =
                    TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
                        cal.set(Calendar.HOUR_OF_DAY, hour)
                        cal.set(Calendar.MINUTE, minute)
                        timerTextChange.text = SimpleDateFormat("HH:mm").format(cal.time)
                    }
                nameCardText.text = cardNameEdit.text.toString()
                TimePickerDialog(
                    view?.context,
                    timeSetListener,
                    cal.get(Calendar.HOUR_OF_DAY),
                    cal.get(Calendar.MINUTE),
                    true
                ).show()
            }


            saveButton.setOnClickListener {
                if (uriImage.isNotEmpty() && cardNameEdit.text.toString().trim().isNotEmpty() && chooseBackCardImg.drawable != null) {

                    Log.e("LOG", uriImage)

                    addCardToDB(
                        createDirectoryAndSaveFile(
                            uriToBitmap(Uri.parse(uriImage))!!,
                            cardNameEdit.text.toString()
                        ).toString(),
                        cardNameEdit.text.toString(),
                        timerTextChange.text.toString() + ":00"
                    )
                } else {
                    Toast.makeText(
                        view?.context,
                        "You have not selected a image or choose name",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } else {

            deleteButton.visibility = View.VISIBLE

            cardNameEdit.setText(card.cardName)
            nameCardText.text = card.cardName
            timerTextChange.text = card.timer.substring(0, 5)

            chooseBackCardImg.setPadding(0, 0, 0, 0)
            showCardImg.setPadding(0, 0, 0, 0)

            val multi = MultiTransformation(
                CenterCrop(),
                RoundedCornersTransformation(
                    15.dpToPx,
                    0,
                    RoundedCornersTransformation.CornerType.ALL
                )
            )
            Glide.with(this).load(
                requireActivity().resources.getIdentifier(
                    card.background,
                    "drawable",
                    requireActivity().applicationContext.packageName
                ))
                .apply(RequestOptions.skipMemoryCacheOf(true))
                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                .apply(RequestOptions.bitmapTransform(multi))
                .error(Uri.parse(card.background))
                .into(chooseBackCardImg)

            Glide.with(this).load(
                requireActivity().resources.getIdentifier(
                    card.background,
                    "drawable",
                    requireActivity().applicationContext.packageName
                ))
                .apply(RequestOptions.skipMemoryCacheOf(true))
                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                .apply(RequestOptions.bitmapTransform(multi))
                .error(Uri.parse(card.background))
                .into(showCardImg)


            backButton.setOnClickListener {

                requireActivity().supportFragmentManager.beginTransaction()
                    .setCustomAnimations(
                        androidx.appcompat.R.anim.abc_slide_in_bottom,
                        androidx.appcompat.R.anim.abc_slide_out_bottom
                    )
                    .remove(this@CardCreateFragment)
                    .commit()
                onDestroy()
            }

            chooseBackCard.setOnClickListener {

                selectImageIntent.launch("image/*")
                nameCardText.text = cardNameEdit.text.toString()

            }

            changeTimerButton.setOnClickListener {
                val cal = Calendar.getInstance()
                val timeSetListener =
                    TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
                        cal.set(Calendar.HOUR_OF_DAY, hour)
                        cal.set(Calendar.MINUTE, minute)
                        timerTextChange.text = SimpleDateFormat("HH:mm").format(cal.time)
                    }
                nameCardText.text = cardNameEdit.text.toString()
                TimePickerDialog(
                    view?.context,
                    timeSetListener,
                    cal.get(Calendar.HOUR_OF_DAY),
                    cal.get(Calendar.MINUTE),
                    true
                ).show()
            }

            deleteButton.setOnClickListener {
                deleteCard(idCard!!)
            }

            saveButton.setOnClickListener {
                if (chooseBackCardImg.drawable != null && cardNameEdit.text.toString().trim().isNotEmpty()) {

                    if (uriImage == "") {

                        updateCard(idCard!!,
                            "",
                            cardNameEdit.text.toString(),
                            timerTextChange.text.toString() + ":00"
                        )

                    } else {
                        updateCard(idCard!!,
                            createDirectoryAndSaveFile(
                                uriToBitmap(Uri.parse(uriImage))!!,
                                cardNameEdit.text.toString()
                            ).toString(),
                            cardNameEdit.text.toString(),
                            timerTextChange.text.toString() + ":00"
                        )
                    }

                } else {
                    Toast.makeText(
                        view?.context,
                        "You have not selected a image or choose name",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

        }
    }

    private fun deleteCard(id: Int) {
        Observable.fromCallable {

            // main
            val db = AppDatabase.getAppDatabase(requireActivity().applicationContext)

            val cardDao = db!!.cardDao()

            cardDao.deleteCard(cardDao.getCardById(id))


        }.doOnError { Log.e("ERROR", it.message.toString()) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnComplete {

                requireActivity().supportFragmentManager.beginTransaction()
                    .setCustomAnimations(androidx.appcompat.R.anim.abc_slide_in_bottom, androidx.appcompat.R.anim.abc_slide_out_bottom)
                    .remove(this@CardCreateFragment)
                    .commit()
                onDestroy()
                startActivity(Intent(view?.context!!, MainActivity::class.java))

            }
            .subscribe()
    }

    private fun updateCard(id: Int, cardImg: String = "", titleText: String, timer: String?) {

        Observable.fromCallable {

            val db = AppDatabase.getAppDatabase(requireActivity().applicationContext)

            val cardDao = db!!.cardDao()

            if (cardImg == "") {
                cardDao.updateCard(cardDao.getCardById(id).copy(cardName = titleText, timer = timer!!))
            } else {
                cardDao.updateCard(cardDao.getCardById(id).copy(cardName = titleText, background = cardImg, timer = timer!!))
            }


        }.doOnError { Log.e("ERROR", it.message.toString()) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnComplete {

                requireActivity().supportFragmentManager.beginTransaction()
                    .setCustomAnimations(androidx.appcompat.R.anim.abc_slide_in_bottom, androidx.appcompat.R.anim.abc_slide_out_bottom)
                    .remove(this@CardCreateFragment)
                    .commit()
                onDestroy()
                startActivity(Intent(view?.context!!, MainActivity::class.java))

            }
            .subscribe()

    }

    private fun uriToBitmap(selectedFileUri: Uri): Bitmap? {
        try {
            val parcelFileDescriptor = view?.context?.contentResolver?.openFileDescriptor(selectedFileUri, "r")
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
        val path = view?.context?.filesDir?.absolutePath + "/MeditAction"
        val direct = File(path)
        if (!direct.exists()) {
            val wallpaperDirectory = File(path)
            wallpaperDirectory.mkdirs()
        }
        val file = File(path, fileName)
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
            val db = AppDatabase.getAppDatabase(view?.context!!)

            val cardDao = db!!.cardDao()

            cardDao.insertCard(Card(0, titleText, cardImg, timer!!))


        }.doOnError { Log.e("ERROR", it.message.toString()) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnComplete {
                requireActivity().supportFragmentManager.beginTransaction()
                    .setCustomAnimations(androidx.appcompat.R.anim.abc_slide_in_bottom, androidx.appcompat.R.anim.abc_slide_out_bottom)
                    .remove(this@CardCreateFragment)
                    .commit()

                onDestroy()

                startActivity(Intent(view?.context!!, MainActivity::class.java))
            }
            .subscribe()
    }

    private fun showCardInDB(id: Int) {

        lateinit var card: Card

        Observable.fromCallable {

            // main
            val db = AppDatabase.getAppDatabase(requireActivity().applicationContext)

            val cardDao = db!!.cardDao()

            card = cardDao.getCardById(id)

        }.doOnError { Log.e("ERROR", it.message.toString()) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnComplete {

                showcreateCard(card)

            }
            .subscribe()
    }

}