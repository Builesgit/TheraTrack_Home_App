package com.example.theratrackhome.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.theratrackhome.activity.LoginActivity
import com.example.theratrackhome.databinding.FragmentPerfilBinding

class PerfilFragment : Fragment() {

    private var _binding: FragmentPerfilBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPerfilBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnEditarPerfil.setOnClickListener {
            Toast.makeText(requireContext(), "Editar perfil", Toast.LENGTH_SHORT).show()
        }

        binding.btnVerRecomendaciones.setOnClickListener {
            Toast.makeText(requireContext(), "Abrir recomendaciones", Toast.LENGTH_SHORT).show()
        }

        binding.itemNotificaciones.setOnClickListener {
            Toast.makeText(requireContext(), "Notificaciones", Toast.LENGTH_SHORT).show()
        }

        binding.itemIdioma.setOnClickListener {
            Toast.makeText(requireContext(), "Idioma", Toast.LENGTH_SHORT).show()
        }

        binding.itemContactoHospital.setOnClickListener {
            val telefonoHospital = "912345678"

            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = android.net.Uri.parse("tel:$telefonoHospital")

            startActivity(intent)
        }

        binding.itemCerrarSesion.setOnClickListener {
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}