package com.rodrigo.fitvision.core

import android.graphics.Canvas
import android.graphics.Color
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark

class SquatProcessor(private val thresholds: Map<String, Double>) {
    private var state = "STANDING"
    private var correctCount = 0
    private var incorrectCount = 0
    private var alertMessage = ""

    fun process(pose: Pose, canvas: Canvas?): Pair<Int, Int> {
        val leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP)
        val leftKnee = pose.getPoseLandmark(PoseLandmark.LEFT_KNEE)
        val leftAnkle = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE)

        val rightHip = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP)
        val rightKnee = pose.getPoseLandmark(PoseLandmark.RIGHT_KNEE)
        val rightAnkle = pose.getPoseLandmark(PoseLandmark.RIGHT_ANKLE)

        if (listOf(leftHip, leftKnee, leftAnkle, rightHip, rightKnee, rightAnkle).any { it == null })
            return Pair(correctCount, incorrectCount)

        val leftKneeAngle = Utils.calculateAngle(leftHip!!, leftKnee!!, leftAnkle!!)
        val rightKneeAngle = Utils.calculateAngle(rightHip!!, rightKnee!!, rightAnkle!!)
        val avgKneeAngle = (leftKneeAngle + rightKneeAngle) / 2.0

        val minKnee = thresholds["min_knee_angle"] ?: 60.0
        val maxKnee = thresholds["max_knee_angle"] ?: 160.0

        if (state == "STANDING" && avgKneeAngle <= minKnee) {
            state = "SQUATTING"
        } else if (state == "SQUATTING" && avgKneeAngle >= maxKnee) {
            correctCount++
            alertMessage = "✅ Correcta"
            state = "STANDING"
        } else {
            alertMessage = if (avgKneeAngle > minKnee && avgKneeAngle < maxKnee) {
                "¡BAJA MÁS!"
            } else ""
        }

        canvas?.let {
            Utils.drawText(it, "Sentadillas correctas: $correctCount", 30f, 80f, Color.GREEN)
            Utils.drawText(it, "Incorrectas: $incorrectCount", 30f, 130f, Color.RED)
            Utils.drawText(it, alertMessage, 30f, 180f, Color.YELLOW)
        }

        return Pair(correctCount, incorrectCount)
    }
}
