package com.example.proyecto

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DetallesMisAlimentosGeneral : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalles_mis_alimentos_general)

        // Obtener referencias a las vistas
        val imageView = findViewById<ImageView>(R.id.imageView16)
        val textViewName = findViewById<TextView>(R.id.textView15)
        val textViewProteinas = findViewById<TextView>(R.id.textViewProteinas)
        val textViewCalorias = findViewById<TextView>(R.id.textViewCalorias)
        val textViewGrasas = findViewById<TextView>(R.id.textViewGrasas)
        val textViewCarbs = findViewById<TextView>(R.id.textViewCarbs)
        val buttonAddToExtras = findViewById<Button>(R.id.button)

        // Obtener datos del Intent
        val name = intent.getStringExtra("name") ?: "Sin nombre"
        val imageUrl = intent.getStringExtra("imageUrl")
        val proteinas = intent.getStringExtra("proteinas") ?: "0"
        val calorias = intent.getStringExtra("calorias") ?: "0"
        val grasas = intent.getStringExtra("grasas") ?: "0"
        val carbohidratos = intent.getStringExtra("carbohidratos") ?: "0"

        // Configurar los datos en la UI
        textViewName.text = name
        textViewProteinas.text = "$proteinas g"
        textViewCalorias.text = "$calorias kcal"
        textViewGrasas.text = "$grasas g"
        textViewCarbs.text = "$carbohidratos g"

        // Cargar la imagen con Glide
        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.placeholder_image)
                .into(imageView)
        } else {
            imageView.setImageResource(R.drawable.placeholder_image) // Imagen de error
        }

        // Configurar acción del botón "Añadir a Extras"
        buttonAddToExtras.setOnClickListener {
            showDatePicker { selectedDate ->
                val alimento = Alimento(
                    name = name,
                    image = imageUrl ?: "",
                    info_nutricion = mapOf(
                        "proteinas" to proteinas,
                        "calorias" to calorias,
                        "grasas" to grasas,
                        "carbohidratos" to carbohidratos
                    )
                )

                addAlimentoToExtras(alimento, selectedDate)
            }
        }

    }

    private fun showDatePicker(onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val selectedCalendar = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                }
                val formattedDate = SimpleDateFormat(
                    "yyyy-MM-dd",
                    Locale.getDefault()
                ).format(selectedCalendar.time)
                onDateSelected(formattedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun addAlimentoToExtras(alimento: Alimento<String>, date: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "Usuario no autenticado.", Toast.LENGTH_SHORT).show()
            return
        }

        val extrasReference = FirebaseDatabase.getInstance()
            .getReference("extras")
            .child(userId)
            .child(date)

        extrasReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val currentExtras = snapshot.children.mapNotNull { it.getValue(Alimento::class.java) }
                val existingAlimento = currentExtras.find { it.name == alimento.name }

                if (existingAlimento != null) {
                    Toast.makeText(
                        this@DetallesMisAlimentosGeneral,
                        "No te sobrelimites, este alimento ya existe en tus extras.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    // Agregar el nuevo Alimento
                    extrasReference.push().setValue(alimento)
                        .addOnSuccessListener {
                            Toast.makeText(
                                this@DetallesMisAlimentosGeneral,
                                "Alimento añadido a extras.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        .addOnFailureListener { e ->
                            Log.e("DetallesMisAlimentosGeneral", "Error añadiendo alimento: ${e.message}")
                        }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("DetallesMisAlimentosGeneral", "Error al acceder a extras: ${error.message}")
            }
        })
    }
}
