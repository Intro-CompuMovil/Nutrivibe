package com.example.proyecto.preguntasRegistro

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.proyecto.R

class Pregunta2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_pregunta2)

        val button = findViewById<Button>(R.id.p2ap3)
        button.setOnClickListener {
            // Aquí debes obtener los datos específicos de Pregunta2
            // Por ejemplo:
            // val datoPregunta2 = findViewById<EditText>(R.id.tuEditText).text.toString()

            val intent = Intent(this, Pregunta3::class.java).apply {
                putExtras(intent.extras ?: Bundle()) // Mantener datos anteriores
                // putExtra("datoPregunta2", datoPregunta2)
            }
            startActivity(intent)
        }
    }
}