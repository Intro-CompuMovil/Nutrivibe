package com.example.proyecto

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class MisAlimentoAdapter(
    private val alimentos: List<MisAlimento>,
    private val onItemClick: (MisAlimento) -> Unit
) : RecyclerView.Adapter<MisAlimentoAdapter.MisAlimentoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MisAlimentoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mis_alimentos, parent, false)
        return MisAlimentoViewHolder(view)
    }

    override fun onBindViewHolder(holder: MisAlimentoViewHolder, position: Int) {
        val alimento = alimentos[position]
        holder.bind(alimento, onItemClick)
    }

    override fun getItemCount(): Int = alimentos.size

    class MisAlimentoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.ivMisAlimentoImage)
        private val textView: TextView = itemView.findViewById(R.id.tvMisAlimentoName)

        fun bind(alimento: MisAlimento, onItemClick: (MisAlimento) -> Unit) {
            textView.text = alimento.name

            Glide.with(itemView.context)
                .load(alimento.image)
                .placeholder(R.drawable.placeholder_image)
                .into(imageView)

            itemView.setOnClickListener { onItemClick(alimento) }
        }
    }
}
