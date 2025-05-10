package com.rodrigo.fitvision

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.rodrigo.fitvision.databinding.ActivityCameraTrainingBinding

class CameraTrainingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCameraTrainingBinding

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraTrainingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val exercise = intent.getStringExtra("exercise")

        if (exercise != "squats") {
            Toast.makeText(this, "Este ejercicio aún no está disponible", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        binding.webView.apply {
            webViewClient = WebViewClient()
            settings.javaScriptEnabled = true
            settings.allowContentAccess = true
            settings.allowFileAccess = true
            loadUrl("http://192.168.1.85:5000/video_stream")
        }

        binding.btnStop.setOnClickListener {
            finish()
        }
    }

    override fun onDestroy() {
        binding.webView.loadUrl("about:blank")
        super.onDestroy()
    }
}