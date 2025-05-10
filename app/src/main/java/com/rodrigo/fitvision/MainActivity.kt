package com.rodrigo.fitvision

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnStart = findViewById<MaterialButton>(R.id.btnStart)
        btnStart.setOnClickListener {
            val intent = Intent(this, ExerciseSelectionActivity::class.java)
            startActivity(intent)
        }
    }
}