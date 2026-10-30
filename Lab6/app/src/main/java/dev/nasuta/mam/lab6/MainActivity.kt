package dev.nasuta.mam.lab6

import android.os.Bundle
import android.widget.Button
import android.widget.FrameLayout
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity(), SeekBar.OnSeekBarChangeListener {
    private lateinit var glSurfaceView: ESSurfaceView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        glSurfaceView = ESSurfaceView(this)
        findViewById<FrameLayout>(R.id.containerLayout).addView(glSurfaceView)

        findViewById<SeekBar>(R.id.lightSeekBar).setOnSeekBarChangeListener(this)
        findViewById<Button>(R.id.lightOrangeButton).setOnClickListener {
            setLightColour(1f, 0.5f, 0f)
        }
        findViewById<Button>(R.id.lightWhiteButton).setOnClickListener {
            setLightColour(1f, 1f, 1f)
        }
    }

    private fun setLightColour(r: Float, g: Float, b: Float) {
        glSurfaceView.renderer.lightCol = floatArrayOf(r, g, b)
    }

    override fun onResume() {
        super.onResume()
        glSurfaceView.onResume()
    }

    override fun onPause() {
        super.onPause()
        glSurfaceView.onPause()
    }

    // The seek bar moves the light along the X axis (range <-5, 5>).
    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        glSurfaceView.renderer.lightPos = floatArrayOf((progress - 50) / 10f, 2f, 1f)
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {}

    override fun onStopTrackingTouch(seekBar: SeekBar?) {}
}
