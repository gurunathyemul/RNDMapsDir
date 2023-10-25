package com.example.rndmapsdir

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.rndmapsdir.databinding.ActivityABinding
import java.util.Timer
import java.util.TimerTask
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin


class A : AppCompatActivity(), SensorEventListener, LocationListener {
    private lateinit var binding: ActivityABinding
    private lateinit var sensorManager: SensorManager
    private lateinit var locationManager: LocationManager
    private var rotationVector: Sensor? = null
    private var accelerometer: Sensor? = null
    private var magnetometer: Sensor? = null
    private var magnetometerValues: FloatArray? = null
    private var accelerometerValues: FloatArray? = null
    private var position: Int = 0
    private var currentLocation: Location? = null
    private lateinit var secondLatLng: LatLng
    val ameerpetLatLng = LatLng(17.4375, 78.4482)
    val KothagudaLatLng = LatLng(17.4643, 78.3756)
    val tcsLat = LatLng(17.444826231193, 78.37781581990357)
    val durgamCheruvuLatLng = LatLng(17.4300, 78.3895)
    val gachibowliLatLng = LatLng(17.439942546312206, 78.34882434530869)
    val ikeaLatLng = LatLng(17.4424122448374, 78.3780579478865)
    val cyberTowerLatLng = LatLng(17.45077048554629, 78.38119090225709)
    val ptgLatLng = LatLng(17.448632032825227, 78.37596994760975)

    val latLngArray = arrayOf(
        ameerpetLatLng,
        KothagudaLatLng,
        tcsLat,
        durgamCheruvuLatLng,
        gachibowliLatLng,
        ikeaLatLng,
        cyberTowerLatLng
    )
    val latLngNames = arrayOf(
        "AmeerpetLatLng",
        "KothagudaLatLng",
        "TCSLatLng",
        "DurgamCheruvuLatLng",
        "GachibowliLatLng",
        "IkeaLatLng",
        "CyberTowerLatLng",
    )
    private val timer = Timer()

