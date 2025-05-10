package com.rodrigo.fitvision

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class VideoTutorialActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_tutorial)

        val videoView = findViewById<VideoView>(R.id.videoView)
        val exercise = intent.getStringExtra("exercise")

        val videoUris = mapOf(
            "pushups" to "https://sample-videos.com/video123/mp4/720/big_buck_bunny_720p_1mb.mp4",
            "squats" to "https://sample-videos.com/video123/mp4/720/big_buck_bunny_720p_2mb.mp4",
            "abs" to "https://sample-videos.com/video123/mp4/720/big_buck_bunny_720p_5mb.mp4"
        )

        exercise?.let { currentExercise ->
            videoUris[currentExercise]?.let { videoUrl ->
                try {
                    videoView.setVideoURI(Uri.parse(videoUrl))
                    videoView.setOnPreparedListener { mediaPlayer ->
                        mediaPlayer.isLooping = true
                        videoView.start()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        findViewById<MaterialButton>(R.id.btnBack).setOnClickListener {
            finish()
        }
    }
}