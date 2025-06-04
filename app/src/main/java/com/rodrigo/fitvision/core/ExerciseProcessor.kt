package com.rodrigo.fitvision.core

import android.graphics.Canvas
import android.graphics.Paint
import com.google.mlkit.vision.pose.Pose

abstract class ExerciseProcessor {
    var goodCount: Int = 0
    var badCount: Int = 0
    abstract fun process(pose: Pose, canvas: Canvas, paint: Paint): String
}