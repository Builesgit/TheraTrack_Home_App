package com.example.theratrackhome.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.theratrackhome.AlertasActivity
import com.example.theratrackhome.AsistenciaActivity
import com.example.theratrackhome.ConfiguracionNotificacionesActivity
import com.example.theratrackhome.PerfilActivity
import com.example.theratrackhome.R
import com.example.theratrackhome.controller.AuthController
import com.example.theratrackhome.controller.PacienteController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class PerfilPacienteFragment : Fragment() {

    private lateinit var tvNombre: TextView
    private lateinit var tvSubtitulo: TextView
    private lateinit var tvRadiofarmaco: TextView
    private lateinit var tvFechaTratamiento: TextView
    private lateinit var tvHospital: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.activity_perfil_paciente, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<BottomNavigationView>(R.id.bottomNavigation)?.visibility = View.GONE
        view.findViewById<ImageView>(R.id.btnBack)?.visibility = View.GONE

        tvNombre           = view.findViewById(R.id.tvNombre)
        tvSubtitulo        = view.findViewById(R.id.tvSubtitulo)
        tvRadiofarmaco     = view.findViewById(R.id.tvRadiofarmaco)
        tvFechaTratamiento = view.findViewById(R.id.tvFechaTratamiento)
        tvHospital         = view.findViewById(R.id.tvHospital)

        configurarBotones(view)
        cargarDatos()
    }

    private fun configurarBotones(view: View) {
        view.findViewById<MaterialButton>(R.id.btnVerRecomendaciones).setOnClickListener {
            startActivity(Intent(requireContext(), AlertasActivity::class.java))
        }
        view.findViewById<LinearLayout>(R.id.rowNotificaciones).setOnClickListener {
            startActivity(Intent(requireContext(), ConfiguracionNotificacionesActivity::class.java))
        }
        view.findViewById<LinearLayout>(R.id.rowIdioma).setOnClickListener {
            mostrarDialogoIdioma()
        }
        view.findViewById<LinearLayout>(R.id.rowContacto).setOnClickListener {
            startActivity(Intent(requireContext(), AsistenciaActivity::class.java))
        }
        view.findViewById<LinearLayout>(R.id.rowCerrarSesion).setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Cerrar sesión")
                .setMessage("¿Estás seguro de que quieres cerrar sesión?")
                .setPositiveButton("Cerrar sesión") { _, _ -> cerrarSesion() }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    private fun mostrarDialogoIdioma() {
        val idiomas = arrayOf("Español", "English")
        val tags = arrayOf("es", "en")
        AlertDialog.Builder(requireContext())
            .setTitle("Idioma")
            .setItems(idiomas) { _, which ->
                val locale = tags[which]
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(locale))
                requireActivity().getSharedPreferences("preferencias_app", android.content.Context.MODE_PRIVATE)
                    .edit().putString("idioma", locale).apply()
                Toast.makeText(requireContext(), "Idioma actualizado", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun cargarDatos() {
        viewLifecycleOwner.lifecycleScope.launch {
            PacienteController.obtenerPerfilActual()
                .onSuccess { perfil ->
                    tvNombre.text = perfil.nombre
                    tvSubtitulo.text = "Paciente • ${perfil.rol.replaceFirstChar { it.uppercase() }}"
                }

            PacienteController.obtenerPacienteActual()
                .onSuccess { paciente ->
                    tvRadiofarmaco.text = paciente.radiofarmaco

                    val sdfIn  = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val sdfOut = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val fechaFormateada = try {
                        sdfOut.format(sdfIn.parse(paciente.fechaTratamiento)!!)
                    } catch (e: Exception) { paciente.fechaTratamiento }
                    tvFechaTratamiento.text = fechaFormateada

                    PacienteController.obtenerNombreHospital(paciente.hospitalId)
                        .onSuccess { nombre -> tvHospital.text = nombre }
                        .onFailure   { tvHospital.text = "—" }
                }
                .onFailure {
                    tvRadiofarmaco.text     = "Sin datos"
                    tvFechaTratamiento.text = "—"
                    tvHospital.text         = "—"
                }
        }
    }

    private fun cerrarSesion() {
        viewLifecycleOwner.lifecycleScope.launch {
            AuthController.logout()
                .onSuccess {
                    val intent = Intent(requireContext(), PerfilActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                }
                .onFailure {
                    Toast.makeText(requireContext(), "Error al cerrar sesión", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
