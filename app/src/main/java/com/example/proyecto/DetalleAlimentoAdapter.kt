package com.example.proyecto

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class DetalleAlimentoAdapter<T>(
    private val alimentosList: List<Alimento<T>>,
    private val onItemClick: (Alimento<T>) -> Unit
) : RecyclerView.Adapter<DetalleAlimentoAdapter<T>.AlimentoViewHolder>() {

    inner class AlimentoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.foodImage)
        private val textView: TextView = itemView.findViewById(R.id.foodName)

        fun bind(alimento: Alimento<T>) {
            // Set food name
            textView.text = alimento.name

            // Set image (adjust if using URLs or local assets)
            val imagePath = if (alimento.image.startsWith("http")) {
                alimento.image
            } else {
                "file:///android_asset/${alimento.image}"
            }

            Glide.with(itemView.context)
                .load(imagePath)
                .placeholder(R.drawable.placeholder_image) // Placeholder while loading
                .error(R.drawable.error) // Error image if load fails
                .into(imageView)

            // Set click listener
            itemView.setOnClickListener { onItemClick(alimento) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlimentoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_food, parent, false) // Ensure this is the correct layout
        return AlimentoViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlimentoViewHolder, position: Int) {
        holder.bind(alimentosList[position])
    }

    override fun getItemCount(): Int = alimentosList.size
}
