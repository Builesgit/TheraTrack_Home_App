package com.example.theratrackhome.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.theratrackhome.R
import com.example.theratrackhome.activity.MainActivity
import com.example.theratrackhome.databinding.FragmentSintomasBinding

class SintomasFragment : Fragment() {

    private var _binding: FragmentSintomasBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSintomasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        inicializarChecks()

        binding.backButton.setOnClickListener {
            (activity as? MainActivity)?.supportFragmentManager
                ?.beginTransaction()
                ?.replace(R.id.fragmentContainer, HomeFragment())
                ?.commit()
        }

        binding.itemFatiga.setOnClickListener {
            val seleccionado = toggleCheck(binding.checkFatiga)
            if (seleccionado) desmarcarNinguno()
        }

        binding.itemNauseas.setOnClickListener {
            val seleccionado = toggleCheck(binding.checkNauseas)
            if (seleccionado) desmarcarNinguno()
        }

        binding.itemDolor.setOnClickListener {
            val seleccionado = toggleCheck(binding.checkDolor)
            if (seleccionado) desmarcarNinguno()
        }

        binding.itemIrritacion.setOnClickListener {
            val seleccionado = toggleCheck(binding.checkIrritacion)
            if (seleccionado) desmarcarNinguno()
        }

        binding.itemMareo.setOnClickListener {
            val seleccionado = toggleCheck(binding.checkMareo)
            if (seleccionado) desmarcarNinguno()
        }

        binding.itemNinguno.setOnClickListener {
            val seleccionado = toggleCheck(binding.checkNinguno)
            if (seleccionado) {
                desmarcar(binding.checkFatiga)
                desmarcar(binding.checkNauseas)
                desmarcar(binding.checkDolor)
                desmarcar(binding.checkIrritacion)
                desmarcar(binding.checkMareo)
            }
        }

        binding.btnGuardarRegistro.setOnClickListener {
            (activity as? MainActivity)?.supportFragmentManager
                ?.beginTransaction()
                ?.replace(R.id.fragmentContainer, ConfirmacionSintomasFragment())
                ?.commit()
        }
    }

    private fun inicializarChecks() {
        desmarcar(binding.checkFatiga)
        desmarcar(binding.checkNauseas)
        desmarcar(binding.checkDolor)
        desmarcar(binding.checkIrritacion)
        desmarcar(binding.checkMareo)
        desmarcar(binding.checkNinguno)
    }

    private fun toggleCheck(imageView: ImageView): Boolean {
        val isSelected = imageView.tag == true
        return if (isSelected) {
            imageView.setImageResource(R.drawable.ic_check_unselected)
            imageView.tag = false
            false
        } else {
            imageView.setImageResource(R.drawable.ic_check_selected)
            imageView.tag = true
            true
        }
    }

    private fun desmarcar(imageView: ImageView) {
        imageView.setImageResource(R.drawable.ic_check_unselected)
        imageView.tag = false
    }

    private fun desmarcarNinguno() {
        desmarcar(binding.checkNinguno)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}