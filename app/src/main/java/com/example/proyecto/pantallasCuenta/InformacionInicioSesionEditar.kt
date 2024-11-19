package com.example.proyecto.pantallasCuenta

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.proyecto.R
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

class InformacionInicioSesionEditar : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_informacion_inicio_sesion_editar)

        val nuevaContraseniaEditText = findViewById<EditText>(R.id.nuevacontrasenia)
        val repetirContraseniaEditText = findViewById<EditText>(R.id.repetircontrasenia)
        val guardarButton = findViewById<Button>(R.id.button)

        guardarButton.setOnClickListener {
            val nuevaContrasenia = nuevaContraseniaEditText.text.toString()
            val repetirContrasenia = repetirContraseniaEditText.text.toString()

            if (nuevaContrasenia == repetirContrasenia) {
                guardarContraseniaEnJSON(nuevaContrasenia, repetirContrasenia)
            } else {
                // Mostrar mensaje de error si las contraseñas no coinciden
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun guardarContraseniaEnJSON(nuevaContrasenia: String, repetirContrasenia: String) {
        // Crear objeto JSON
        val json = JSONObject().apply {
            put("nuevaContrasenia", nuevaContrasenia)
            put("repetirContrasenia", repetirContrasenia)
        }

        // Nombre del archivo
        val fileName = "contrasenia.json"

        // Guardar en el almacenamiento interno
        try {
            val file = File(filesDir, fileName)
            val fos = FileOutputStream(file)
            fos.write(json.toString().toByteArray())
            fos.close()

            // Mostrar mensaje de éxito
            Toast.makeText(this, "Contraseña guardada exitosamente", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error al guardar contraseña", Toast.LENGTH_SHORT).show()
        }
    }
}
