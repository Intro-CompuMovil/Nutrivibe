package com.example.proyecto

import FoodRecognitionRepository
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.proyecto.foodRecognition.NutritionRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class DetallesCamara : AppCompatActivity() {

    private val storageReference = FirebaseStorage.getInstance().reference
    private val databaseReference = FirebaseDatabase.getInstance().getReference("misAlimentos")
    private val foodRepository = FoodRecognitionRepository(RetrofitClient.apiService)
    private val nutritionRepository = NutritionRepository(NutritionRetrofitClient.apiService)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalles_camara)

        val productTitle = findViewById<TextView>(R.id.productTitle)
        val proteinTextView = findViewById<TextView>(R.id.proteinTextView)
        val caloriesTextView = findViewById<TextView>(R.id.caloriesTextView)
        val fatTextView = findViewById<TextView>(R.id.fatTextView)
        val carbsTextView = findViewById<TextView>(R.id.carbsTextView)
        val imageView = findViewById<ImageView>(R.id.productImage)
        val detailsProduct = findViewById<TextView>(R.id.detailsProduct)
        val spinnerLoading = findViewById<ProgressBar>(R.id.spinnerLoading)
        val saveButton = findViewById<Button>(R.id.button3)

        // Recuperar el URI de la imagen desde CamaraFragment
        val imageUri = intent.getStringExtra("imageUri")
        Log.d("DetallesCamara", "Image URI recibido: $imageUri")

        if (!imageUri.isNullOrEmpty()) {
            try {
                Glide.with(this)
                    .load(Uri.parse(imageUri))
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_dialog_alert)
                    .into(imageView)
            } catch (e: Exception) {
                Log.e("DetallesCamara", "Error al cargar la imagen con Glide", e)
                Toast.makeText(this, "No se pudo cargar la imagen", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "No se pudo cargar la imagen, URI nulo o vacío", Toast.LENGTH_SHORT).show()
        }

        // Procesar la predicción y la información nutricional
        MainScope().launch {
            try {
                val fileBytes = imageUri?.let { uriString ->
                    val uri = Uri.parse(uriString)
                    contentResolver.openInputStream(uri)?.use { inputStream ->
                        inputStream.readBytes()
                    }
                }

                if (fileBytes != null) {
                    val base64Image = android.util.Base64.encodeToString(fileBytes, android.util.Base64.DEFAULT)
                    val concepts = foodRepository.predictFoodFromBase64(base64Image)

                    if (!concepts.isNullOrEmpty()) {
                        val topConcept = concepts.maxByOrNull { it.value }
                        topConcept?.let {
                            val foodName = it.name
                            val nutritionalInfo = nutritionRepository.getNutritionalInfo(foodName)

                            spinnerLoading.visibility = View.GONE

                            if (nutritionalInfo != null) {
                                productTitle.text = "Tu producto es: $foodName"

                                val protein = nutritionalInfo.foodNutrients.find { it.nutrientName.contains("Protein", true) }?.value
                                val calories = nutritionalInfo.foodNutrients.find { it.nutrientName.contains("Energy", true) }?.value
                                val fat = nutritionalInfo.foodNutrients.find { it.nutrientName.contains("Total lipid (fat)", true) }?.value
                                val carbs = nutritionalInfo.foodNutrients.find { it.nutrientName.contains("Carbohydrate", true) }?.value

                                proteinTextView.text = "${protein ?: "N/A"} g"
                                caloriesTextView.text = "${calories ?: "N/A"} kcal"
                                fatTextView.text = "${fat ?: "N/A"} g"
                                carbsTextView.text = "${carbs ?: "N/A"} g"

                                saveButton.setOnClickListener {
                                    uploadImageToFirebase(Uri.parse(imageUri)) { uploadedImageUrl ->
                                        if (uploadedImageUrl != null) {
                                            saveDataToDatabase(
                                                foodName,
                                                proteinTextView.text.toString(),
                                                caloriesTextView.text.toString(),
                                                fatTextView.text.toString(),
                                                carbsTextView.text.toString(),
                                                uploadedImageUrl
                                            )
                                        } else {
                                            Toast.makeText(this@DetallesCamara, "Error al guardar los datos.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("DetallesCamara", "Error durante el procesamiento de la imagen", e)
            }
        }
    }

    private fun uploadImageToFirebase(imageUri: Uri, onComplete: (String?) -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "Usuario no autenticado.", Toast.LENGTH_SHORT).show()
            onComplete(null)
            return
        }

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "images/${userId}_${timestamp}.jpg"
        val fileRef = storageReference.child(fileName)

        fileRef.putFile(imageUri)
            .addOnSuccessListener {
                fileRef.downloadUrl.addOnSuccessListener { uri ->
                    onComplete(uri.toString())
                }.addOnFailureListener {
                    onComplete(null)
                }
            }
            .addOnFailureListener {
                onComplete(null)
            }
    }

    private fun saveDataToDatabase(
        foodName: String,
        protein: String,
        calories: String,
        fat: String,
        carbs: String,
        imageUrl: String
    ) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val alimentoData = mapOf(
            "name" to foodName,
            "image" to imageUrl,
            "info_nutricion" to mapOf(
                "proteinas" to protein.replace(" g", ""),
                "calorias" to calories.replace(" kcal", ""),
                "grasas" to fat.replace(" g", ""),
                "carbohidratos" to carbs.replace(" g", "")
            )
        )

        databaseReference.child(userId).child(today).push().setValue(alimentoData)
            .addOnSuccessListener {
                Toast.makeText(this, "Alimento guardado correctamente.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.e("DetallesCamara", "Error al guardar en la base de datos", e)
            }
    }
}
