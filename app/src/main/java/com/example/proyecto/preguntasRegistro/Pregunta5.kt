package com.example.proyecto.preguntasRegistro

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TimePicker
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.proyecto.MainActivity
import com.example.proyecto.R
import com.example.proyecto.registro.InicioSesion
import com.google.firebase.database.FirebaseDatabase

class Pregunta5 : AppCompatActivity() {
    private val database = FirebaseDatabase.getInstance()
    private val usersRef = database.getReference("users")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pregunta5)

        val timePickerCena = findViewById<TimePicker>(R.id.timePickerCena)
        val timePickerSnack = findViewById<TimePicker>(R.id.timePickerSnack)
        val button = findViewById<Button>(R.id.button)

        timePickerCena.setIs24HourView(true)
        timePickerSnack.setIs24HourView(true)

        button.setOnClickListener {
            val horaCena = "${timePickerCena.hour}:${timePickerCena.minute}"
            val horaSnack = "${timePickerSnack.hour}:${timePickerSnack.minute}"

            val extras = intent.extras
            val userId = extras?.getString("userId")

            // Recopila todos los datos
            val datosCompletos = mapOf(
                "peso" to extras?.getString("peso", ""),
                "altura" to extras?.getString("altura", ""),
                "horaDesayuno" to extras?.getString("horaDesayuno", ""),
                "horaAlmuerzo" to extras?.getString("horaAlmuerzo", ""),
                "horaCena" to horaCena,
                "horaSnack" to horaSnack,
                "registroCompleto" to true
            )

            // Guarda en Firebase
            userId?.let { uid ->
                usersRef.child(uid).updateChildren(datosCompletos)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Datos guardados exitosamente", Toast.LENGTH_SHORT).show()
                        // Navegar a MainActivity
                        startActivity(Intent(this, InicioSesion::class.java))
                        finishAffinity() // Cierra todas las actividades anteriores
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error al guardar los datos: ${e.message}",
                            Toast.LENGTH_LONG).show()
                    }
            } ?: run {
                Toast.makeText(this, "Error: No se encontró el ID de usuario",
                    Toast.LENGTH_LONG).show()
            }
        }
    }
}