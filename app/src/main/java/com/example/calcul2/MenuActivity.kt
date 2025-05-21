package com.example.calcul2

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button


class MenuActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        val calculatorButton: Button = findViewById(R.id.button_calculator)
        val mp3PlayerButton: Button = findViewById(R.id.button_mp3_player)

        calculatorButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        mp3PlayerButton.setOnClickListener {
            val intent = Intent(this, Mp3PlayerActivity::class.java)
            startActivity(intent)
        }
        val locationButton: Button = findViewById(R.id.button_location)
        locationButton.setOnClickListener {
            val intent = Intent(this, LocationActivity::class.java)
            startActivity(intent)
        }

    }
}
