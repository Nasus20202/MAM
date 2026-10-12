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
    private lateinit var sensorTextView: TextView
    private lateinit var compassImageView: ImageView

    private lateinit var sensorManager: SensorManager
    private var magneticSensor: Sensor? = null
    private var gravitySensor: Sensor? = null

    private var magneticValues: FloatArray? = FloatArray(3)
    private var gravityValues: FloatArray? = FloatArray(3)

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

        sensorManager = getSystemService(Context.SENSOR_SERVICE)
                as SensorManager
        magneticSensor =
            sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        gravitySensor =
            sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(this, magneticSensor,
            SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(this, gravitySensor,
            SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        when (event?.sensor?.type) {
            Sensor.TYPE_MAGNETIC_FIELD -> {
                magneticValues = event.values.clone()
            }
            Sensor.TYPE_GRAVITY -> {
                gravityValues = event.values.clone()
            }
        }
        updateSensorText()
        updateCompassRotation()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int)
    { }

    fun updateSensorText() {
        val m = magneticValues ?: return
        val g = gravityValues ?: return
        sensorTextView.text = String.format(
            "Magnetic field:  x=%.2f  y=%.2f  z=%.2f\nGravity: x=%.2f  y=%.2f  z=%.2f",
            m[0], m[1], m[2], g[0], g[1], g[2]
        )
    }

    fun updateCompassRotation() {
        val rotationMatrix = FloatArray(9)
        val rotationMatrix2 = FloatArray(9)
        val orientation = FloatArray(3)
        SensorManager.getRotationMatrix(
            rotationMatrix,null, gravityValues, magneticValues
        )
        SensorManager.remapCoordinateSystem(
            rotationMatrix,
            SensorManager.AXIS_X,
            SensorManager.AXIS_Z,
            rotationMatrix2
        )
        SensorManager.getOrientation(rotationMatrix2, orientation)
        val degrees = Math.toDegrees(orientation[0].toDouble())
        compassImageView.rotation = -degrees.toFloat()
    }
}
