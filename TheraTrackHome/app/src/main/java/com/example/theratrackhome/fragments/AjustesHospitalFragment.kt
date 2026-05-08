package com.example.theratrackhome.fragments

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.theratrackhome.LoginHospitalActivity
import com.example.theratrackhome.R
import com.example.theratrackhome.controller.PacienteController
import com.example.theratrackhome.network.SupabaseClient
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.switchmaterial.SwitchMaterial
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch

class AjustesHospitalFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.activity_ajustes_hospital, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<BottomNavigationView>(R.id.bottomNavHospital)?.visibility = View.GONE

        cargarPerfil(view)
        configurarListeners(view)
        cargarPreferencias(view)
    }

    private fun cargarPerfil(view: View) {
        val tvNombrePerfil = view.findViewById<TextView>(R.id.tvNombrePerfil)
        viewLifecycleOwner.lifecycleScope.launch {
            PacienteController.obtenerPerfilActual().onSuccess { perfil ->
                tvNombrePerfil.text = "${perfil.nombre} · ${perfil.rol}"
            }
        }
    }

    private fun configurarListeners(view: View) {
        view.findViewById<TextView>(R.id.itemCambiarPassword).setOnClickListener { mostrarCambioPassword() }
        view.findViewById<TextView>(R.id.item2FA).setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Autenticación 2FA")
                .setMessage("La autenticación de dos factores estará disponible en una próxima versión.")
                .setPositiveButton("Aceptar", null)
                .show()
        }
        view.findViewById<TextView>(R.id.itemCentroAyuda).setOnClickListener {
            abrirIntentSeguro(Intent(Intent.ACTION_VIEW, Uri.parse("https://theratrack.com/ayuda")), "No hay una app para abrir el enlace")
        }
        view.findViewById<TextView>(R.id.itemContactarSoporte).setOnClickListener {
            abrirIntentSeguro(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:soporte@theratrack.com")), "No hay una app de correo disponible")
        }
        view.findViewById<TextView>(R.id.itemCerrarSesion).setOnClickListener { cerrarSesion() }
        view.findViewById<MaterialButton>(R.id.btnGuardarConfiguracion).setOnClickListener { guardarConfiguracion(view) }
    }

    private fun cargarPreferencias(view: View) {
        val notif = requireActivity().getSharedPreferences("preferencias_notif_hospital", android.content.Context.MODE_PRIVATE)
        val umbrales = requireActivity().getSharedPreferences("umbrales_alerta", android.content.Context.MODE_PRIVATE)
        val swAlertas = view.findViewById<SwitchMaterial>(R.id.switchAlertasHospital)
        val swRecs = view.findViewById<SwitchMaterial>(R.id.switchRecsHospital)
        swAlertas.isChecked = notif.getBoolean("notif_alertas", true)
        swRecs.isChecked = notif.getBoolean("notif_recomendaciones", true)
        swAlertas.setOnCheckedChangeListener { _, checked -> notif.edit().putBoolean("notif_alertas", checked).apply() }
        swRecs.setOnCheckedChangeListener { _, checked -> notif.edit().putBoolean("notif_recomendaciones", checked).apply() }
        view.findViewById<EditText>(R.id.etUmbralAmbar).setText(umbrales.getString("umbral_ambar_msv_h", ""))
        view.findViewById<EditText>(R.id.etUmbralRojo).setText(umbrales.getString("umbral_rojo_msv_h", ""))
    }

    private fun mostrarCambioPassword() {
        val container = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 12, 48, 0)
        }
        val actual = passwordInput("Contraseña actual")
        val nueva = passwordInput("Nueva contraseña")
        val confirmar = passwordInput("Confirmar nueva contraseña")
        container.addView(actual)
        container.addView(nueva)
        container.addView(confirmar)

        AlertDialog.Builder(requireContext())
            .setTitle("Cambiar contraseña")
            .setView(container)
            .setPositiveButton("Guardar") { _, _ -> cambiarPassword(nueva.text.toString(), confirmar.text.toString()) }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun passwordInput(hint: String): EditText = EditText(requireContext()).apply {
        this.hint = hint
        inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
    }

    private fun cambiarPassword(nueva: String, confirmar: String) {
        if (nueva.length < 6) return Toast.makeText(requireContext(), "La nueva contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
        if (nueva != confirmar) return Toast.makeText(requireContext(), "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
        viewLifecycleOwner.lifecycleScope.launch {
            runCatching { SupabaseClient.client.auth.updateUser { password = nueva } }
                .onSuccess {
                    val anchor = requireView()
                    Snackbar.make(anchor, "Contraseña actualizada", Snackbar.LENGTH_LONG).show()
                }
                .onFailure { Toast.makeText(requireContext(), "Error al cambiar contraseña: ${it.message}", Toast.LENGTH_LONG).show() }
        }
    }

    private fun guardarConfiguracion(view: View) {
        requireActivity().getSharedPreferences("umbrales_alerta", android.content.Context.MODE_PRIVATE).edit()
            .putString("umbral_ambar_msv_h", view.findViewById<EditText>(R.id.etUmbralAmbar).text.toString())
            .putString("umbral_rojo_msv_h", view.findViewById<EditText>(R.id.etUmbralRojo).text.toString())
            .apply()
        Snackbar.make(requireView(), "Configuración guardada", Snackbar.LENGTH_LONG).show()
    }

    private fun cerrarSesion() {
        viewLifecycleOwner.lifecycleScope.launch {
            PacienteController.cerrarSesion()
            val intent = Intent(requireContext(), LoginHospitalActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    private fun abrirIntentSeguro(intent: Intent, mensajeError: String) {
        try {
            startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(requireContext(), mensajeError, Toast.LENGTH_SHORT).show()
        }
    }
}
