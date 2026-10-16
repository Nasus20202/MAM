package dev.nasuta.mam.lab3

import android.view.MotionEvent
import dev.romainguy.kotlin.math.Float2
import io.github.sceneview.gesture.GestureDetector
import io.github.sceneview.gesture.MoveGestureDetector
import io.github.sceneview.gesture.RotateGestureDetector
import io.github.sceneview.gesture.ScaleGestureDetector
import io.github.sceneview.node.Node

class GestureListener(private val activity: MainActivity) : GestureDetector.OnGestureListener {
    override fun onSingleTapConfirmed(e: MotionEvent, node: Node?) {
        if (node == null) {
            activity.moveToHit(e.x, e.y)
        }
    }

    override fun onSingleTapUp(e: MotionEvent, node: Node?) {}

    override fun onContextClick(e: MotionEvent, node: Node?) {}

    override fun onDoubleTap(e: MotionEvent, node: Node?) {}

    override fun onDoubleTapEvent(e: MotionEvent, node: Node?) {}

    override fun onDown(e: MotionEvent, node: Node?) {}

    override fun onFling(e1: MotionEvent?, e2: MotionEvent, node: Node?, velocity: Float2) {}

    override fun onLongPress(e: MotionEvent, node: Node?) {}

    override fun onMove(detector: MoveGestureDetector, e: MotionEvent, node: Node?) {}

    override fun onMoveBegin(detector: MoveGestureDetector, e: MotionEvent, node: Node?) {}

    override fun onMoveEnd(detector: MoveGestureDetector, e: MotionEvent, node: Node?) {}

    override fun onRotate(detector: RotateGestureDetector, e: MotionEvent, node: Node?) {}

    override fun onRotateBegin(detector: RotateGestureDetector, e: MotionEvent, node: Node?) {}

    override fun onRotateEnd(detector: RotateGestureDetector, e: MotionEvent, node: Node?) {}

    override fun onScale(detector: ScaleGestureDetector, e: MotionEvent, node: Node?) {}

    override fun onScaleBegin(detector: ScaleGestureDetector, e: MotionEvent, node: Node?) {}

    override fun onScaleEnd(detector: ScaleGestureDetector, e: MotionEvent, node: Node?) {}

    override fun onScroll(e1: MotionEvent?, e2: MotionEvent, node: Node?, distance: Float2) {}

    override fun onShowPress(e: MotionEvent, node: Node?) {}
}
