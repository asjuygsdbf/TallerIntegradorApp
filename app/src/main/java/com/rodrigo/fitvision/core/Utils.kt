package com.rodrigo.fitvision.core

import com.google.mlkit.vision.pose.PoseLandmark
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt
import android.graphics.Canvas
import android.graphics.Paint

object Utils {

    fun calculateAngle(p1: PoseLandmark, p2: PoseLandmark, p3: PoseLandmark): Double {
        val angle = Math.toDegrees(
            atan2(
                (p3.position.y - p2.position.y).toDouble(),
                (p3.position.x - p2.position.x).toDouble()
            ) - atan2(
                (p1.position.y - p2.position.y).toDouble(),
                (p1.position.x - p2.position.x).toDouble()
            )
        )
        return if (angle < 0) angle + 360 else angle
    }

    fun distance(p1: PoseLandmark, p2: PoseLandmark): Double {
        val dx = p1.position.x.toDouble() - p2.position.x.toDouble()
        val dy = p1.position.y.toDouble() - p2.position.y.toDouble()
        return sqrt(dx * dx + dy * dy)
    }

    fun drawText(
        canvas: Canvas,
        text: String,
        x: Float,
        y: Float,
        color: Int,
        textSize: Float = 40f
    ) {
        val paint = Paint().apply {
            this.color = color
            this.textSize = textSize
            this.style = Paint.Style.FILL
        }
        canvas.drawText(text, x, y, paint)
    }
}
