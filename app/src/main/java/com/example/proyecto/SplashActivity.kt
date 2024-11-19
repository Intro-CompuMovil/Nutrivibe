package com.example.proyecto

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.ImageView
import com.example.proyecto.registro.InicioSesion
import com.example.proyecto.MainActivity
import com.google.firebase.auth.FirebaseAuth

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

        // Verificar si el usuario está logueado
        val user = FirebaseAuth.getInstance().currentUser

        // Navegar a la actividad correspondiente después de la duración del splash
        Handler(Looper.getMainLooper()).postDelayed({
            if (user != null) {
                // Si el usuario está logueado, redirigir a MainActivity
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            } else {
                // Si el usuario no está logueado, redirigir a InicioSesion
                val intent = Intent(this, InicioSesion::class.java)
                startActivity(intent)
            }
            finish() // Finalizar el SplashActivity para que el usuario no pueda volver a él
        }, splashDuration)
    }
}
