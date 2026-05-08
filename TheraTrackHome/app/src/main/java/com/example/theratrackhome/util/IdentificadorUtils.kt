package com.example.theratrackhome.util

object IdentificadorUtils {

    fun cipaToEmail(cipa: String): String =
        "${cipa.trim()}@paciente.theratrack.internal"

    fun colegiadoToEmail(numeroColegiado: String): String =
        "${numeroColegiado.trim()}@medico.theratrack.internal"

    fun validarCipa(cipa: String): Boolean =
        cipa.trim().length == 10 && cipa.trim().all { it.isDigit() }

    fun validarColegiado(numero: String): Boolean =
        numero.trim().length in 6..10 && numero.trim().all { it.isDigit() }

    fun generarPassword(identificador: String, pin: String): String =
        "${identificador.trim()}#${pin.trim()}"

    fun detectarTipoInput(input: String): String {
        val trimmed = input.trim()
        return when {
            trimmed.contains("@") -> "email"
            trimmed.length == 10 && trimmed.all { it.isDigit() } -> "cipa"
            trimmed.length in 6..10 && trimmed.all { it.isDigit() } -> "colegiado"
            else -> "desconocido"
        }
    }

    fun normalizarAEmail(input: String, esPaciente: Boolean): String {
        val tipo = detectarTipoInput(input)
        return when (tipo) {
            "email"     -> input.trim()
            "cipa"      -> cipaToEmail(input)
            "colegiado" -> colegiadoToEmail(input)
            else        -> input.trim()
        }
    }

    fun extraerIdentificadorOriginal(input: String): String {
        return if (input.contains("@")) input.substringBefore("@").trim() else input.trim()
    }
}
