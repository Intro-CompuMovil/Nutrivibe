package com.example.proyecto

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class CameraReducedActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera_reduced)

        // Botón para reintentar otorgar el permiso
        val buttonRetry = findViewById<Button>(R.id.buttonRetryPermission)
        buttonRetry.setOnClickListener {
            // Termina esta actividad y vuelve a la anterior, que solicitará el permiso de nuevo
            finish()
        }
    }
}

