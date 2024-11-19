package com.example.proyecto

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ExtrasActivity : AppCompatActivity() {

    private lateinit var recyclerViewExtras: RecyclerView
    private lateinit var extrasAdapter: ExtrasAdapter
    private lateinit var tvNoExtrasMessage: TextView

    // Change extrasList to use Alimento<String>
    private val extrasList = mutableListOf<Alimento<String>>()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private val databaseReference = FirebaseDatabase.getInstance().getReference("extras")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_extras)

        Log.d("ExtrasActivity", "Activity created.")

        // Initialize views
        recyclerViewExtras = findViewById(R.id.recyclerViewExtras)
        tvNoExtrasMessage = findViewById(R.id.tvNoExtrasMessage)

        recyclerViewExtras.layoutManager = LinearLayoutManager(this)

        // Convert extrasList to List<Alimento<String>> for adapter
        val convertedExtrasList = extrasList.map { alimento ->
            Alimento(
                name = alimento.name,
                image = alimento.image,
                info_nutricion = alimento.info_nutricion.mapValues { it.value.toString() },
                supermarkets = alimento.supermarkets
            )
        }

        // Set adapter with convertedExtrasList
        extrasAdapter = ExtrasAdapter(convertedExtrasList) { convertedItem ->
            Log.d("ExtrasActivity", "Clicked item: $convertedItem")

            // Convert back to Alimento<Double> for further processing
            val alimentoDouble = Alimento(
                name = convertedItem.name,
                image = convertedItem.image,
                info_nutricion = convertedItem.info_nutricion.mapValues {
                    it.value.toDoubleOrNull() ?: 0.0
                },
                supermarkets = convertedItem.supermarkets
            )
            navigateToDetalleAlimento(alimentoDouble)
        }
        recyclerViewExtras.adapter = extrasAdapter

        // Get the selected date from the intent
        val selectedDate = intent.getStringExtra("selectedDate") ?: getTodayDate()
        Log.d("ExtrasActivity", "Selected date: $selectedDate")

        // Load extras for the selected date
        loadExtrasForDate(selectedDate)
    }


    private fun getTodayDate(): String {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().time)
        Log.d("ExtrasActivity", "Today's date: $today")
        return today
    }

    private fun loadExtrasForDate(date: String) {
        if (userId == null) {
            Log.e("ExtrasActivity", "User is not authenticated.")
            Toast.makeText(this, "Usuario no autenticado.", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d("ExtrasActivity", "Fetching extras for user ID: $userId on date: $date")

        databaseReference.child(userId).child(date).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val loadedExtras = mutableListOf<Alimento<String>>() // Temporary list for data
                Log.d("ExtrasActivity", "Data snapshot exists: ${snapshot.exists()} for date: $date")

                if (snapshot.exists()) {
                    snapshot.children.forEach { child ->
                        val alimentoMap = child.getValue(object : GenericTypeIndicator<Map<String, Any>>() {})
                        Log.d("ExtrasActivity", "Fetched child data: $alimentoMap")

                        if (alimentoMap != null) {
                            try {
                                val name = alimentoMap["name"] as? String ?: ""
                                val image = alimentoMap["image"] as? String ?: ""
                                val infoNutricionMap = alimentoMap["info_nutricion"] as? Map<String, Any> ?: emptyMap()
                                val infoNutricion = infoNutricionMap.mapValues { it.value.toString() }

                                val alimento = Alimento(
                                    name = name,
                                    image = image,
                                    info_nutricion = infoNutricion,
                                    supermarkets = emptyList() // Update if supermarkets are needed
                                )
                                loadedExtras.add(alimento)
                                Log.d("ExtrasActivity", "Added alimento to temporary list: $alimento")
                            } catch (e: Exception) {
                                Log.e("ExtrasActivity", "Error processing alimento: ${e.message}")
                            }
                        }
                    }
                }

                if (loadedExtras.isEmpty()) {
                    Log.d("ExtrasActivity", "No extras found for date: $date")
                    showNoExtrasMessage()
                } else {
                    Log.d("ExtrasActivity", "Loaded extras: $loadedExtras")
                    showExtras(loadedExtras)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ExtrasActivity", "Error loading extras: ${error.message}")
                showNoExtrasMessage()
            }
        })
    }

    private fun showExtras(loadedExtras: List<Alimento<String>>) {
        // Update the adapter directly
        Log.d("ExtrasActivity", "Updating adapter with loadedExtras: $loadedExtras")
        extrasAdapter.updateData(loadedExtras)
    }


    private fun navigateToDetalleAlimento(alimento: Alimento<Double>) {
        Log.d("ExtrasActivity", "Navigating to detail with alimento: $alimento")
        val intent = Intent(this, DetalleAlimentoGeneral::class.java)
        intent.putExtra("alimento", HashMap(alimento.toMap())) // Convert Alimento<String> to a HashMap
        startActivity(intent)
    }

    fun <T> Alimento<T>.toMap(): Map<String, Any> {
        return mapOf(
            "name" to name,
            "image" to image,
            "info_nutricion" to info_nutricion,
            "supermarkets" to supermarkets
        )
    }

    private fun showNoExtrasMessage() {
        tvNoExtrasMessage.visibility = View.VISIBLE
        recyclerViewExtras.visibility = View.GONE
        Log.d("ExtrasActivity", "Displayed no extras message.")
    }
}
