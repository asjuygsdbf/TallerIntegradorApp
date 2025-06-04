package com.rodrigo.fitvision.core

object Thresholds {

    data class AngleRange(val min: Int, val max: Int)
    data class OffsetRange(val min: Double, val max: Double)

    data class ThresholdSet(
        val elbowAngle: Map<String, AngleRange>,
        val hipAlignment: Map<String, OffsetRange>,
        val chestToGround: Map<String, OffsetRange>,
        val minElbowFlexionForCount: Int,
        val maxElbowExtensionForCount: Int,
        val maxFormDeviationOffset: Double,
        val inactiveFramesThresh: Int,
        val postureHoldFramesThresh: Int
    )

    fun getPushUpThresholds(): ThresholdSet {
        return ThresholdSet(
            elbowAngle = mapOf(
                "EXTENDED" to AngleRange(160, 180),
                "TRANSITION" to AngleRange(90, 159),
                "FLEXED" to AngleRange(0, 90)
            ),
            hipAlignment = mapOf(
                "TOO_HIGH" to OffsetRange(0.15, Double.POSITIVE_INFINITY),
                "GOOD_ALIGN" to OffsetRange(-0.15, 0.15),
                "TOO_LOW" to OffsetRange(Double.NEGATIVE_INFINITY, -0.15)
            ),
            chestToGround = mapOf(
                "FAR" to OffsetRange(0.3, Double.POSITIVE_INFINITY),
                "CLOSE" to OffsetRange(0.0, 0.3)
            ),
            minElbowFlexionForCount = 85,
            maxElbowExtensionForCount = 170,
            maxFormDeviationOffset = 0.20,
            inactiveFramesThresh = 15,
            postureHoldFramesThresh = 30
        )
    }

    fun getSquatThresholds(): Map<String, Double> {
        return mapOf(
            "min_knee_angle" to 60.0,
            "max_knee_angle" to 160.0,
            "hip_knee_alignment_tol" to 0.15,
            "inactivity_timeout" to 3.5
        )
    }

    fun getAbdominalThresholds(): Map<String, Any> {
        return mapOf(
            "placeholder" to true
        )
    }
}