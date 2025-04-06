package com.example.calcul2

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class Mp3PlayerActivity : AppCompatActivity() {

    private lateinit var pleer: MediaPlayer
    private lateinit var povtorKnopka: Button
    private lateinit var knopkaPlay: Button
    private lateinit var knopkaNext: Button
    private lateinit var knopkaNazad: Button
    private lateinit var polzunok: SeekBar
    private lateinit var zvukPolzunok: SeekBar
    private lateinit var vremyaText: TextView
    private lateinit var obshVremyaText: TextView

    private val pesni = listOf(R.raw.song1, R.raw.song2)
    private var tekPesnya = 0
    private var povtorVkl = false
    private lateinit var zvuk: AudioManager
    private val obrabotchik = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mp3_player)

        knopkaPlay = findViewById(R.id.playPauseButton)
        knopkaNext = findViewById(R.id.nextButton)
        knopkaNazad = findViewById(R.id.prevButton)
        povtorKnopka = findViewById(R.id.loopButton)
        polzunok = findViewById(R.id.seekBar)
        zvukPolzunok = findViewById(R.id.volumeBar)
        vremyaText = findViewById(R.id.currentTimeText)
        obshVremyaText = findViewById(R.id.durationText)

        zvuk = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        zvukPolzunok.max = zvuk.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        zvukPolzunok.progress = zvuk.getStreamVolume(AudioManager.STREAM_MUSIC)

        zvukPolzunok.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, prog: Int, b: Boolean) {
                zvuk.setStreamVolume(AudioManager.STREAM_MUSIC, prog, 0)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })

        zapustitPesnyu()

        knopkaPlay.setOnClickListener {
            if (pleer.isPlaying) {
                pleer.pause()
                knopkaPlay.text = "Play"
            } else {
                pleer.start()
                knopkaPlay.text = "Pause"
                obnovitPolzunok()
            }
        }

        knopkaNext.setOnClickListener {
            tekPesnya = (tekPesnya + 1) % pesni.size
            zapustitPesnyu()
            pleer.start()
            knopkaPlay.text = "Pause"
        }

        knopkaNazad.setOnClickListener {
            tekPesnya = if (tekPesnya - 1 < 0) pesni.size - 1 else tekPesnya - 1
            zapustitPesnyu()
            pleer.start()
            knopkaPlay.text = "Pause"
        }

        povtorKnopka.setOnClickListener {
            povtorVkl = !povtorVkl
            pleer.isLooping = povtorVkl
            povtorKnopka.text = if (povtorVkl) "Povtor: ON" else "Povtor: OFF"
        }

        polzunok.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, prog: Int, b: Boolean) {
                if (b) pleer.seekTo(prog)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })
    }

    private fun zapustitPesnyu() {
        if (::pleer.isInitialized) {
            pleer.stop()
            pleer.release()
        }

        pleer = MediaPlayer.create(this, pesni[tekPesnya])
        pleer.isLooping = povtorVkl

        polzunok.max = pleer.duration
        obshVremyaText.text = vremya(pleer.duration)

        pleer.setOnCompletionListener {
            if (!povtorVkl) {
                tekPesnya = (tekPesnya + 1) % pesni.size
                zapustitPesnyu()
                pleer.start()
            }
        }
        obnovitPolzunok()
    }

    private fun obnovitPolzunok() {
        obrabotchik.postDelayed(object : Runnable {
            override fun run() {
                if (pleer.isPlaying) {
                    polzunok.progress = pleer.currentPosition
                    vremyaText.text = vremya(pleer.currentPosition)
                    obrabotchik.postDelayed(this, 500)
                }
            }
        }, 0)
    }

    private fun vremya(ms: Int): String {
        val sekundy = ms / 1000
        val min = sekundy / 60
        val sek = sekundy % 60
        return String.format("%02d:%02d", min, sek)
    }

    override fun onPause() {
        super.onPause()
        if (pleer.isPlaying) pleer.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::pleer.isInitialized) pleer.release()
    }
}
