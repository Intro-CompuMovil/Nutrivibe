package com.example.proyecto

import android.content.Intent
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.style.CharacterStyle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Home : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setupGradientText()

        val boton = findViewById<Button>(R.id.buttonHome)
        val intent = Intent(this, MainActivity::class.java)
        boton.setOnClickListener(){
            startActivity(intent)
        }

    }

    private fun setupGradientText() {
        val channelName = findViewById<TextView>(R.id.textNombre)
        channelName.text = getString(R.string.channel_name)

        val paint = channelName.paint
        val width = paint.measureText(getString(R.string.channel_name))

        val textShader = LinearGradient(
            0f, 0f, width, channelName.textSize,
            intArrayOf(
                Color.parseColor("#C5ABD8"),
                Color.parseColor("#8EBAE5"),
            ),
            null, Shader.TileMode.CLAMP
        )

        channelName.paint.shader = textShader
    }
}