package com.tsa.medissa

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.tsa.medissa.databinding.ItemPillTimeBinding

class PillTimeAdapter(
    private val onItemClick: (PillTime) -> Unit
) : ListAdapter<PillTime, PillTimeAdapter.ViewHolder>(PillTimeDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPillTimeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemPillTimeBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }
        }

        fun bind(pillTime: PillTime) {
            binding.apply {
                txtMedicationName.text = pillTime.medicationName
                txtTime.text = pillTime.getFormattedTime()
                txtCompartment.text = "Compartment ${pillTime.compartmentNumber}"
            }
        }
    }

    private class PillTimeDiffCallback : DiffUtil.ItemCallback<PillTime>() {
        override fun areItemsTheSame(oldItem: PillTime, newItem: PillTime): Boolean {
            return oldItem.timestamp == newItem.timestamp
        }

        override fun areContentsTheSame(oldItem: PillTime, newItem: PillTime): Boolean {
            return oldItem == newItem
        }
    }
}