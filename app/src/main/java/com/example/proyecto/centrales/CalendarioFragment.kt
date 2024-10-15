package com.example.proyecto.centrales

import android.content.Intent
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.style.CharacterStyle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.proyecto.AgregarAlimentos
import com.example.proyecto.AlimentosAdapter
import com.example.proyecto.MainActivity
import com.example.proyecto.R
import org.json.JSONObject
import java.io.InputStream

class CalendarioFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_calendario, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val alimentos = leerAlimentosDesdeJSON()

        // RecyclerView para Desayuno
        val recyclerViewDesayuno = view.findViewById<RecyclerView>(R.id.recyclerViewDesayuno)
        recyclerViewDesayuno.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recyclerViewDesayuno.adapter = AlimentosAdapter(alimentos)

        // RecyclerView para Almuerzo
        val recyclerViewAlmuerzo =
            view.findViewById<RecyclerView>(R.id.recyclerViewAlimentosAlmuerzo)
        recyclerViewAlmuerzo.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recyclerViewAlmuerzo.adapter = AlimentosAdapter(alimentos)

        // RecyclerView para Cena
        val recyclerViewCena = view.findViewById<RecyclerView>(R.id.recyclerViewCena)
        recyclerViewCena.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recyclerViewCena.adapter = AlimentosAdapter(alimentos)

        val botonAgregarDesayuno = view.findViewById<ImageButton>(R.id.aniadirdesayuno)
        val botonAgregarAlmuerzo = view.findViewById<ImageButton>(R.id.aniadiralmuerzo)
        val botonAgregarCena = view.findViewById<ImageButton>(R.id.aniadircena)

        val intent = Intent(requireContext(), AgregarAlimentos::class.java)


        botonAgregarDesayuno.setOnClickListener {
            startActivity(intent)
        }
        botonAgregarAlmuerzo.setOnClickListener {
            startActivity(intent)
        }
        botonAgregarCena.setOnClickListener {
            startActivity(intent)
        }
    }

    private fun leerAlimentosDesdeJSON(): List<Map<String, String>> {
        val alimentos = mutableListOf<Map<String, String>>()
        try {
            val inputStream: InputStream = requireContext().assets.open("alimentos.json")
            val json = inputStream.bufferedReader().use { it.readText() }

            val jsonObject = JSONObject(json)
            val jsonArray = jsonObject.getJSONArray("alimentos")

            for (i in 0 until jsonArray.length()) {
                val jsonObjectAlimento = jsonArray.getJSONObject(i)
                val foto = jsonObjectAlimento.getString("foto")
                alimentos.add(mapOf("foto" to foto))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return alimentos
    }


}