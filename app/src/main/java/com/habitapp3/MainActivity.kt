package com.habitapp3

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.habitapp3.fragments.Cama
import com.habitapp3.fragments.Ejercicio
import com.habitapp3.fragments.Inicio
import com.habitapp3.fragments.Opciones
import com.habitapp3.fragments.Relax
import com.ismaeldivita.chipnavigation.ChipNavigationBar

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNav: ChipNavigationBar = findViewById(R.id.bottom_nav)

        // Carga el fragmento inicial SIN etiquetas de parámetro
        if (savedInstanceState == null) {
            loadFragment(Inicio())
            bottomNav.setItemSelected(R.id.inicio, true)
        }

        // Listener SIN etiquetas de parámetro
        bottomNav.setOnItemSelectedListener { id ->
            when (id) {
                R.id.inicio -> loadFragment(Inicio())
                R.id.cama -> loadFragment(Cama())
                R.id.ejercicio -> loadFragment(Ejercicio())
                R.id.relax -> loadFragment(Relax())
                R.id.opciones -> loadFragment(Opciones())
            }
        }
    }

    // Función de ayuda SIN etiquetas de parámetro en su uso
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}