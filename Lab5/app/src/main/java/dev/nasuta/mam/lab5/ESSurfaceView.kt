package dev.nasuta.mam.lab5

import android.annotation.SuppressLint
import android.content.Context
import android.opengl.GLSurfaceView
import android.view.MotionEvent

class ESSurfaceView(context: Context) : GLSurfaceView(context) {
    val renderer = GLRenderer()

    private var previousX = 0f
    private var previousY = 0f

    init {
        // OpenGL ES context initialization.
        setEGLContextClientVersion(2)
        renderer.setContext(context)
        setRenderer(renderer)
    }

    // Dragging a finger moves the camera (and its target) in the plane of the screen.
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(e: MotionEvent): Boolean {
        val x = e.x
        val y = e.y
        if (e.action == MotionEvent.ACTION_MOVE) {
            val dx = (x - previousX) * 0.01f
            val dy = (y - previousY) * 0.01f
            val camera = renderer.camera
            camera[0] -= dx
            camera[3] -= dx
            camera[1] += dy
            camera[4] += dy
        }
        previousX = x
        previousY = y
        return true
    }
}
