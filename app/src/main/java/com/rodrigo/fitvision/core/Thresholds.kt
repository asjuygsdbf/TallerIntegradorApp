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
                "FLEXED" to AngleRange(20, 89)
            ),
            hipAlignment = mapOf(
                "TOO_HIGH" to OffsetRange(0.25, Double.POSITIVE_INFINITY),
                "GOOD_ALIGN" to OffsetRange(-0.25, 0.25),
                "TOO_LOW" to OffsetRange(Double.NEGATIVE_INFINITY, -0.25)
            ),
            chestToGround = mapOf(
                "FAR" to OffsetRange(0.3, Double.POSITIVE_INFINITY),
                "CLOSE" to OffsetRange(0.0, 0.3)
            ),
            minElbowFlexionForCount = 70,
            maxElbowExtensionForCount = 165,
            maxFormDeviationOffset = 0.25,
            inactiveFramesThresh = 15,
            postureHoldFramesThresh = 30
        )
    }

    fun getSquatThresholds(): ThresholdSet {
        return ThresholdSet(
            elbowAngle = mapOf(
                "FLEXED" to AngleRange(40, 90),
                "EXTENDED" to AngleRange(130, 180)
            ),
            hipAlignment = mapOf(
                "TOO_HIGH" to OffsetRange(0.15, Double.POSITIVE_INFINITY),
                "GOOD_ALIGN" to OffsetRange(-0.25, 0.25),
                "TOO_LOW" to OffsetRange(Double.NEGATIVE_INFINITY, -0.15)
            ),
            chestToGround = mapOf(
                "CLOSE" to OffsetRange(0.0, 0.2),
                "FAR" to OffsetRange(0.2, Double.POSITIVE_INFINITY)
            ),
            minElbowFlexionForCount = 60,
            maxElbowExtensionForCount = 160,
            maxFormDeviationOffset = 0.20,
            inactiveFramesThresh = 15,
            postureHoldFramesThresh = 20
        )
    }

    fun getAbdominalThresholds(): ThresholdSet {
        return ThresholdSet(
            elbowAngle = mapOf(
                "FLEXED" to AngleRange(40, 90),
                "EXTENDED" to AngleRange(130, 180)
            ),
            hipAlignment = mapOf(
                "TOO_HIGH" to OffsetRange(0.15, Double.POSITIVE_INFINITY),
                "GOOD_ALIGN" to OffsetRange(-0.25, 0.25),
                "TOO_LOW" to OffsetRange(Double.NEGATIVE_INFINITY, -0.15)
            ),
            chestToGround = mapOf(
                "FAR" to OffsetRange(0.2, Double.POSITIVE_INFINITY),
                "CLOSE" to OffsetRange(0.0, 0.2)
            ),
            minElbowFlexionForCount = 60,
            maxElbowExtensionForCount = 150,
            maxFormDeviationOffset = 0.25,
            inactiveFramesThresh = 15,
            postureHoldFramesThresh = 20
        )
    }
}