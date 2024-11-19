package com.example.proyecto

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

class ExtrasAdapter(
    private var extras: List<Alimento<String>>,
    private val onItemClick: (Alimento<String>) -> Unit
) : RecyclerView.Adapter<ExtrasAdapter.ExtrasViewHolder>() {

    inner class ExtrasViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val foodImage: ImageView = itemView.findViewById(R.id.foodImage)
        private val foodName: TextView = itemView.findViewById(R.id.foodName)

        fun bind(item: Alimento<String>) {
            // Set food name
            foodName.text = item.name

            // Determine the full image path (supports Firebase or local paths)
            val imagePath = if (item.image.startsWith("http") || item.image.startsWith("https")) {
                item.image // Full URL (e.g., Firebase Storage)
            } else {
                "file:///android_asset/${item.image}" // Local asset path
            }

            // Load image using Glide
            Glide.with(itemView.context)
                .load(imagePath)
                .placeholder(R.drawable.placeholder_image) // Default placeholder
                .error(R.drawable.error) // Error placeholder
                .diskCacheStrategy(DiskCacheStrategy.ALL) // Cache optimization
                .into(foodImage)

            // Set onClick listener for the item
            itemView.setOnClickListener {
                Log.d("ExtrasAdapter", "Clicked item: $item")
                onItemClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExtrasViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_food, parent, false)
        return ExtrasViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExtrasViewHolder, position: Int) {
        val item = extras[position]
        Log.d("ExtrasAdapter", "Binding item at position $position: $item")
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        Log.d("ExtrasAdapter", "getItemCount called. Size: ${extras.size}")
        return extras.size
    }

    // Method to update the adapter data
    fun updateData(newExtras: List<Alimento<String>>) {
        extras = newExtras
        Log.d("ExtrasAdapter", "Data updated. New size: ${extras.size}")
        notifyDataSetChanged()
    }
}
