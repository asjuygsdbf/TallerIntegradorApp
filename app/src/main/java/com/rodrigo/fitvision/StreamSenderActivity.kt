package com.rodrigo.fitvision

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.rodrigo.fitvision.databinding.ActivityCameraTrainingBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okio.BufferedSink
import java.io.ByteArrayOutputStream
import java.util.concurrent.Executors

class StreamSenderActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCameraTrainingBinding
    private val scope = CoroutineScope(Dispatchers.IO)
    private val client = OkHttpClient()
    private var lastTimestamp = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraTrainingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        startCameraAndSendStream()
        setupWebView()
        binding.btnStop.setOnClickListener { finish() }
    }

    private fun setupWebView() {
        binding.webView.apply {
            webViewClient = object : WebViewClient() {
                override fun onReceivedError(
                    view: android.webkit.WebView?,
                    errorCode: Int,
                    description: String?,
                    failingUrl: String?
                ) {
                    super.onReceivedError(view, errorCode, description, failingUrl)
                    runOnUiThread {
                        Toast.makeText(
                            this@StreamSenderActivity,
                            "No se pudo cargar el video. Verifica la conexión.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
            settings.javaScriptEnabled = true
            settings.loadWithOverviewMode = true
            settings.useWideViewPort = true
            settings.setSupportZoom(false)
            settings.builtInZoomControls = false
            settings.displayZoomControls = false
            loadUrl("http://192.168.1.85:5000/view_stream")
        }
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun startCameraAndSendStream() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build()

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            imageAnalysis.setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->
                val now = System.currentTimeMillis()
                if (now - lastTimestamp >= 67) { // Aproximadamente 15 fps
                    val bitmap = imageProxy.toBitmapCompat()
                    sendBitmapToServer(bitmap)
                    lastTimestamp = now
                }
                imageProxy.close()
            }

            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis)
            } catch (e: Exception) {
                Log.e("Camera", "Use case binding failed", e)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun sendBitmapToServer(bitmap: Bitmap) {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
        val byteArray = stream.toByteArray()

        scope.launch {
            val requestBody = object : RequestBody() {
                override fun contentType() = "application/octet-stream".toMediaTypeOrNull()
                override fun writeTo(sink: BufferedSink) {
                    sink.write(byteArray)
                }
            }

            val request = Request.Builder()
                .url("http://192.168.1.85:5000/video_stream_upload")
                .post(requestBody)
                .build()

            try {
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        Log.e("Stream", "Error: ${response.code}")
                    }
                }
            } catch (e: Exception) {
                Log.e("Stream", "Exception: ${e.localizedMessage}")
            }
        }
    }
}

fun ImageProxy.toBitmapCompat(): Bitmap {
    val yBuffer = planes[0].buffer
    val uBuffer = planes[1].buffer
    val vBuffer = planes[2].buffer

    val ySize = yBuffer.remaining()
    val uSize = uBuffer.remaining()
    val vSize = vBuffer.remaining()

    val nv21 = ByteArray(ySize + uSize + vSize)
    yBuffer.get(nv21, 0, ySize)
    vBuffer.get(nv21, ySize, vSize)
    uBuffer.get(nv21, ySize + vSize, uSize)

    val yuvImage = android.graphics.YuvImage(nv21, android.graphics.ImageFormat.NV21, width, height, null)
    val out = ByteArrayOutputStream()
    yuvImage.compressToJpeg(android.graphics.Rect(0, 0, width, height), 100, out)
    val imageBytes = out.toByteArray()
    return android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
}