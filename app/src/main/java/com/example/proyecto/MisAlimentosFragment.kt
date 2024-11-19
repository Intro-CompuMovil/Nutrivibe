package com.example.proyecto

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MisAlimentosFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var alimentosAdapter: MisAlimentoAdapter
    private val alimentosList = mutableListOf<MisAlimento>()
    private val databaseReference = FirebaseDatabase.getInstance().getReference("misAlimentos")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_mis_alimentos, container, false)

        // Inicializar RecyclerView
        recyclerView = view.findViewById(R.id.recyclerViewAlimentos)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        alimentosAdapter = MisAlimentoAdapter(alimentosList) { alimento ->
            val intent = Intent(requireContext(), DetallesMisAlimentosGeneral::class.java)
            intent.putExtra("name", alimento.name)
            intent.putExtra("imageUrl", alimento.image)
            intent.putExtra("proteinas", alimento.info_nutricion["proteinas"])
            intent.putExtra("calorias", alimento.info_nutricion["calorias"])
            intent.putExtra("grasas", alimento.info_nutricion["grasas"])
            intent.putExtra("carbohidratos", alimento.info_nutricion["carbohidratos"])
            startActivity(intent)
        }

        recyclerView.adapter = alimentosAdapter

        loadAlimentos()

        return view
    }

    private fun loadAlimentos() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(requireContext(), "Usuario no autenticado.", Toast.LENGTH_SHORT).show()
            return
        }

        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        databaseReference.child(userId).child(today).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    alimentosList.clear()

                    for (child in snapshot.children) {
                        try {
                            val alimento = child.getValue(MisAlimento::class.java)
                            if (alimento != null) {
                                Log.d("MisAlimentosFragment", "Alimento cargado: ${alimento.name}")
                                alimentosList.add(alimento)
                            }
                        } catch (e: Exception) {
                            Log.e("MisAlimentosFragment", "Error al mapear datos: ${e.message}")
                        }
                    }

                    alimentosAdapter.notifyDataSetChanged()
                } else {
                    Log.d("MisAlimentosFragment", "No hay alimentos en misAlimentos.")
                    Toast.makeText(requireContext(), "No hay alimentos para hoy.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MisAlimentosFragment", "Error al cargar alimentos: ${error.message}")
            }
        })
    }
}