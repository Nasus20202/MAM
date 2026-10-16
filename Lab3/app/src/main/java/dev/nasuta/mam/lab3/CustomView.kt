package dev.nasuta.mam.lab3

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View


class CustomView : View {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    var bitmap: Bitmap? = null

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        val p = Paint()
        if (bitmap == null) {
            p.setARGB(255, 0, 0, 0)
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), p)
        }
        bitmap?.let {
            val dstRect = Rect(0, 0, width, height)
            canvas.drawBitmap(it, null, dstRect, null)
        }
    }
}