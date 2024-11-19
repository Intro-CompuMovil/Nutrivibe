package com.example.proyecto

import android.os.Bundle
import android.widget.GridView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.json.JSONObject

class AgregarAlimentos : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_agregar_alimentos)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val gridView: GridView = findViewById(R.id.gridview)
        val alimentos = leerAlimentosDesdeJSON() // Función que ya tienes para leer el JSON

        // Usar el nuevo adaptador para cargar las imágenes
        gridView.adapter = AlimentosGridAdapter(this, alimentos)
    }

    private fun leerAlimentosDesdeJSON(): List<Map<String, String>> {
        val alimentos = mutableListOf<Map<String, String>>()
        try {
            val inputStream = assets.open("alimentos.json")
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
