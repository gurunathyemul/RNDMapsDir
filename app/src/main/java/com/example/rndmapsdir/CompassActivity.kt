package com.example.rndmapsdir

import android.Manifest
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.rndmapsdir.databinding.ActivitySensorBinding
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin

class CompassActivity : AppCompatActivity(), SensorEventListener, LocationListener {
    private lateinit var binding: ActivitySensorBinding
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var magnetometer: Sensor? = null
    private lateinit var locationManager: LocationManager

    private lateinit var destinationLatLng: LatLng

    private val accelerometerData = FloatArray(3)
    private val magnetometerData = FloatArray(3)
    private val rotationMatrix = FloatArray(9)
    private val orientationData = FloatArray(3)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =
            DataBindingUtil.setContentView(this, R.layout.activity_sensor) as ActivitySensorBinding
        getLocationPermission()
        // Initialize sensors and location manager
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val ptgLat = 17.448632032825227
        val ptgLng = 78.37596994760975

        val tcsLat = 17.444826231193
        val tcsLng = 78.37781581990357

        val yashodhaHospLat = 17.46223491885894
        val yashodhaHospLng = 78.38532577841055
        // Initialize destinationLatLng with your destination coordinates
        destinationLatLng = LatLng(tcsLat, tcsLng)
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

    override fun onResume() {
        super.onResume()
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

        // Request location updates
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, this)
    }

    override fun onPause() {
        super.onPause()
        // Unregister sensor listeners
        sensorManager.unregisterListener(this)

        // Remove location updates
        locationManager.removeUpdates(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor == accelerometer) {
            accelerometerData.forEachIndexed { index, value ->
                accelerometerData[index] = event?.values?.get(index)!!
            }
        } else if (event?.sensor == magnetometer) {
            magnetometerData.forEachIndexed { index, value ->
                magnetometerData[index] = event?.values?.get(index)!!
            }
        }

        // Calculate the orientation
        SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerData, magnetometerData)
        SensorManager.getOrientation(rotationMatrix, orientationData)

        // Calculate the azimuth (bearing) angle
        val azimuth = Math.toDegrees(orientationData[0].toDouble()).toFloat()
        val direction = calculateCompassDirection(azimuth)
        binding.ivDirection.animate().rotation(azimuth)
        binding.tvAngle.text = "Direction: $azimuth"

        // Update your UI with the compass direction
        updateUIWithDirection(direction)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Handle accuracy changes if needed
    }

    override fun onLocationChanged(location: Location) {
        val currentLatLng = LatLng(location.latitude, location.longitude)

        // Calculate the compass direction when the location changes
        val direction = calculateCompassDirection(getBearing(currentLatLng, destinationLatLng))
        // Update your UI with the new compass direction
        updateUIWithDirection(direction)
    }

    // Calculate the compass direction based on the bearing angle
    private fun calculateCompassDirection(bearing: Float): String {
        // Convert the bearing to a compass direction (e.g., N, NE, E, SE, etc.)
        val compassDirections = arrayOf("N", "NE", "E", "SE", "S", "SW", "W", "NW", "N")
        val sectorIndex = ((bearing + 22.5) / 45.0).toInt() % 8

        return when (sectorIndex) {
            -1 -> compassDirections[7]
            -2 -> compassDirections[6]
            -3 -> compassDirections[5]
            -4 -> compassDirections[4]
            else -> compassDirections[sectorIndex]
        }
    }

    // Calculate the bearing angle between two LatLng coordinates
    private fun getBearing(start: LatLng, end: LatLng): Float {
        val lat1 = Math.toRadians(start.latitude)
        val lon1 = Math.toRadians(start.longitude)
        val lat2 = Math.toRadians(end.latitude)
        val lon2 = Math.toRadians(end.longitude)

        val deltaLon = lon2 - lon1

        val y = sin(deltaLon) * cos(lat2)
        val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(deltaLon)

        var initialBearing = atan2(y, x)

        // Convert the initial bearing to degrees
        initialBearing = Math.toDegrees(initialBearing)

        // Normalize the bearing to be in the range [0, 360)
        return ((initialBearing + 360) % 360).toFloat()
    }

    // Update your UI with the compass direction
    private fun updateUIWithDirection(direction: String) {
        // Update your UI elements with the compass direction
        // For example, you can display it in a TextView
        binding.tvDirection.text = "Direction: $direction"
    }

    // Implement other LocationListener methods (onProviderEnabled, onProviderDisabled, onStatusChanged)
    // if needed, but they are not used in this example.
    companion object {
        private const val TAG = "CompassActivity"
    }
}
