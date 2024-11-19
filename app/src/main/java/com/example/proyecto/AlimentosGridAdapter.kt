package com.example.proyecto

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import com.bumptech.glide.Glide

class AlimentosGridAdapter(
    private val context: Context,
    private val alimentosList: List<Map<String, String>>
) : BaseAdapter() {

    override fun getCount(): Int {
        return alimentosList.size
    }

    override fun getItem(position: Int): Any {
        return alimentosList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = convertView
            ?: LayoutInflater.from(context).inflate(R.layout.activity_alimentos_adapter_grid, parent, false)

        val imageView = view.findViewById<ImageView>(R.id.imageViewAlimentoGrid)
        val foto = alimentosList[position]["foto"]
        val assetPath = "file:///android_asset/$foto"

        // Usar Glide para cargar la imagen desde los assets
        Glide.with(context)
            .load(assetPath)
            .error(R.drawable.error) // Imagen de error si falla
            .into(imageView)

        return view
    }
}
