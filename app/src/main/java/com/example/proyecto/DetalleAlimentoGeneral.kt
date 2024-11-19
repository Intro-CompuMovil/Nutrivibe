package com.example.proyecto

import android.app.DatePickerDialog
import android.content.Intent
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

class DetalleAlimentoGeneral : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_alimento_general)

        // References to views
        val imageView = findViewById<ImageView>(R.id.imageView16)
        val textViewName = findViewById<TextView>(R.id.textView15)
        val textViewProteinas = findViewById<TextView>(R.id.textViewProteinas)
        val textViewCalorias = findViewById<TextView>(R.id.textViewCalorias)
        val textViewGrasas = findViewById<TextView>(R.id.textViewGrasas)
        val textViewCarbs = findViewById<TextView>(R.id.textViewCarbs)
        val buttonAddToExtras = findViewById<Button>(R.id.button)
        val buttonViewLocations = findViewById<Button>(R.id.button3)

        // Retrieve data from the Intent
        val alimentoMap = intent.getSerializableExtra("alimento") as? HashMap<String, Any>
        Log.d("DetalleAlimentoGeneral", "Received alimentoMap: $alimentoMap")

        if (alimentoMap != null) {
            try {
                // Extract data and create `Alimento` object
                val name = alimentoMap["name"] as? String ?: ""
                val image = alimentoMap["image"] as? String ?: ""
                val infoNutricionMap = alimentoMap["info_nutricion"] as? Map<String, Any> ?: emptyMap()
                val supermarketsList = alimentoMap["supermarkets"] as? List<HashMap<String, Any>> ?: emptyList()

                val infoNutricion = infoNutricionMap.mapValues { it.value.toString().toDoubleOrNull() ?: 0.0 }
                val supermarkets = supermarketsList.map {
                    Supermarket(
                        name = it["name"] as? String ?: "",
                        lat = it["lat"] as? Double ?: 0.0,
                        lon = it["lon"] as? Double ?: 0.0
                    )
                }

                val alimento = Alimento(
                    name = name,
                    image = image,
                    info_nutricion = infoNutricion,
                    supermarkets = supermarkets
                )

                Log.d("DetalleAlimentoGeneral", "Processed alimento: $alimento")

                // Set data to UI components
                textViewName.text = alimento.name
                textViewProteinas.text = "${alimento.info_nutricion["proteinas"] ?: 0.0} g"
                textViewCalorias.text = "${alimento.info_nutricion["calorias"] ?: 0.0} kcal"
                textViewGrasas.text = "${alimento.info_nutricion["grasas"] ?: 0.0} g"
                textViewCarbs.text = "${alimento.info_nutricion["carbohidratos"] ?: 0.0} g"

                // Load the image using Glide
                val imagePath = if (alimento.image.startsWith("http")) alimento.image else "file:///android_asset/${alimento.image}"
                Glide.with(this)
                    .load(imagePath)
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.error)
                    .into(imageView)

                // Add to Extras Button
                buttonAddToExtras.setOnClickListener {
                    showDatePicker { selectedDate ->
                        addAlimentoToExtras(alimento, selectedDate)
                    }
                }

                // View Locations Button
                buttonViewLocations.setOnClickListener {
                    if (supermarkets.isNotEmpty()) {
                        Log.d("DetalleAlimentoGeneral", "Supermarkets available: $supermarkets")
                        val intent = Intent(this, UbicacionAlimentoActivity::class.java)
                        intent.putExtra("supermarkets", ArrayList(supermarkets))
                        startActivity(intent)
                    } else {
                        Log.e("DetalleAlimentoGeneral", "No supermarkets found for this alimento.")
                        Toast.makeText(this, "No hay ubicaciones disponibles para este alimento.", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("DetalleAlimentoGeneral", "Error al procesar alimento: ${e.message}")
                Toast.makeText(this, "No se pudo cargar la información del alimento.", Toast.LENGTH_SHORT).show()
                finish()
            }
        } else {
            Log.e("DetalleAlimentoGeneral", "alimentoMap is null")
            Toast.makeText(this, "No se pudo cargar la información del alimento.", Toast.LENGTH_SHORT).show()
            finish()
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

    private fun addAlimentoToExtras(alimento: Alimento<Double>, date: String) {
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
                        this@DetalleAlimentoGeneral,
                        "No te sobrelimites, este alimento ya existe en tus extras.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    extrasReference.push().setValue(alimento)
                        .addOnSuccessListener {
                            Toast.makeText(
                                this@DetalleAlimentoGeneral,
                                "Alimento añadido a extras.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        .addOnFailureListener { e ->
                            Log.e("DetalleAlimentoGeneral", "Error añadiendo alimento: ${e.message}")
                        }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("DetalleAlimentoGeneral", "Error al acceder a extras: ${error.message}")
            }
        })
    }
}
