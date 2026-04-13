package com.example.theratrackhome.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.theratrackhome.R
import com.example.theratrackhome.activity.MainActivity
import com.example.theratrackhome.databinding.FragmentHomeBinding
import com.example.theratrackhome.fragment.HistorialFragment
import com.example.theratrackhome.fragment.SintomasFragment

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // CAMBIA AQUÍ EL ESTADO PARA PROBAR
        setEstado("seguro")
        // setEstado("advertencia")
        // setEstado("peligro")

        binding.btnVerHistorial.setOnClickListener {
            (activity as? MainActivity)?.supportFragmentManager
                ?.beginTransaction()
                ?.replace(R.id.fragmentContainer, HistorialFragment())
                ?.commit()
        }

        binding.btnSintomas.setOnClickListener {
            (activity as? MainActivity)?.supportFragmentManager
                ?.beginTransaction()
                ?.replace(R.id.fragmentContainer, SintomasFragment())
                ?.commit()
        }
    }

    private fun setEstado(estado: String) {
        when (estado) {

            "seguro" -> {
                binding.tvEstado.text = "Estado: Seguro"
                binding.tvNivel.text = "Nivel bajo de radiación detectado"

                binding.tvEstado.setTextColor(Color.parseColor("#111827"))
                binding.tvNivel.setTextColor(Color.parseColor("#4ADE80"))

                binding.cardEstado.setBackgroundResource(R.drawable.bg_card_home)
            }

            "advertencia" -> {
                binding.tvEstado.text = "Estado: Advertencia"
                binding.tvNivel.text = "Nivel moderado detectado"

                binding.tvNivel.setTextColor(Color.parseColor("#F59E0B"))

                binding.cardEstado.setBackgroundResource(R.drawable.bg_card_warning)
            }

            "peligro" -> {
                binding.tvEstado.text = "Estado: Peligro"
                binding.tvNivel.text = "Nivel alto de radiación detectado"

                binding.tvNivel.setTextColor(Color.parseColor("#EF4444"))

                binding.cardEstado.setBackgroundResource(R.drawable.bg_card_danger)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}