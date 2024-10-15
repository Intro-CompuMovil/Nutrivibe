package com.example.proyecto

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.proyecto.centrales.CalendarioFragment
import com.example.proyecto.centrales.CamaraFragment
import com.example.proyecto.centrales.PerfilFragment
import com.example.proyecto.centrales.UbicacionFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private val firstFragment = CalendarioFragment()  // Corregido: Nombres de clases de fragmentos
    private val secondFragment = CamaraFragment()
    private val thirdFragment = UbicacionFragment()
    private val fourFragment = PerfilFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Set up bottom navigation
        val navigation: BottomNavigationView = findViewById(R.id.bottom_navigation)
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        // Set up gradient text (Opcionalmente añadir aquí la lógica para el gradient text si es necesario)
        loadFragment(firstFragment)
    }

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.calendarioFragment -> {  // IDs del menú de navegación deben coincidir
                loadFragment(firstFragment)
                return@OnNavigationItemSelectedListener true
            }
            R.id.camaraFragment -> {
                loadFragment(secondFragment)
                return@OnNavigationItemSelectedListener true
            }
            R.id.ubicacionFragment -> {
                loadFragment(thirdFragment)
                return@OnNavigationItemSelectedListener true
            }
            R.id.perfilFragment -> {
                loadFragment(fourFragment)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    private fun loadFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frame_container, fragment)
        transaction.commit()
    }
}
