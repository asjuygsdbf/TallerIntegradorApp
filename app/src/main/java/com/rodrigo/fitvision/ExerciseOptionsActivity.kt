package com.rodrigo.fitvision

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class ExerciseOptionsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercise_options)

        val exercise = intent.getStringExtra("exercise")
        
        findViewById<MaterialButton>(R.id.btnWatchVideo).setOnClickListener {
            val intent = Intent(this, VideoTutorialActivity::class.java)
            intent.putExtra("exercise", exercise)
            startActivity(intent)
        }

        findViewById<MaterialButton>(R.id.btnStartExercise).setOnClickListener {
            val nextActivity = OnDeviceExerciseActivity::class.java
            val intent = Intent(this, nextActivity)
            intent.putExtra("exercise", exercise)
            startActivity(intent)
        }
    }
}