package com.rodrigo.fitvision.core

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark
import kotlin.math.*

class SquatProcessor(
    private val thresholds: Thresholds.ThresholdSet,
    private val frameWidth: Float = 480f,
    private val frameHeight: Float = 640f
) {
    private var state = "s1"
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

        val lh = pose.getPoseLandmark(PoseLandmark.LEFT_HIP) ?: return
        val lk = pose.getPoseLandmark(PoseLandmark.LEFT_KNEE) ?: return
        val la = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE) ?: return
        val rh = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP) ?: return
        val rk = pose.getPoseLandmark(PoseLandmark.RIGHT_KNEE) ?: return
        val ra = pose.getPoseLandmark(PoseLandmark.RIGHT_ANKLE) ?: return
        val ls = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER) ?: return
        val rs = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER) ?: return

        val leftKneeAngle = angle(lh, lk, la)
        val rightKneeAngle = angle(rh, rk, ra)
        val avgKneeAngle = (leftKneeAngle + rightKneeAngle) / 2

        when (state) {
            "s1" -> if (avgKneeAngle < 130) {
                state = "s2"; lastFeedback = "⬇ Bajando"
            }
            "s2" -> if (avgKneeAngle < 90) {
                state = "s3"; lastFeedback = "⬆ Subiendo"
            }
            "s3" -> if (avgKneeAngle > 160) {
                counter++; state = "s1"; lastFeedback = "✔ ¡Bien hecho!"
            }
        }

        for (lm in landmarks) canvas.drawCircle(sx(lm), sy(lm), 18f, paintPoint)

        for ((a, b) in listOf(
            PoseLandmark.LEFT_SHOULDER to PoseLandmark.RIGHT_SHOULDER,
            PoseLandmark.LEFT_SHOULDER to PoseLandmark.LEFT_HIP,
            PoseLandmark.RIGHT_SHOULDER to PoseLandmark.RIGHT_HIP,
            PoseLandmark.LEFT_HIP to PoseLandmark.RIGHT_HIP,
            PoseLandmark.LEFT_HIP to PoseLandmark.LEFT_KNEE,
            PoseLandmark.LEFT_KNEE to PoseLandmark.LEFT_ANKLE,
            PoseLandmark.RIGHT_HIP to PoseLandmark.RIGHT_KNEE,
            PoseLandmark.RIGHT_KNEE to PoseLandmark.RIGHT_ANKLE
        )) {
            val p1 = pose.getPoseLandmark(a)
            val p2 = pose.getPoseLandmark(b)
            if (p1 != null && p2 != null) canvas.drawLine(sx(p1), sy(p1), sx(p2), sy(p2), paintLine)
        }

        drawText(canvas, "RODILLA IZQ: ${leftKneeAngle.toInt()}°", 30f, 60f, Color.CYAN)
        drawText(canvas, "RODILLA DER: ${rightKneeAngle.toInt()}°", 30f, 110f, Color.CYAN)
        drawText(canvas, "SENTADILLAS: $counter", 30f, canvas.height - 140f, Color.GREEN)
        drawText(canvas, lastFeedback, 30f, canvas.height - 80f, Color.WHITE)
    }

    private fun drawText(canvas: Canvas, text: String, x: Float, y: Float, color: Int) {
        paintText.color = color
        canvas.drawText(text, x, y, paintText)
    }
}