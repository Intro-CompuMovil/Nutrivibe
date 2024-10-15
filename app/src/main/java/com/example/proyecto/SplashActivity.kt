package com.example.proyecto

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.ImageView
import com.example.proyecto.preguntasRegistro.PreguntaInicial

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Encuentra el ImageView que contiene el logo
        val logoImageView = findViewById<ImageView>(R.id.logoImageView)

        // Cargar la animación de fade_in desde la carpeta anim
        val fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in)

        // Aplicar la animación al logo
        logoImageView.startAnimation(fadeInAnimation)

        // Duración del splash screen (3 segundos)
        val splashDuration = 5000L

        // Navegar a la MainActivity después de 3 segundos
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, PreguntaInicial::class.java)
            startActivity(intent)
            finish() // Finalizar el SplashActivity para que el usuario no pueda volver a él
        }, splashDuration)
    }
}
