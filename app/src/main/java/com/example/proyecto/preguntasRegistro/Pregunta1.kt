package com.example.proyecto.preguntasRegistro

import android.content.Intent
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.proyecto.R

class Pregunta1 : AppCompatActivity() {
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_pregunta1)

        userId = intent.getStringExtra("userId")

        val pesoEditText = findViewById<EditText>(R.id.editTextNumberPeso)
        val alturaEditText = findViewById<EditText>(R.id.editTextNumberAltura)

        val button = findViewById<Button>(R.id.p1ap2)
        button.setOnClickListener {
            val peso = pesoEditText.text.toString()
            val altura = alturaEditText.text.toString()

            val intent = Intent(this, Pregunta2::class.java).apply {
                putExtra("userId", userId)
                putExtra("peso", peso)
                putExtra("altura", altura)
            }
            startActivity(intent)
        }
    }
}

