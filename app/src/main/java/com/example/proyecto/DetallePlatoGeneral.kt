package com.example.proyecto

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DetallePlatoGeneral : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_plato_general)

        // Recibe el objeto Plato
        val plato = intent.getParcelableExtra<Plato>("plato")

        if (plato == null) {
            // Log y manejo del error si el objeto no se encuentra
            Log.e("DetallePlatoGeneral", "Plato no encontrado en el Intent")
            Toast.makeText(this, "Error: Plato no encontrado", Toast.LENGTH_SHORT).show()
            finish() // Cierra la actividad si no hay datos válidos
            return
        }

        Log.d("DetallePlatoGeneral", "Plato recibido: $plato")

        // Poblar vistas
        findViewById<TextView>(R.id.textView15).text = plato.nombre
        findViewById<TextView>(R.id.textViewCalorias).text =
            "${plato.informacion_nutricional["calorias"] ?: "0"}"
        findViewById<TextView>(R.id.textViewProteinas).text =
            "${plato.informacion_nutricional["proteinas"] ?: "0"}"
        findViewById<TextView>(R.id.textViewGrasas).text =
            "${plato.informacion_nutricional["grasas"] ?: "0"}"
        findViewById<TextView>(R.id.textViewCarbs).text =
            "${plato.informacion_nutricional["carbohidratos"] ?: "0"}"

        val imageView = findViewById<ImageView>(R.id.imageView16)
        val assetPath = "file:///android_asset/${plato.imagen}"

        Glide.with(this)
            .load(assetPath)
            .error(R.drawable.placeholder_image)
            .into(imageView)

        // Log para la imagen cargada
        Log.d("DetallePlatoGeneral", "Cargando imagen desde: $assetPath")

        // Botones
        findViewById<Button>(R.id.button).setOnClickListener {
            // Step 1: Prompt user to choose meal type (Desayuno, Almuerzo, Cena)
            val mealOptions = arrayOf("Desayuno", "Almuerzo", "Cena")
            AlertDialog.Builder(this)
                .setTitle("Seleccione el tipo de comida")
                .setItems(mealOptions) { _, which ->
                    val selectedMeal = mealOptions[which]

                    // Step 2: Prompt user to select the date
                    showDatePicker { selectedDate ->
                        // Step 3: Check for existing meals in the selected date
                        checkAndAddMeal(selectedMeal, selectedDate)
                    }
                }
                .show()
        }
        findViewById<Button>(R.id.button3).setOnClickListener {
            plato.tutorial?.let { tutorialUrl ->
                val videoId = tutorialUrl.substringAfter("v=").substringBefore("&")
                val embeddedUrl = "https://www.youtube.com/embed/$videoId"
                val intent = Intent(this, TutorialWebViewActivity::class.java)
                intent.putExtra("tutorial_url", embeddedUrl)
                startActivity(intent)
            } ?: run {
                Log.e("DetallePlatoGeneral", "Tutorial URL is null")
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
                val formattedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedCalendar.time)
                onDateSelected(formattedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun checkAndAddMeal(mealType: String, date: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "Usuario no autenticado.", Toast.LENGTH_SHORT).show()
            return
        }

        // Convert mealType to lowercase to match database structure
        val standardizedMealType = when (mealType) {
            "Desayuno" -> "desayunos"
            "Almuerzo" -> "almuerzos"
            "Cena" -> "cenas"
            else -> {
                Log.e("DetallePlatoGeneral", "Tipo de comida no reconocido: $mealType")
                return
            }
        }

        val plansReference = FirebaseDatabase.getInstance().getReference("Plans").child(userId).child(date)

        plansReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Check if the meal already exists
                    val existingMeal = snapshot.child(standardizedMealType).getValue(Plato::class.java)
                    if (existingMeal != null) {
                        // Ask user if they want to replace the meal
                        AlertDialog.Builder(this@DetallePlatoGeneral)
                            .setTitle("Reemplazar Plato")
                            .setMessage("Ya existe un plato para $mealType el $date. ¿Desea reemplazarlo?")
                            .setPositiveButton("Sí") { _, _ ->
                                addMealToDatabase(plansReference, standardizedMealType)
                            }
                            .setNegativeButton("No", null)
                            .show()
                    } else {
                        // No conflict, add meal directly
                        addMealToDatabase(plansReference, standardizedMealType)
                    }
                } else {
                    // No data for the selected date, create a new entry
                    addMealToDatabase(plansReference, standardizedMealType)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("DetallePlatoGeneral", "Error al verificar plato: ${error.message}")
            }
        })
    }


    private fun addMealToDatabase(plansReference: DatabaseReference, mealType: String) {
        // Extract data from views to create a Plato object
        val nombre = findViewById<TextView>(R.id.textView15).text.toString()
        val calorias = findViewById<TextView>(R.id.textViewCalorias).text.toString()
        val proteinas = findViewById<TextView>(R.id.textViewProteinas).text.toString()
        val grasas = findViewById<TextView>(R.id.textViewGrasas).text.toString()
        val carbohidratos = findViewById<TextView>(R.id.textViewCarbs).text.toString()
        val imagen = intent.getParcelableExtra<Plato>("plato")?.imagen ?: ""

        // Create a Plato object using extracted data
        val currentPlato = Plato(
            nombre = nombre,
            imagen = imagen,
            tutorial = intent.getParcelableExtra<Plato>("plato")?.tutorial ?: "",
            informacion_nutricional = mapOf(
                "calorias" to calorias,
                "proteinas" to proteinas,
                "grasas" to grasas,
                "carbohidratos" to carbohidratos
            )
        )

        // Insert the Plato object into the database
        plansReference.child(mealType).setValue(currentPlato)
            .addOnSuccessListener {
                Toast.makeText(this, "Plato añadido con éxito.", Toast.LENGTH_SHORT).show()
                Log.d("DetallePlatoGeneral", "Plato añadido: ${currentPlato.nombre}")
            }
            .addOnFailureListener { e ->
                Log.e("DetallePlatoGeneral", "Error al añadir plato: ${e.message}")
            }
    }

}

