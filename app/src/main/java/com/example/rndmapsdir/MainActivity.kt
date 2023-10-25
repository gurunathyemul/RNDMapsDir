package com.example.rndmapsdir

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.rndmapsdir.databinding.ActivityMainBinding
import kotlin.math.floor


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
//        setContentView(R.layout.activity_main)

        val madhapurLat = 17.4486
        val madhapurLng = 78.3908

        val ameerpetLat = 17.4375
        val ameerpetLng = 78.4482

        val KothagudaLat = 17.4643
        val KothagudaLng = 78.3756

        val durgamCheruvuLat = 17.4300
        val durgamCheruvuLng = 78.3895

        val currentLat = 17.4484125
        val currentLng = 78.3750947

        val ptgLat = 17.448632032825227
        val ptgLng = 78.37596994760975

        val tcsLat = 17.444826231193
        val tcsLng = 78.37781581990357

        val aigLat = 17.4431
        val aigLng = 78.3661

        val bearing = calculateBearing(ptgLat, ptgLng, aigLat, aigLat)
        val compassDirection: String = getCompassDirection(bearing)

        Log.d(TAG, "Bearing: $bearing degrees")
        Log.d(TAG, "Compass Direction: $compassDirection")
    }

    private fun getCompassDirection(bearing: Double): String {
        val compassDirections = arrayOf(
            "North",
            "Northeast",
            "East",
            "Southeast",
            "South",
            "Southwest",
            "West",
            "Northwest"
        )
        val sectorSize = 360 / compassDirections.size
        val sectorIndex =
            floor((bearing + sectorSize / 2) / sectorSize).toInt() % compassDirections.size
        return compassDirections[sectorIndex]
    }

    private fun calculateBearing(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val dLon = lon2 - lon1
        val y = Math.sin(Math.toRadians(dLon)) * Math.cos(Math.toRadians(lat2))
        val x = Math.cos(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) -
                Math.sin(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(
            Math.toRadians(
                dLon
            )
        )
        val initialBearing = Math.toDegrees(Math.atan2(y, x))
        return (initialBearing + 360) % 360
    }

}