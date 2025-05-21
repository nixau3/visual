package com.example.calcul2

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.*
import android.telephony.PhoneStateListener
import android.telephony.SignalStrength
import android.telephony.TelephonyManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import java.text.SimpleDateFormat
import java.util.*

class LocationActivity : AppCompatActivity() {

    private lateinit var locationText: TextView
    private lateinit var telephonyManager: TelephonyManager
    private val locationClient by lazy { LocationServices.getFusedLocationProviderClient(this) }

    private var lastDbm: Int? = null

    private val updateInterval = 60000L
    private val handler = Handler(Looper.getMainLooper())
    private val updateRunnable = object : Runnable {
        override fun run() {
            getSignalStrength()
            handler.postDelayed(this, updateInterval)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location)

        locationText = findViewById(R.id.locationText)
        telephonyManager = getSystemService(TELEPHONY_SERVICE) as TelephonyManager

        if (
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_PHONE_STATE),
                100
            )
        } else {
            startAutoUpdate()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            startAutoUpdate()
        } else {
            locationText.text = "Нет разрешения на доступ к местоположению или телефону"
        }
    }

    private fun startAutoUpdate() {
        handler.post(updateRunnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateRunnable)
    }

    private fun getSignalStrength() {
        telephonyManager.listen(object : PhoneStateListener() {
            override fun onSignalStrengthsChanged(signalStrength: SignalStrength) {
                super.onSignalStrengthsChanged(signalStrength)

                val dBm = signalStrength.cellSignalStrengths.firstOrNull()?.dbm
                lastDbm = dBm

                val networkType = when (telephonyManager.networkType) {
                    TelephonyManager.NETWORK_TYPE_LTE -> "LTE"
                    TelephonyManager.NETWORK_TYPE_NR -> "5G"
                    TelephonyManager.NETWORK_TYPE_HSPAP,
                    TelephonyManager.NETWORK_TYPE_HSPA -> "HSPA"
                    TelephonyManager.NETWORK_TYPE_EDGE -> "EDGE"
                    TelephonyManager.NETWORK_TYPE_GPRS -> "GPRS"
                    TelephonyManager.NETWORK_TYPE_UMTS -> "UMTS"
                    else -> "Неизвестно"
                }

                val signalInfo = if (dBm != null) {
                    "тип сети: $networkType\nМщность сигнала: $dBm дБм"
                } else {
                    "тип сети: $networkType\nМощность сигнала: не определена"
                }

                updateFullText(signalInfo)
            }
        }, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS)
    }

    private fun updateFullText(signalInfo: String) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        locationClient.lastLocation.addOnSuccessListener { location: Location? ->
            val time = if (location != null) {
                SimpleDateFormat("HH:mm:ss dd.MM.yyyy", Locale.getDefault()).format(Date(location.time))
            } else {
                SimpleDateFormat("HH:mm:ss dd.MM.yyyy", Locale.getDefault()).format(Date())
            }

            if (location != null) {
                val latitude = location.latitude
                val longitude = location.longitude
                locationText.text = """
                    Широта: $latitude
                    Долгота: $longitude
                    Время: $time
                    $signalInfo
                """.trimIndent()
            } else {
                locationText.text = "Не удалось получить местоположение\n$signalInfo"
            }
        }
    }
}
