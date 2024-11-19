package com.example.proyecto

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class GpsGrantedActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gps_granted)

        // Recoger el mensaje de la intención
        val message = intent.getStringExtra("message")

        // Mostrar el mensaje en el TextView
        val textView = findViewById<TextView>(R.id.textMessage)
        textView.text = message ?: "Permiso concedido"
    }
}
