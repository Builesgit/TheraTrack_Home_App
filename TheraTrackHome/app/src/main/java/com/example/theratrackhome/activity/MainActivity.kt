package com.example.theratrackhome.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.theratrackhome.R
import com.example.theratrackhome.databinding.ActivityMainBinding
import com.example.theratrackhome.fragment.HistorialFragment
import com.example.theratrackhome.fragment.HomeFragment
import com.example.theratrackhome.fragment.PerfilFragment
import com.example.theratrackhome.fragment.SintomasFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            replaceFragment(HomeFragment())
        }

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> replaceFragment(HomeFragment())
                R.id.nav_historial -> replaceFragment(HistorialFragment())
                R.id.nav_sintomas -> replaceFragment(SintomasFragment())
                R.id.nav_perfil -> replaceFragment(PerfilFragment())
            }
            true
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}