package com.rodrigo.fitvision.core

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark
import kotlin.math.*

class AbdominalProcessor(
    private val thresholds: Thresholds.ThresholdSet,
    private val frameWidth: Float = 480f,
    private val frameHeight: Float = 640f
) {
    private var state = "down"
    private var counter = 0
    private var lastFeedback = ""

    private val paintText = Paint().apply {
        color = Color.WHITE
        textSize = 42f
    }

    private val paintPoint = Paint().apply {
        color = Color.YELLOW
        style = Paint.Style.FILL
    }

    private val paintLine = Paint().apply {
        color = Color.GREEN
        strokeWidth = 6f
        style = Paint.Style.STROKE
    }

    fun process(pose: Pose, canvas: Canvas) {
        val landmarks = pose.allPoseLandmarks
        if (landmarks.isEmpty()) return

        val scaleX = canvas.width / frameWidth
        val scaleY = canvas.height / frameHeight
        val scale = min(scaleX, scaleY)
        val offsetY = -100f

        fun sx(lm: PoseLandmark) = canvas.width - (lm.position.x * scale)
        fun sy(lm: PoseLandmark) = lm.position.y * scale + offsetY

        fun angle(p1: PoseLandmark, p2: PoseLandmark, p3: PoseLandmark): Double {
            val x1 = p1.position.x.toDouble()
            val y1 = p1.position.y.toDouble()
            val x2 = p2.position.x.toDouble()
            val y2 = p2.position.y.toDouble()
            val x3 = p3.position.x.toDouble()
            val y3 = p3.position.y.toDouble()
            val radians = atan2(y3 - y2, x3 - x2) - atan2(y1 - y2, x1 - x2)
            var degrees = Math.toDegrees(radians)
            if (degrees < 0) degrees += 360.0
            return if (degrees > 180.0) 360.0 - degrees else degrees
        }

        val ls = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER) ?: return
        val rs = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER) ?: return
        val lh = pose.getPoseLandmark(PoseLandmark.LEFT_HIP) ?: return
        val rh = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP) ?: return
        val lk = pose.getPoseLandmark(PoseLandmark.LEFT_KNEE) ?: return
        val rk = pose.getPoseLandmark(PoseLandmark.RIGHT_KNEE) ?: return

        val avgShoulderY = (ls.position.y + rs.position.y) / 2
        val avgHipY = (lh.position.y + rh.position.y) / 2
        val avgKneeY = (lk.position.y + rk.position.y) / 2

        val torsoAngle = angle(ls, lh, lk)
        val shoulderToHip = avgShoulderY - avgHipY
        val offset = shoulderToHip / frameHeight

        val upCondition = offset < 0.05 && torsoAngle < 90
        val downCondition = offset > 0.15 && torsoAngle > 130

        when (state) {
            "down" -> if (upCondition) {
                state = "up"
                lastFeedback = "⬆ Sube más"
            }
            "up" -> if (downCondition) {
                counter++
                state = "down"
                lastFeedback = "✔ ¡Bien hecho!"
            }
        }

        for (lm in landmarks) {
            canvas.drawCircle(sx(lm), sy(lm), 18f, paintPoint)
        }

        for ((a, b) in listOf(
            PoseLandmark.LEFT_SHOULDER to PoseLandmark.RIGHT_SHOULDER,
            PoseLandmark.LEFT_SHOULDER to PoseLandmark.LEFT_HIP,
            PoseLandmark.RIGHT_SHOULDER to PoseLandmark.RIGHT_HIP,
            PoseLandmark.LEFT_HIP to PoseLandmark.RIGHT_HIP,
            PoseLandmark.LEFT_HIP to PoseLandmark.LEFT_KNEE,
            PoseLandmark.RIGHT_HIP to PoseLandmark.RIGHT_KNEE
        )) {
            val p1 = pose.getPoseLandmark(a)
            val p2 = pose.getPoseLandmark(b)
            if (p1 != null && p2 != null) {
                canvas.drawLine(sx(p1), sy(p1), sx(p2), sy(p2), paintLine)
            }
        }

        drawText(canvas, "TORSO: ${torsoAngle.toInt()}°", 30f, 60f, Color.CYAN)
        drawText(canvas, "ABDOMINALES: $counter", 30f, canvas.height - 140f, Color.GREEN)
        drawText(canvas, lastFeedback, 30f, canvas.height - 80f, Color.WHITE)
    }

    private fun drawText(canvas: Canvas, text: String, x: Float, y: Float, color: Int) {
        paintText.color = color
        canvas.drawText(text, x, y, paintText)
    }
}