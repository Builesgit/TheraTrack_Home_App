package com.example.theratrackhome.controller

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Environment
import com.example.theratrackhome.model.Alerta
import com.example.theratrackhome.model.Paciente
import com.example.theratrackhome.model.Perfil
import com.example.theratrackhome.model.Recomendacion
import com.example.theratrackhome.model.RegistroDiario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

object PdfReportGenerator {

    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 40f
    private const val PRIMARY = 0xFF1A56DB.toInt()

    suspend fun generarInformeClinico(
        context: Context,
        paciente: Paciente,
        perfilPaciente: Perfil,
        registros: List<RegistroDiario>,
        alertas: List<Alerta>,
        recomendaciones: List<Recomendacion>,
        medicoResponsable: String,
        rangoDesde: String,
        rangoHasta: String
    ): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
                ?: throw Exception("No se pudo acceder a Documents")
            if (!dir.exists()) dir.mkdirs()

            val pdf = PdfDocument()
            try {
                val renderer = Renderer(pdf)
                val iniciales = perfilPaciente.nombre.split(" ")
                    .filter { it.isNotBlank() }
                    .take(2)
                    .joinToString("") { it.first().uppercase() }
                    .ifBlank { paciente.id.take(2).uppercase() }
                val year = SimpleDateFormat("yyyy", Locale.US).format(Date())
                val ref = "TT-$year-$iniciales-${System.currentTimeMillis().toString().takeLast(5)}"
                val fechaEmision = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

                renderer.header("INFORME CLÍNICO - THERATRACK HOME", "Ref: $ref")
                renderer.section("DATOS DEL PACIENTE")
                renderer.grid(
                    listOf(
                        "Paciente" to perfilPaciente.nombre,
                        "Fecha Emisión" to fechaEmision,
                        "Edad-Sexo" to "No especificado",
                        "Médico Resp." to medicoResponsable
                    )
                )
                renderer.text("Rango analizado: $rangoDesde a $rangoHasta")
                renderer.text("Tratamiento: ${paciente.radiofarmaco} · Dosis: ${paciente.dosisMbq?.toString() ?: "No especificada"} MBq")

                renderer.section("EVOLUCIÓN DE SÍNTOMAS")
                if (registros.isEmpty()) {
                    renderer.text("Sin datos en el rango seleccionado.")
                } else {
                    renderer.barChart(registros)
                }

                renderer.section("RESUMEN DE SÍNTOMAS")
                val sintomas = contarSintomas(registros)
                if (sintomas.isEmpty()) renderer.text("Sin síntomas reportados.")
                sintomas.forEach { (sintoma, total) -> renderer.text("• $sintoma: $total") }

                renderer.section("ALERTAS CLÍNICAS")
                if (alertas.isEmpty()) renderer.text("Sin alertas clínicas en el rango.")
                else renderer.alertTable(alertas)

                renderer.section("RECOMENDACIONES EMITIDAS")
                if (recomendaciones.isEmpty()) renderer.text("Sin recomendaciones emitidas.")
                recomendaciones.take(20).forEach { rec ->
                    renderer.text("• ${rec.titulo} (${rec.createdAt?.take(10) ?: "sin fecha"})")
                }

                renderer.finish()

                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
                val file = File(dir, "informe_${paciente.id.take(8)}_${timestamp}.pdf")
                file.outputStream().use { pdf.writeTo(it) }
                file
            } finally {
                pdf.close()
            }
        }
    }

    private fun contarSintomas(registros: List<RegistroDiario>): Map<String, Int> {
        val conteo = linkedMapOf<String, Int>()
        registros.flatMap { it.sintomas.orEmpty().split(",", ";") }
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .forEach { conteo[it] = (conteo[it] ?: 0) + 1 }
        return conteo.toList().sortedByDescending { it.second }.toMap()
    }

    private class Renderer(private val pdf: PdfDocument) {
        private val normal = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(31, 41, 55); textSize = 10f }
        private val small = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(107, 114, 128); textSize = 8f }
        private val title = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE; textSize = 18f; typeface = Typeface.DEFAULT_BOLD }
        private val subtitle = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE; textSize = 10f }
        private val sectionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = PRIMARY; textSize = 12f; typeface = Typeface.DEFAULT_BOLD }
        private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(229, 231, 235); strokeWidth = 1f }
        private var pageNumber = 0
        private var page: PdfDocument.Page? = null
        private lateinit var canvas: Canvas
        private var y = MARGIN

        fun header(main: String, ref: String) {
            newPage()
            val paint = Paint().apply { color = PRIMARY }
            canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 82f, paint)
            canvas.drawText(main, MARGIN, 34f, title)
            canvas.drawText(ref, MARGIN, 56f, subtitle)
            y = 112f
        }

        fun section(text: String) {
            ensure(32f)
            canvas.drawText(text, MARGIN, y, sectionPaint)
            y += 8f
            canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, linePaint)
            y += 18f
        }

        fun text(text: String) {
            val lines = wrap(text, 92)
            ensure(lines.size * 15f + 4f)
            lines.forEach {
                canvas.drawText(it, MARGIN, y, normal)
                y += 15f
            }
        }

        fun grid(items: List<Pair<String, String>>) {
            ensure(76f)
            val cellW = (PAGE_WIDTH - MARGIN * 2) / 2
            items.forEachIndexed { index, item ->
                val col = index % 2
                val row = index / 2
                val x = MARGIN + col * cellW
                val top = y + row * 34f
                canvas.drawText(item.first.uppercase(), x, top, small)
                canvas.drawText(item.second.take(36), x, top + 16f, normal)
            }
            y += 78f
        }

        fun barChart(registros: List<RegistroDiario>) {
            ensure(170f)
            val left = MARGIN
            val top = y
            val width = PAGE_WIDTH - MARGIN * 2
            val height = 130f
            canvas.drawRect(RectF(left, top, left + width, top + height), Paint().apply { color = Color.rgb(249, 250, 251) })
            val values = registros.takeLast(14).map { r ->
                val dolor = r.nivelDolor
                val fatiga = r.nivelFatiga
                when {
                    dolor != null && fatiga != null -> (dolor + fatiga) / 2f
                    dolor != null -> dolor.toFloat()
                    fatiga != null -> fatiga.toFloat()
                    else -> 0f
                }
            }
            val maxValue = max(10f, values.maxOrNull() ?: 10f)
            val gap = 4f
            val barW = (width - gap * (values.size + 1)) / values.size.coerceAtLeast(1)
            val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = PRIMARY }
            values.forEachIndexed { index, value ->
                val barH = (value / maxValue) * (height - 28f)
                val x = left + gap + index * (barW + gap)
                canvas.drawRect(x, top + height - barH - 18f, x + barW, top + height - 18f, barPaint)
            }
            canvas.drawText("Dolor/Fatiga promedio por registro", left, top + height + 14f, small)
            y += height + 32f
        }

        fun alertTable(alertas: List<Alerta>) {
            ensure(24f)
            canvas.drawText("Tipo", MARGIN, y, sectionPaint)
            canvas.drawText("Estado", MARGIN + 190f, y, sectionPaint)
            y += 14f
            alertas.take(18).forEach { alerta ->
                ensure(18f)
                canvas.drawText(alerta.tipo.take(28), MARGIN, y, normal)
                canvas.drawText(if (alerta.leida) "Leída" else "Pendiente", MARGIN + 190f, y, normal)
                y += 16f
            }
        }

        fun finish() {
            page?.let {
                footer()
                pdf.finishPage(it)
            }
        }

        private fun newPage() {
            page?.let {
                footer()
                pdf.finishPage(it)
            }
            pageNumber++
            page = pdf.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create())
            canvas = page!!.canvas
            y = MARGIN
        }

        private fun ensure(required: Float) {
            if (y + required > PAGE_HEIGHT - 56f) newPage()
        }

        private fun footer() {
            val fecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
            canvas.drawText("Página $pageNumber - TheraTrack Home - Generado el $fecha", MARGIN, PAGE_HEIGHT - 24f, small)
        }

        private fun wrap(text: String, max: Int): List<String> {
            if (text.length <= max) return listOf(text)
            val result = mutableListOf<String>()
            var current = ""
            text.split(" ").forEach { word ->
                if ((current.length + word.length + 1) > max) {
                    result.add(current)
                    current = word
                } else {
                    current = if (current.isBlank()) word else "$current $word"
                }
            }
            if (current.isNotBlank()) result.add(current)
            return result
        }
    }
}
