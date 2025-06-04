package com.rodrigo.fitvision

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View

class CameraOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var drawCallback: ((Canvas) -> Unit)? = null

    fun setDrawCallback(callback: (Canvas) -> Unit) {
        drawCallback = callback
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawCallback?.invoke(canvas)
    }
}