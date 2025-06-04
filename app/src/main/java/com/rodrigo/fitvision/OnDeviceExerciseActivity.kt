package com.rodrigo.fitvision

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

        // Redibuja constantemente el overlay cada 100ms
        val handler = android.os.Handler(mainLooper)
        val redrawRunnable = object : Runnable {
            override fun run() {
                binding.overlayView.invalidate()
                handler.postDelayed(this, 100)
            }
        }
        handler.post(redrawRunnable)
    }

    private fun initProcessors() {
        when (exercise) {
            "pushups" -> {
            }
            "squats" -> squatProcessor = SquatProcessor(Thresholds.getSquatThresholds())
            "abs" -> abdominalProcessor = AbdominalProcessor(Thresholds.getAbdominalThresholds())
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

            val options = AccuratePoseDetectorOptions.Builder()
                .setDetectorMode(AccuratePoseDetectorOptions.STREAM_MODE)
                .build()

            poseDetector = PoseDetection.getClient(options)

            imageAnalysis.setAnalyzer(
                Executors.newSingleThreadExecutor()
            ) { imageProxy ->
                @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
                val mediaImage = imageProxy.image
                if (mediaImage != null) {
                    val rotationDegrees = imageProxy.imageInfo.rotationDegrees
                    val image = InputImage.fromMediaImage(mediaImage, rotationDegrees)

                    poseDetector.process(image)
                        .addOnSuccessListener { pose ->
                            Log.d("POSE", "Landmarks detectados: ${pose.allPoseLandmarks.size}")

                            // Obtenemos dimensiones del frame
                            val frameWidth = image.width.toFloat()
                            val frameHeight = image.height.toFloat()

                            // Inicializar procesador con dimensiones reales
                            if (exercise == "pushups" && pushupProcessor == null) {
                                pushupProcessor = PushUpProcessor(
                                    Thresholds.getPushUpThresholds(),
                                    frameWidth,
                                    frameHeight
                                )
                            }

                            binding.overlayView.setDrawCallback { canvas ->
                                when (exercise) {
                                    "pushups" -> pushupProcessor?.process(pose, canvas)
                                    "squats" -> squatProcessor?.process(pose, canvas)
                                    "abs" -> abdominalProcessor?.process(pose, canvas)
                                }
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e("PoseDetection", "Error detecting pose", e)
                        }
                        .addOnCompleteListener {
                            imageProxy.close()
                        }
                } else {
                    imageProxy.close()
                }
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
                Log.e("CameraSetup", "Error binding camera use cases", e)
                Toast.makeText(this, "Error al iniciar cámara", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::poseDetector.isInitialized) {
            poseDetector.close()
        }
    }
}