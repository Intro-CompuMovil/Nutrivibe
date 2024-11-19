package com.example.proyecto

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class AlimentoAdapter<T>(
    private val alimentos: List<Alimento<T>>,
    private val onItemClick: (Alimento<T>) -> Unit
) : RecyclerView.Adapter<AlimentoViewHolder<T>>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlimentoViewHolder<T> {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_food, parent, false)
        return AlimentoViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlimentoViewHolder<T>, position: Int) {
        holder.bind(alimentos[position], onItemClick)
    }

    override fun getItemCount(): Int = alimentos.size
}
    class AlimentoViewHolder<T>(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvFoodName)
        private val ivImage: ImageView = itemView.findViewById(R.id.ivFoodImage)

        fun bind(alimento: Alimento<T>, onItemClick: (Alimento<T>) -> Unit) {
            tvName.text = alimento.name

            // Determina si la imagen es una URL o una ruta relativa
            val imagePath = if (alimento.image.startsWith("http")) {
                alimento.image // URL absoluta
            } else {
                "file:///android_asset/${alimento.image}" // Ruta relativa
            }

            // Cargar la imagen con Glide
            Glide.with(itemView.context)
                .load(imagePath)
                .placeholder(R.drawable.placeholder_image) // Imagen de carga por defecto
                .error(R.drawable.error) // Imagen en caso de error
                .into(ivImage)

            // Configurar la acción de clic
            itemView.setOnClickListener { onItemClick(alimento) }
        }
    }

