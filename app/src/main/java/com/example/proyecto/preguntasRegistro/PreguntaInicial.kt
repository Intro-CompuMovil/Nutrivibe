package com.example.proyecto.preguntasRegistro

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.proyecto.MainActivity
import com.example.proyecto.R
import com.example.proyecto.registro.InicioSesion
import com.example.proyecto.registro.RegistroInicial

class PreguntaInicial : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pregunta_inicial)

        // Configuración de las insets del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Botón para ir a MainActivity
        val button = findViewById<Button>(R.id.piap1)
        button.setOnClickListener {
            val intent = Intent(this, RegistroInicial::class.java)
            startActivity(intent)
        }
    }
}
