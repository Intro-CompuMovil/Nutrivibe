package com.example.proyecto

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class AlimentosFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DetalleAlimentoAdapter<String>
    private lateinit var databaseReference: DatabaseReference
    private val items = mutableListOf<Alimento<String>>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_alimentos, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.recyclerViewAlimentos)
        recyclerView.layoutManager = GridLayoutManager(context, 2)

        // Initialize Firebase reference
        databaseReference = FirebaseDatabase.getInstance().getReference("foods")

        // Load data from Firebase
        loadFoodsFromFirebase()
    }

    private fun loadFoodsFromFirebase() {
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                items.clear()

                val genericTypeIndicator = object : GenericTypeIndicator<Map<String, Any>>() {}
                for (foodSnapshot in snapshot.children) {
                    val alimentoMap = foodSnapshot.getValue(genericTypeIndicator)
                    if (alimentoMap != null) {
                        try {
                            val name = alimentoMap["name"] as? String ?: ""
                            val image = alimentoMap["image"] as? String ?: ""
                            val infoNutricionMap = alimentoMap["info_nutricion"] as? Map<String, Any> ?: emptyMap()
                            val supermarketsList = alimentoMap["supermarkets"] as? List<Map<String, Any>> ?: emptyList()

                            val infoNutricion = infoNutricionMap.mapValues { it.value.toString() }

                            // Map supermarkets
                            val supermarkets = supermarketsList.mapNotNull {
                                val supermarketName = it["name"] as? String ?: return@mapNotNull null
                                val lat = it["lat"] as? Double ?: return@mapNotNull null
                                val lon = it["lon"] as? Double ?: return@mapNotNull null
                                Supermarket(name = supermarketName, lat = lat, lon = lon)
                            }

                            val alimento = Alimento(
                                name = name,
                                image = image,
                                info_nutricion = infoNutricion,
                                supermarkets = supermarkets
                            )
                            Log.d("AlimentosFragment", "Loaded alimento: $alimento")
                            items.add(alimento)
                        } catch (e: Exception) {
                            Log.e("AlimentosFragment", "Error processing alimento: ${e.message}")
                        }
                    }
                }

                adapter = DetalleAlimentoAdapter(items) { alimento: Alimento<String> ->
                    navigateToDetalleAlimento(alimento)
                }
                recyclerView.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("AlimentosFragment", "Error loading foods: ${error.message}")
            }
        })
    }


    private fun navigateToDetalleAlimento(alimento: Alimento<String>) {
        // Convert Alimento<String> to HashMap for intent transfer
        val alimentoMap = hashMapOf(
            "name" to alimento.name,
            "image" to alimento.image,
            "info_nutricion" to alimento.info_nutricion,
            "supermarkets" to alimento.supermarkets.map { supermarket ->
                hashMapOf(
                    "name" to supermarket.name,
                    "lat" to supermarket.lat,
                    "lon" to supermarket.lon
                )
            }
        )
        Log.d("AlimentosFragment", "Navigating with alimentoMap: $alimentoMap")

        val intent = Intent(requireContext(), DetalleAlimentoGeneral::class.java)
        intent.putExtra("alimento", alimentoMap)
        startActivity(intent)
    }

}
