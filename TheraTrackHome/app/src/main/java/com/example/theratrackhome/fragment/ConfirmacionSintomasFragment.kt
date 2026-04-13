package com.example.theratrackhome.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.theratrackhome.R
import com.example.theratrackhome.activity.MainActivity
import com.example.theratrackhome.databinding.FragmentConfirmacionSintomasBinding

class ConfirmacionSintomasFragment : Fragment() {

    private var _binding: FragmentConfirmacionSintomasBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConfirmacionSintomasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backButton.setOnClickListener {
            (activity as? MainActivity)?.supportFragmentManager
                ?.beginTransaction()
                ?.replace(R.id.fragmentContainer, SintomasFragment())
                ?.commit()
        }

        binding.btnVolverInicio.setOnClickListener {
            (activity as? MainActivity)?.supportFragmentManager
                ?.beginTransaction()
                ?.replace(R.id.fragmentContainer, HomeFragment())
                ?.commit()
        }

        binding.tvResumenRegistro.setOnClickListener {
            (activity as? MainActivity)?.supportFragmentManager
                ?.beginTransaction()
                ?.replace(R.id.fragmentContainer, HistorialFragment())
                ?.commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}