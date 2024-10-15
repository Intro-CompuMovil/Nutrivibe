package com.example.proyecto

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.proyecto.R

class AlimentosAdapter(private val alimentosList: List<Map<String, String>>) : RecyclerView.Adapter<AlimentosAdapter.AlimentoViewHolder>() {

    class AlimentoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageButton = itemView.findViewById(R.id.imageViewAlimento)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlimentoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.activity_recycle_view_alimentos_adapter, parent, false)
        return AlimentoViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlimentoViewHolder, position: Int) {
        val foto = alimentosList[position]["foto"]
        val assetPath = "file:///android_asset/$foto" // Agrega el prefijo de assets

        // Cargar la imagen desde assets, y si hay error, cargar imagen de error
        Glide.with(holder.itemView.context)
            .load(assetPath)
            .error(R.drawable.error)  // Imagen de error en caso de fallo
            .into(holder.imageView)
    }


    override fun getItemCount(): Int {
        return alimentosList.size
    }
}