package dev.nasuta.mam.lab5

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.SeekBar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class MainActivity : AppCompatActivity(), SeekBar.OnSeekBarChangeListener {
    private var glSurfaceView: ESSurfaceView? = null

    private var lightBar: SeekBar? = null
    private var orangeButton: Button? = null
    private var whiteButton: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        glSurfaceView = ESSurfaceView(this)

        // GUI initialization
        setContentView(R.layout.activity_main)
        val tempView = findViewById<View>(R.id.tempView)
        val parent = tempView.parent as ViewGroup
        val index = parent.indexOfChild(tempView)
        parent.removeView(tempView)
        parent.addView(glSurfaceView, index)

        // Fullscreen
        val rootView = findViewById<ConstraintLayout>(R.id.main)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, rootView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        lightBar = findViewById(R.id.lightSeekBar)
        lightBar?.setOnSeekBarChangeListener(this)
        orangeButton = findViewById(R.id.lightOrangeButton)
        orangeButton?.setOnClickListener {
            // LAB_2
        }
        whiteButton = findViewById(R.id.lightWhiteButton)
        whiteButton?.setOnClickListener {
            // LAB_2
        }
    }

    override fun onResume() {
        super.onResume()
        glSurfaceView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        glSurfaceView?.onPause()
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        // LAB_2
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {}

    override fun onStopTrackingTouch(seekBar: SeekBar?) {}
}