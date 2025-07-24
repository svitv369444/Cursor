package com.sewingfactory.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sewingfactory.data.Worker
import com.sewingfactory.databinding.ItemWorkerBinding

class WorkerAdapter(
    private val onWorkerClick: (Worker) -> Unit
) : ListAdapter<Worker, WorkerAdapter.WorkerViewHolder>(WorkerDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkerViewHolder {
        val binding = ItemWorkerBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return WorkerViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: WorkerViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class WorkerViewHolder(
        private val binding: ItemWorkerBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(worker: Worker) {
            binding.apply {
                tvWorkerName.text = worker.fullName
                tvWorkerPosition.text = worker.position
                tvWorkerDepartment.text = worker.department
                tvWorkerSkillLevel.text = "Уровень: ${worker.skillLevel}"
                
                root.setOnClickListener {
                    onWorkerClick(worker)
                }
            }
        }
    }
}

class WorkerDiffCallback : DiffUtil.ItemCallback<Worker>() {
    override fun areItemsTheSame(oldItem: Worker, newItem: Worker): Boolean {
        return oldItem.id == newItem.id
    }
    
    override fun areContentsTheSame(oldItem: Worker, newItem: Worker): Boolean {
        return oldItem == newItem
    }
}