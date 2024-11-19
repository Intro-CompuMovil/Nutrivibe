package com.example.proyecto.centrales

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.proyecto.AlimentosFragment
import com.example.proyecto.MisAlimentosFragment
import com.example.proyecto.PlatosFragment

class DetalleAlimentoPagerAdapter(
    fragmentActivity: FragmentActivity
) : FragmentStateAdapter(fragmentActivity) {

    // Lista de fragmentos que se mostrarán en las pestañas
    private val fragments = listOf(
        AlimentosFragment(), // Fragmento para Alimentos
        PlatosFragment(),    // Fragmento para Platos
        MisAlimentosFragment() // Fragmento para Mis Alimentos
    )

    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment = fragments[position]
}
