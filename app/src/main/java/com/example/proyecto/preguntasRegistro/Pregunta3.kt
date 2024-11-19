package com.example.proyecto.preguntasRegistro

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.proyecto.Home
import com.example.proyecto.MainActivity
import com.example.proyecto.R

class Pregunta3 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_pregunta3)

        val button = findViewById<Button>(R.id.p3ah)
        button.setOnClickListener {
            // Aquí debes obtener los datos específicos de Pregunta3
            // Por ejemplo:
            // val datoPregunta3 = findViewById<EditText>(R.id.tuEditText).text.toString()

            val intent = Intent(this, Pregunta4::class.java).apply {
                putExtras(intent.extras ?: Bundle()) // Mantener datos anteriores
                // putExtra("datoPregunta3", datoPregunta3)
            }
            startActivity(intent)
        }
    }
}