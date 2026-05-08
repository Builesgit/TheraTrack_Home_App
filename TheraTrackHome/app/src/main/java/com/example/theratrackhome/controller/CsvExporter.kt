package com.example.theratrackhome.controller

import android.content.Context
import android.os.Environment
import com.example.theratrackhome.model.Paciente
import com.example.theratrackhome.model.RegistroDiario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CsvExporter {

    suspend fun exportarRegistros(
        context: Context,
        paciente: Paciente,
        registros: List<RegistroDiario>
    ): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
                ?: throw Exception("No se pudo acceder a Documents")
            if (!dir.exists()) dir.mkdirs()

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val file = File(dir, "registros_${paciente.id.take(8)}_${timestamp}.csv")
            val contenido = buildString {
                appendLine("fecha,temperatura,pulso,nivel_fatiga,nivel_dolor,sintomas,notas")
                registros.forEach { r ->
                    appendLine(
                        listOf(
                            r.fecha,
                            r.temperatura?.toString().orEmpty(),
                            r.pulso?.toString().orEmpty(),
                            r.nivelFatiga?.toString().orEmpty(),
                            r.nivelDolor?.toString().orEmpty(),
                            r.sintomas.orEmpty(),
                            r.notas.orEmpty()
                        ).joinToString(",") { escape(it) }
                    )
                }
            }
            file.writeText(contenido, Charsets.UTF_8)
            file
        }
    }

    private fun escape(value: String): String {
        val mustQuote = value.contains(',') || value.contains('"') || value.contains('\n') || value.contains('\r')
        val escaped = value.replace("\"", "\"\"")
        return if (mustQuote) "\"$escaped\"" else escaped
    }
}
