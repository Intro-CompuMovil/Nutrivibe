package com.example.proyecto.preguntasRegistro

import android.content.Intent
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.proyecto.R

class Pregunta1 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_pregunta1)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

    setupGradientText()
        val button = findViewById<Button>(R.id.p1ap2)
        button.setOnClickListener {
            val intent = Intent(this, Pregunta2::class.java)
            startActivity(intent)
        }
}
private fun setupGradientText() {
    val channelName = findViewById<TextView>(R.id.textNombre)
    channelName.text = "Acerca de ti"

    val paint = channelName.paint
    val width = paint.measureText(getString(R.string.channel_name))

    val textShader = LinearGradient(
        0f, 0f, width, channelName.textSize,
        intArrayOf(
            Color.parseColor("#006C69"),
            Color.parseColor("#17A41B"),
        ),
        null, Shader.TileMode.CLAMP
    )

    channelName.paint.shader = textShader
}
}