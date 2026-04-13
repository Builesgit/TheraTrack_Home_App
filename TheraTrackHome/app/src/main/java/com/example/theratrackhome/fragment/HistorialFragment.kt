package com.example.theratrackhome.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.theratrackhome.R
import com.example.theratrackhome.activity.MainActivity
import com.example.theratrackhome.adapter.MedicionesAdapter
import com.example.theratrackhome.databinding.FragmentHistorialBinding
import com.example.theratrackhome.model.Medicion
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.components.XAxis

class HistorialFragment : Fragment() {

    private var _binding: FragmentHistorialBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistorialBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val listaMediciones = listOf(
            Medicion("12 Marzo, 14:32", "12 µSv/h", "Seguro"),
            Medicion("11 Marzo, 09:15", "34 µSv/h", "Precaución"),
            Medicion("10 Marzo, 18:40", "18 µSv/h", "Seguro")
        )

        binding.recyclerMediciones.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerMediciones.adapter = MedicionesAdapter(listaMediciones)

        setupChart()

        binding.backButton.setOnClickListener {
            (activity as? MainActivity)?.supportFragmentManager
                ?.beginTransaction()
                ?.replace(R.id.fragmentContainer, HomeFragment())
                ?.commit()
        }

        binding.btnHoy.setOnClickListener {
            activarFiltro("hoy")
        }

        binding.btn7Dias.setOnClickListener {
            activarFiltro("7dias")
        }

        binding.btn30Dias.setOnClickListener {
            activarFiltro("30dias")
        }
    }

    private fun activarFiltro(filtro: String) {
        when (filtro) {
            "hoy" -> {
                binding.btnHoy.setBackgroundColor(Color.parseColor("#1D4ED8"))
                binding.btnHoy.setTextColor(Color.WHITE)
                binding.btn7Dias.setBackgroundColor(Color.parseColor("#E5E7EB"))
                binding.btn7Dias.setTextColor(Color.parseColor("#6B7280"))
                binding.btn30Dias.setBackgroundColor(Color.parseColor("#E5E7EB"))
                binding.btn30Dias.setTextColor(Color.parseColor("#6B7280"))
            }

            "7dias" -> {
                binding.btnHoy.setBackgroundColor(Color.parseColor("#E5E7EB"))
                binding.btnHoy.setTextColor(Color.parseColor("#6B7280"))
                binding.btn7Dias.setBackgroundColor(Color.parseColor("#1D4ED8"))
                binding.btn7Dias.setTextColor(Color.WHITE)
                binding.btn30Dias.setBackgroundColor(Color.parseColor("#E5E7EB"))
                binding.btn30Dias.setTextColor(Color.parseColor("#6B7280"))
            }

            "30dias" -> {
                binding.btnHoy.setBackgroundColor(Color.parseColor("#E5E7EB"))
                binding.btnHoy.setTextColor(Color.parseColor("#6B7280"))
                binding.btn7Dias.setBackgroundColor(Color.parseColor("#E5E7EB"))
                binding.btn7Dias.setTextColor(Color.parseColor("#6B7280"))
                binding.btn30Dias.setBackgroundColor(Color.parseColor("#1D4ED8"))
                binding.btn30Dias.setTextColor(Color.WHITE)
            }
        }
    }

    private fun setupChart() {

        val entries = listOf(
            Entry(0f, 12f),
            Entry(1f, 18f),
            Entry(2f, 34f),
            Entry(3f, 20f),
            Entry(4f, 15f),
            Entry(5f, 10f)
        )

        val dataSet = LineDataSet(entries, "")

        dataSet.color = Color.parseColor("#1D4ED8")
        dataSet.lineWidth = 3f

        dataSet.setCircleColor(Color.parseColor("#1D4ED8"))
        dataSet.circleRadius = 4f

        dataSet.setDrawFilled(true)
        dataSet.fillColor = Color.parseColor("#DBEAFE")

        dataSet.setDrawValues(false)

        val lineData = LineData(dataSet)

        binding.chartRadiacion.data = lineData

        val xAxis = binding.chartRadiacion.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)

        binding.chartRadiacion.axisRight.isEnabled = false
        binding.chartRadiacion.axisLeft.setDrawGridLines(false)
        binding.chartRadiacion.description.isEnabled = false
        binding.chartRadiacion.legend.isEnabled = false

        binding.chartRadiacion.invalidate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}