package com.rodrigo.fitvision.core

import android.graphics.Canvas
import android.graphics.Color
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark

class PushUpProcessor(private val thresholds: Thresholds.ThresholdSet) {
    private var state = "UP"
    private var correctCount = 0
    private var incorrectCount = 0
    private var alertMessage = ""

    fun process(pose: Pose, canvas: Canvas?): Pair<Int, Int> {
        val leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
        val leftElbow = pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW)
        val leftWrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST)

        val rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)
        val rightElbow = pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW)
        val rightWrist = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST)

        val leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP)
        val rightHip = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP)

        val leftAnkle = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE)
        val rightAnkle = pose.getPoseLandmark(PoseLandmark.RIGHT_ANKLE)

        if (listOf(
                leftShoulder,
                leftElbow,
                leftWrist,
                rightShoulder,
                rightElbow,
                rightWrist,
                leftHip,
                rightHip,
                leftAnkle,
                rightAnkle
            ).any { it == null }) return Pair(correctCount, incorrectCount)

        val leftElbowAngle = Utils.calculateAngle(leftShoulder!!, leftElbow!!, leftWrist!!)
        val rightElbowAngle = Utils.calculateAngle(rightShoulder!!, rightElbow!!, rightWrist!!)
        val avgElbowAngle = (leftElbowAngle + rightElbowAngle) / 2

        val shoulderY = (leftShoulder.position.y + rightShoulder.position.y) / 2
        val hipY = (leftHip.position.y + rightHip.position.y) / 2
        val ankleY = (leftAnkle.position.y + rightAnkle.position.y) / 2

        val alignmentOffset = ((hipY - shoulderY) - (ankleY - hipY)) / (ankleY - shoulderY)

        val formCorrect = alignmentOffset in -thresholds.maxFormDeviationOffset..thresholds.maxFormDeviationOffset

        if (state == "UP" && avgElbowAngle <= thresholds.minElbowFlexionForCount) {
            state = "DOWN"
        } else if (state == "DOWN" && avgElbowAngle >= thresholds.maxElbowExtensionForCount) {
            if (formCorrect) {
                correctCount++
                alertMessage = "✅ Correcta"
            } else {
                incorrectCount++
                alertMessage = "🟠 Cuerpo torcido"
            }
            state = "UP"
        } else {
            if (!formCorrect) {
                alertMessage = "¡MANTÉN EL CUERPO RECTO!"
            } else if (avgElbowAngle > 90 && avgElbowAngle < 160) {
                alertMessage = "¡BAJA MÁS LOS CODOS!"
            } else {
                alertMessage = ""
            }
        }

        canvas?.let {
            Utils.drawText(it, "Flexiones correctas: $correctCount", 30f, 80f, Color.GREEN)
            Utils.drawText(it, "Incorrectas: $incorrectCount", 30f, 130f, Color.RED)
            Utils.drawText(it, alertMessage, 30f, 180f, Color.YELLOW)
        }

        return Pair(correctCount, incorrectCount)
    }
}