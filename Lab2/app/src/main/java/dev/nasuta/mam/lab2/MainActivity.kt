package dev.nasuta.mam.lab2

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var magneticSensor: Sensor? = null
    private var gravitySensor: Sensor? = null

    private lateinit var sensorTextView: TextView
    private lateinit var compassImageView: ImageView

    private var magneticValues: FloatArray? = FloatArray(3)
    private var gravityValues: FloatArray? = FloatArray(3)
    private var hasMagnetic = false
    private var hasGravity = false

    private val rotationMatrix = FloatArray(9)
    private val remappedRotationMatrix = FloatArray(9)
    private val orientation = FloatArray(3)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        sensorTextView = findViewById(R.id.sensorTextView)
        compassImageView = findViewById(R.id.compassImageView)
        sensorTextView.text = "Awaiting sensor data"

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        magneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)

        if (magneticSensor == null || gravitySensor == null) {
            sensorTextView.text = "Required sensors (magnetometer, gravity) are not available"
        }
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(this, magneticSensor, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(this, gravitySensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        when (event?.sensor?.type) {
            Sensor.TYPE_MAGNETIC_FIELD -> {
                magneticValues = event.values.clone()
                hasMagnetic = true
            }
            Sensor.TYPE_GRAVITY -> {
                gravityValues = event.values.clone()
                hasGravity = true
            }
            else -> return
        }
        updateText()
        updateCompass()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun updateText() {
        val m = magneticValues ?: return
        val g = gravityValues ?: return
        sensorTextView.text = String.format(
            "Magnetic field (µT):\n  x=%.2f  y=%.2f  z=%.2f\nGravity (m/s²):\n  x=%.2f  y=%.2f  z=%.2f",
            m[0], m[1], m[2], g[0], g[1], g[2]
        )
    }

    private fun updateCompass() {
        if (!hasMagnetic || !hasGravity) return
        val gravity = gravityValues ?: return
        val magnetic = magneticValues ?: return

        if (!SensorManager.getRotationMatrix(rotationMatrix, null, gravity, magnetic)) return
        // Remap so that the compass works with the device held upright (camera pointing forward).
        SensorManager.remapCoordinateSystem(
            rotationMatrix,
            SensorManager.AXIS_X,
            SensorManager.AXIS_Z,
            remappedRotationMatrix
        )
        SensorManager.getOrientation(remappedRotationMatrix, orientation)

        val azimuthDegrees = Math.toDegrees(orientation[0].toDouble()).toFloat()
        // The needle must counter-rotate by the azimuth to keep pointing at magnetic north.
        compassImageView.rotation = -azimuthDegrees
    }
}
