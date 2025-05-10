package com.rodrigo.fitvision

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class ExerciseSelectionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercise_selection)

        // Configuración para Flexiones
        findViewById<MaterialCardView>(R.id.cardPushups).setOnClickListener {
            showExerciseOptions("pushups")
        }

        // Configuración para Sentadillas
        findViewById<MaterialCardView>(R.id.cardSquats).setOnClickListener {
            showExerciseOptions("squats")
        }

        // Configuración para Abdominales
        findViewById<MaterialCardView>(R.id.cardAbs).setOnClickListener {
            showExerciseOptions("abs")
        }
    }

    private fun showExerciseOptions(exerciseType: String) {
        val intent = Intent(this, ExerciseOptionsActivity::class.java)
        intent.putExtra("exercise", exerciseType)
        startActivity(intent)
    }
}