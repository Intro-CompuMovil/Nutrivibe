package com.example.proyecto.centrales

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.proyecto.R
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import androidx.viewpager2.widget.ViewPager2

class DetalleAlimentoFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_detalle_alimento, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            // Initialize TabLayout and ViewPager2
            val tabLayout = view.findViewById<TabLayout>(R.id.tabLayout)
            val viewPager = view.findViewById<ViewPager2>(R.id.viewPager)

            Log.d("DetalleAlimentoFragment", "Initializing TabLayout and ViewPager2...")

            // Set up ViewPager2 with DetalleAlimentoPagerAdapter
            val pagerAdapter = DetalleAlimentoPagerAdapter(requireActivity())
            viewPager.adapter = pagerAdapter

            Log.d("DetalleAlimentoFragment", "PagerAdapter set on ViewPager2.")

            // Link TabLayout and ViewPager2
            TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                when (position) {
                    0 -> tab.text = "Alimentos"
                    1 -> tab.text = "Platos"
                    2 -> tab.text = "Mis Alimentos"
                }
            }.attach()

            Log.d("DetalleAlimentoFragment", "TabLayout linked to ViewPager2.")
        } catch (e: Exception) {
            Log.e("DetalleAlimentoFragment", "Error initializing TabLayout and ViewPager2: ${e.message}")
            e.printStackTrace()
        }
    }
}
