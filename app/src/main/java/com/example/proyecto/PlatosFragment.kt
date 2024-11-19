package com.example.proyecto

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.*
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.proyecto.AlimentosAdapter.AlimentoViewHolder

class PlatosFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var spinner: Spinner
    private lateinit var platosAdapter: PlatosAdapter
    private lateinit var databaseReference: DatabaseReference
    private val items = mutableListOf<Plato>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_platos, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize RecyclerView and Spinner
        recyclerView = view.findViewById(R.id.recyclerViewPlatos)
        spinner = view.findViewById(R.id.spinnerFilter)

        // Setup RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)
        platosAdapter = PlatosAdapter(items) { plato ->
            navigateToDetallePlato(plato)
        }
        recyclerView.adapter = platosAdapter

        // Initialize Firebase Database
        databaseReference = FirebaseDatabase.getInstance().getReference("platos")

        // Setup Spinner
        setupSpinner()
    }
    private fun setupSpinner() {
        val categories = arrayOf("Snacks", "Desayunos", "Almuerzos", "Cenas")
        val spinnerAdapter = ArrayAdapter(requireContext(), R.layout.custom_spinner, categories)
        spinnerAdapter.setDropDownViewResource(R.layout.custom_spinner_dropdown)
        spinner.adapter = spinnerAdapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val category = parent.getItemAtPosition(position).toString()
                loadCategory(category)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun loadCategory(category: String) {
        Log.d("PlatosFragment", "Loading category '$category' from Firebase...")

        // Navigate to the correct node in the database
        databaseReference.child("alimentos").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                items.clear()
                Log.d("PlatosFragment", "Snapshot exists: ${snapshot.exists()}")

                // Iterate over numeric indices (0, 1, 2, ...)
                for (indexSnapshot in snapshot.children) {
                    val categorySnapshot = indexSnapshot.child(category.lowercase())
                    if (categorySnapshot.exists()) {
                        Log.d("PlatosFragment", "Category '$category' found under index: ${indexSnapshot.key}")
                        for (platoSnapshot in categorySnapshot.children) {
                            val plato = platoSnapshot.getValue(Plato::class.java)
                            if (plato != null) {
                                items.add(plato)
                                Log.d("PlatosFragment", "Loaded dish: ${plato.nombre}")
                            } else {
                                Log.e("PlatosFragment", "Error: null dish in category '$category'.")
                            }
                        }
                    } else {
                        Log.d("PlatosFragment", "Category '$category' not found under index: ${indexSnapshot.key}")
                    }
                }

                if (items.isEmpty()) {
                    Log.w("PlatosFragment", "No dishes found for category '$category'.")
                }

                // Update RecyclerView Adapter
                platosAdapter = PlatosAdapter(items) { plato ->
                    navigateToDetallePlato(plato)
                }
                recyclerView.adapter = platosAdapter
                platosAdapter.notifyDataSetChanged()

                Log.d("PlatosFragment", "RecyclerView updated with ${items.size} elements.")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("PlatosFragment", "Error loading category '$category': ${error.message}")
            }
        })
    }

    private fun navigateToDetallePlato(plato: Plato) {
        val intent = Intent(requireContext(), DetallePlatoGeneral::class.java)
        intent.putExtra("plato", plato) // Ensure `Plato` implements `Parcelable`
        startActivity(intent)
    }

}
class PlatosAdapter(
    private val items: List<Plato>,
    private val onItemClick: (Plato) -> Unit
) : RecyclerView.Adapter<PlatosAdapter.PlatoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlatoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_plato_list, parent, false)
        return PlatoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlatoViewHolder, position: Int) {
        holder.bind(items[position], onItemClick)
    }

    override fun getItemCount() = items.size

    class PlatoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNombre: TextView = itemView.findViewById(R.id.textViewNombrePlato)
        private val tvCalorias: TextView = itemView.findViewById(R.id.textViewCalorias)
        private val imageView: ImageView = itemView.findViewById(R.id.imageViewPlato)
        private val btnVerInformacion: Button = itemView.findViewById(R.id.buttonverInformacion)

        fun bind(plato: Plato, onItemClick: (Plato) -> Unit) {
            tvNombre.text = plato.nombre
            tvCalorias.text = "Calorías: ${plato.informacion_nutricional["calorias"] ?: "0"}"

            val assetPath = "file:///android_asset/${plato.imagen}"

            Glide.with(itemView.context)
                .load(assetPath)
                .error(R.drawable.placeholder_image)
                .into(imageView)

            // Configurar el click del botón Ver Información
            btnVerInformacion.setOnClickListener {
                val intent = Intent(itemView.context, DetallePlatoGeneral::class.java).apply {
                    putExtra("plato", plato)
                }
                itemView.context.startActivity(intent)
            }

            // Mantener el click del item completo
            itemView.setOnClickListener { onItemClick(plato) }
        }
    }
}