    private val timerTask = object : TimerTask() {
        override fun run() {
            runOnUiThread {
                if (latLngArray.size != position) {
                    secondLatLng = latLngArray[position]
                    binding.tvLatLngName.text = latLngNames[position]
                    position++
                } else position = 0
                binding.tvSecLatLng.text = "${secondLatLng.latitude}::${secondLatLng.longitude}"
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_a
        )
        getLocationPermission()

        timer.schedule(timerTask, 50, 6000)
        initializeLocSensorManager()
        reqLocUpdates()

        binding.btnGachibowli.setOnClickListener {
            secondLatLng = gachibowliLatLng
        }
        binding.btnIkea.setOnClickListener {
            secondLatLng = ikeaLatLng
        }
        binding.btnCyberTowers.setOnClickListener {
            secondLatLng = cyberTowerLatLng
        }

    }

    @SuppressLint("MissingPermission")
    private fun reqLocUpdates() {
        val minTime = 10000L
        val minDistance = 0f
//        val provider = LocationManager.GPS_PROVIDER // Use GPS provider
        val provider = getAvailableLocationProvider()
        provider?.let {
            locationManager.requestLocationUpdates(
                it,
                minTime,
                minDistance,
                this
            )
        }
    }

    private fun getAvailableLocationProvider(): String? {
        val criteria = Criteria()
        criteria.accuracy = Criteria.ACCURACY_COARSE  // Adjust accuracy as needed
        return locationManager.getBestProvider(criteria, true)
    }

    private fun initializeLocSensorManager() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorManager.getSensorList(Sensor.TYPE_ALL).forEach {
            Log.d(TAG, "initializeLocSensorManager: ${it.name}")
        }
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onResume() {
        super.onResume()
        Log.d(TAG, "LocationEnabled: ${locationManager.isLocationEnabled}")
        if (!locationManager.isLocationEnabled) {
            getLocationPermission()
        }
        // Register sensor listeners
        accelerometer?.let {
            sensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
        magnetometer?.let {
            sensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    override fun onPause() {
        super.onPause()
        // Unregister sensor listeners
        sensorManager.unregisterListener(this)
        // Remove location updates
        locationManager.removeUpdates(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        when (event?.sensor) {
            accelerometer -> {
                accelerometerValues = event?.values
            }

            magnetometer -> {
                magnetometerValues = event?.values
            }
        }
        Log.d(TAG, "onSensorChanged: $accelerometer::$magnetometer")
        // Calculate the rotation matrix
        if (accelerometerValues != null && magnetometerValues != null) {
            val rotationMatrix = FloatArray(9)
            val success = SensorManager.getRotationMatrix(
                rotationMatrix,
                null,
                accelerometerValues,
                magnetometerValues
            )

            if (success) {
                // Calculate the orientation angles
                val orientationValues = FloatArray(3)
                SensorManager.getOrientation(rotationMatrix, orientationValues)
                // Convert the azimuth angle to degrees
                //remove 45(used for different devices)
                val azimuthDegrees = Math.toDegrees(orientationValues[0].toDouble()).toFloat()
                // Calculate the bearing (azimuth) from your current location to the second LatLng
//                currentLocation?.let {
                val bearing = calculateBearing(
//                        currentLocation!!.latitude,
//                        currentLocation!!.longitude,
                    ptgLatLng.latitude,
                    ptgLatLng.longitude,
                    secondLatLng.latitude,
                    secondLatLng.longitude
                )
                // Calculate the relative direction
                val relativeDirection = calculateRelativeDirection(bearing, azimuthDegrees)
                // Display the relative direction in your UI (e.g., TextView)
                updateUIWithDirection(bearing, relativeDirection,azimuthDegrees,relativeDirection)
//                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Handle accuracy changes if needed
    }

    override fun onLocationChanged(location: Location) {
        currentLocation = location
        Log.d(TAG, "onLocationChanged: ${currentLocation?.latitude}::${currentLocation?.longitude}")
    }

    private fun calculateBearing(
        startLat: Double,
        startLng: Double,
        endLat: Double,
        endLng: Double
    ): Float {
        val deltaLng = endLng - startLng
        val y = sin(deltaLng) * cos(endLat)
        val x = cos(startLat) * sin(endLat) - sin(startLat) * cos(endLat) * cos(deltaLng)
        val initialBearing = atan2(y, x)
        return ((Math.toDegrees(initialBearing) + 360) % 360).toFloat()
    }

    private fun calculateRelativeDirection(bearing: Float, azimuth: Float): String {
        // Calculate the relative direction based on the difference between bearing and azimuth
        val angleDifference = (bearing - azimuth + 360) % 360
        binding.ivNav.animate().rotation(angleDifference)
        return when {
            angleDifference >= 337.5 || angleDifference < 22.5 -> "N"
            angleDifference >= 22.5 && angleDifference < 67.5 -> "NE"
            angleDifference >= 67.5 && angleDifference < 112.5 -> "E"
            angleDifference >= 112.5 && angleDifference < 157.5 -> "SE"
            angleDifference >= 157.5 && angleDifference < 202.5 -> "S"
            angleDifference >= 202.5 && angleDifference < 247.5 -> "SW"
            angleDifference >= 247.5 && angleDifference < 292.5 -> "W"
            angleDifference >= 292.5 && angleDifference < 337.5 -> "NW"
            else -> "N"
        }
    }

    private fun updateUIWithDirection(
        bearing: Float,
        direction: String,
        azimuthDegrees: Float,
        relativeDirection: String
    ) {
//        binding.angleDirection.text = "bearing$bearing::direction$direction::azimuthDegrees$azimuthDegrees::relativeDirection$relativeDirection"
        binding.angleDirection.text = "$azimuthDegrees"
    }

    private fun getLocationPermission() {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        reqPermission.launch(permissions)

    }

    private val reqPermission =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permisssions ->
            permisssions.entries.forEach {
                if (!it.value) {
                    Toast.makeText(this, "Permissions Denied", Toast.LENGTH_SHORT).show()
                }
            }

        }

    companion object {
        private const val TAG = "A"
    }
}
