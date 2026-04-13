package com.example.theratrackhome.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.theratrackhome.R
import com.example.theratrackhome.databinding.ItemMedicionBinding
import com.example.theratrackhome.model.Medicion

class MedicionesAdapter(
    private val lista: List<Medicion>
) : RecyclerView.Adapter<MedicionesAdapter.MedicionViewHolder>() {

    inner class MedicionViewHolder(val binding: ItemMedicionBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicionViewHolder {
        val binding = ItemMedicionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MedicionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MedicionViewHolder, position: Int) {
        val item = lista[position]
        val binding = holder.binding

        binding.tvFechaHora.text = item.fechaHora
        binding.tvValor.text = item.valor
        binding.tvEstado.text = item.estado

        when (item.estado.lowercase()) {
            "seguro" -> {
                binding.iconContainer.setBackgroundResource(R.drawable.bg_estado_seguro)
                binding.imgEstado.setImageResource(R.drawable.ic_check_circle)
                binding.imgEstado.setColorFilter(android.graphics.Color.parseColor("#10B981"))
                binding.tvEstado.setTextColor(android.graphics.Color.parseColor("#10B981"))
            }

            "precaución", "precaucion" -> {
                binding.iconContainer.setBackgroundResource(R.drawable.bg_estado_precaucion)
                binding.imgEstado.setImageResource(R.drawable.ic_warning)
                binding.imgEstado.setColorFilter(android.graphics.Color.parseColor("#F59E0B"))
                binding.tvEstado.setTextColor(android.graphics.Color.parseColor("#F59E0B"))
            }

            "peligro" -> {
                binding.iconContainer.setBackgroundResource(R.drawable.bg_estado_peligro)
                binding.imgEstado.setImageResource(R.drawable.ic_warning)
                binding.imgEstado.setColorFilter(android.graphics.Color.parseColor("#EF4444"))
                binding.tvEstado.setTextColor(android.graphics.Color.parseColor("#EF4444"))
            }
        }
    }

    override fun getItemCount(): Int = lista.size
}