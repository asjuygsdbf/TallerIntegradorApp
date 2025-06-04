package com.rodrigo.fitvision.core

import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark

object PoseLandmarksHelper {

    fun Pose.getLandmarkSafe(type: Int): PoseLandmark? {
        return try {
            this.getPoseLandmark(type)
        } catch (e: Exception) {
            null
        }
    }

    fun Pose.hasLandmarks(vararg types: Int): Boolean {
        return types.all { this.getPoseLandmark(it) != null }
    }
}