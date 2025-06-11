package com.rodrigo.fitvision

import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.rodrigo.fitvision.databinding.ActivityCameraTrainingBinding
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException

class OnDeviceExerciseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCameraTrainingBinding
    private lateinit var exercise: String
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraTrainingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        exercise = intent.getStringExtra("exercise") ?: "squats"

        binding.btnStop.setOnClickListener {
            finish()
        }

        startFrameLoop()
    }

    private fun startFrameLoop() {
        val handler = Handler(Looper.getMainLooper())
        handler.post(object : Runnable {
            override fun run() {
                captureAndSendFrame()
                handler.postDelayed(this, 1000) // cada 1 segundo
            }
        })
    }

    private fun captureAndSendFrame() {
        val bitmap = binding.previewView.bitmap ?: return

        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
        val byteArray = stream.toByteArray()

        sendFrameToBackend(byteArray)
    }

    private fun sendFrameToBackend(imageData: ByteArray) {
        val requestBody = imageData.toRequestBody("image/jpeg".toMediaTypeOrNull())
        val url = "http://127.0.0.1:5000/analyze?ejercicio=$exercise"

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@OnDeviceExerciseActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    val responseBody = it.body?.string()
                    val feedback = try {
                        JSONObject(responseBody ?: "").optString("feedback", "Sin respuesta")
                    } catch (e: Exception) {
                        "Error en JSON"
                    }

                    runOnUiThread {
                        Toast.makeText(this@OnDeviceExerciseActivity, feedback, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}
