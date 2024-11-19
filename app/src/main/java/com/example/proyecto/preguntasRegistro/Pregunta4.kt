package com.example.proyecto.preguntasRegistro

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TimePicker
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.proyecto.R

class Pregunta4 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pregunta4)

        val timePickerDesayuno = findViewById<TimePicker>(R.id.timePickerDesayuno)
        val timePickerAlmuerzo = findViewById<TimePicker>(R.id.timePickerAlmuerzo)
        val button = findViewById<Button>(R.id.button)

        timePickerDesayuno.setIs24HourView(true)
        timePickerAlmuerzo.setIs24HourView(true)

        button.setOnClickListener {
            val horaDesayuno = "${timePickerDesayuno.hour}:${timePickerDesayuno.minute}"
            val horaAlmuerzo = "${timePickerAlmuerzo.hour}:${timePickerAlmuerzo.minute}"

            val intent = Intent(this, Pregunta5::class.java).apply {
                putExtras(intent.extras ?: Bundle()) // Mantener datos anteriores
                putExtra("horaDesayuno", horaDesayuno)
                putExtra("horaAlmuerzo", horaAlmuerzo)
            }
            startActivity(intent)
        }
    }
}