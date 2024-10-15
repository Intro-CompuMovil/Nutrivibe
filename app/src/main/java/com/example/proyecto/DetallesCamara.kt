package com.example.proyecto

import android.graphics.Bitmap
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class DetallesCamara : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalles_camara)

        // Obtener los componentes de la interfaz
        val imageView = findViewById<ImageView>(R.id.productImage)
        val productTitle = findViewById<TextView>(R.id.productTitle)
        val addImageView = findViewById<ImageView>(R.id.añadirFavoritos)  // ImageView usado como botón

        // Obtener la imagen capturada desde el intent
        val capturedImage = intent.getParcelableExtra<Bitmap>("capturedImage")

        // Mostrar la imagen capturada
        capturedImage?.let {
            imageView.setImageBitmap(it)
        }

        // Establecer el título del producto (esto es un ejemplo, puedes adaptarlo a tu lógica)
        productTitle.text = "Tu producto es: Una pera"

        // Acciones cuando el usuario presiona el ImageView "Añadir alimento"
        addImageView.setOnClickListener {
            Toast.makeText(this, "Alimento añadido a tu lista", Toast.LENGTH_SHORT).show()
            // Aquí puedes añadir la lógica para almacenar o gestionar el alimento agregado
        }
    }
}
