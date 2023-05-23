package com.example.myapplication3457

import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.content.res.Resources
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException


class MusicListFragment : Fragment() {

    private lateinit var backBtn: ImageView
    private lateinit var addMusic: ImageView
    private lateinit var contentLay: LinearLayout

    private val Int.dpToPx: Int
        get() = (this * Resources.getSystem().displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_music_list, container, false)

        backBtn = view.findViewById(R.id.backMusicButton)
        addMusic = view.findViewById(R.id.addMusicButton)
        contentLay = view.findViewById(R.id.contentMusicLayout)

        backBtn.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .setCustomAnimations(androidx.appcompat.R.anim.abc_slide_in_bottom, androidx.appcompat.R.anim.abc_slide_out_bottom)
                .remove(this@MusicListFragment)
                .commit()
        }

        addMusic.setOnClickListener {

            val dialog = Dialog(view.context)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

            dialog.setContentView(R.layout.dialog_music_layout)

            val url: EditText = dialog.findViewById(R.id.urlEditText)
            val addBtn: Button = dialog.findViewById(R.id.addUrlButton)

            addBtn.setOnClickListener {
                if (url.text.trim().isNotEmpty()) {
                    try { // open url in order to make sure
                        val intentUrl = url.text.toString()
                        val webIntent = Intent(ACTION_VIEW, Uri.parse(intentUrl))
                        startActivity(webIntent)

                        createMusicCard(url.text.toString())
                        addUrlToDB(url.text.toString())

                        dialog.cancel()
                    } catch (e: ActivityNotFoundException) {
                        Toast.makeText(view.context, "URL is wrong", Toast.LENGTH_SHORT)
                            .show()
                    }

                } else {
                    Toast.makeText(view.context, "The text field is empty", Toast.LENGTH_SHORT).show()
                }
            }

            dialog.show()

        }

        showMusic()

        return view
    }

    private fun addUrlToDB(urlText: String) {

        Observable.fromCallable {

            // main
            val db = AppDatabase.getAppDatabase(requireActivity().applicationContext)

            val cardDao = db!!.cardDao()

            cardDao.insertMusic(Music(0, urlText))

        }.doOnError { Log.e("ERROR", it.message.toString()) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe()

    }
    
    private fun createMusicCard(url: String) {

        // Create card layout
        val musicCard = LinearLayout(requireView().context)

        contentLay.addView(musicCard)

        val params: LinearLayout.LayoutParams =
            musicCard.layoutParams as LinearLayout.LayoutParams
        params.setMargins(0, 0, 0, 5.dpToPx)

        musicCard.layoutParams = params

        musicCard.setPadding(10.dpToPx, 10.dpToPx, 10.dpToPx, 10.dpToPx)

        musicCard.setBackgroundResource(R.drawable.xml_border15dp)
        musicCard.backgroundTintList = backBtn.backgroundTintList

        musicCard.setOnClickListener {
            try {
                val intentUrl = url
                val webIntent = Intent(ACTION_VIEW, Uri.parse(intentUrl))
                startActivity(webIntent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(requireView().context, "URL is wrong", Toast.LENGTH_SHORT)
                    .show()
            }
        }


        val urlText = TextView(requireView().context)

        // grab title in URI
        lateinit var doc: Document

        Observable.fromCallable {
            doc = Jsoup.connect(url).get()
        }.doOnError { Log.e("ERROR", it.message.toString()) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnComplete {
                urlText.text = doc.title()
            }
            .doOnError { urlText.text = url }
            .subscribe()

        urlText.setTextColor(Color.WHITE)
        urlText.textSize = 20f
        urlText.typeface =
            ResourcesCompat.getFont(requireView().context, R.font.kumbh_sans_regular)

        musicCard.addView(urlText)

    }

    private fun showMusic() {

        lateinit var musics: List<Music>

        Observable.fromCallable {

            // main
            val db = AppDatabase.getAppDatabase(requireActivity().applicationContext)

            var cardDao = db!!.cardDao()

            musics = cardDao.getMusic()

        }.doOnError { Log.e("ERROR", it.message.toString()) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnComplete {
                for (music in musics) {
                    createMusicCard(music.music)
                }
            }
            .subscribe()

    }

}