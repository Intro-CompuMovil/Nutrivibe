package com.example.proyecto

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ReducedFunctionalityActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reduced_functionality)

        // Recoger el mensaje de la intención
        val message = intent.getStringExtra("message")

        // Mostrar el mensaje en el TextView
        val textView = findViewById<TextView>(R.id.textMessage)
        textView.text = message ?: "Funcionalidades reducidas"
    }
}
