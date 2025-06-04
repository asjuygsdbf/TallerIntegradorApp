package com.rodrigo.fitvision

import android.graphics.Canvas
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseDetector
import com.google.mlkit.vision.pose.PoseDetectorOptions
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions
import com.rodrigo.fitvision.core.*
import com.rodrigo.fitvision.databinding.ActivityCameraTrainingBinding
import java.util.concurrent.Executors

class OnDeviceExerciseActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCameraTrainingBinding
    private lateinit var cameraProvider: ProcessCameraProvider
    private lateinit var poseDetector: PoseDetector
    private var exercise: String = "squats"

    private var pushupProcessor: PushUpProcessor? = null
    private var squatProcessor: SquatProcessor? = null
    private var abdominalProcessor: AbdominalProcessor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraTrainingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        exercise = intent.getStringExtra("exercise") ?: "squats"
        initProcessors()
        setupCamera()

        binding.btnStop.setOnClickListener { finish() }
    }

    private fun initProcessors() {
        when (exercise) {
            "pushups" -> pushupProcessor = PushUpProcessor(Thresholds.getPushUpThresholds())
            "squats" -> squatProcessor = SquatProcessor(Thresholds.getSquatThresholds())
            "abdominal" -> abdominalProcessor = AbdominalProcessor()
        }
    }

    private fun setupCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.previewView.surfaceProvider)
            }

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            poseDetector = PoseDetection.getClient(
                AccuratePoseDetectorOptions.Builder()
                    .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
                    .build()
            )

            imageAnalysis.setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->
                processFrame(imageProxy)
            }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    CameraSelector.DEFAULT_FRONT_CAMERA,
                    preview,
                    imageAnalysis
                )
            } catch (e: Exception) {
                Toast.makeText(this, "Error al iniciar cámara", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    @androidx.camera.core.ExperimentalGetImage
    private fun processFrame(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image ?: run {
            imageProxy.close()
            return
        }

        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        poseDetector.process(image)
            .addOnSuccessListener { pose ->
                val bitmap = binding.previewView.bitmap ?: return@addOnSuccessListener
                val canvas = Canvas(bitmap)

                when (exercise) {
                    "pushups" -> pushupProcessor?.process(pose, canvas)
                    "squats" -> squatProcessor?.process(pose, canvas)
                    "abdominal" -> abdominalProcessor?.process(pose, canvas)
                }

                binding.previewView.invalidate()
            }
            .addOnFailureListener {
                it.printStackTrace()
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        poseDetector.close()
    }
}
