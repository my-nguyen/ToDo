package com.florian_walther.todo

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.florian_walther.todo.databinding.ItemTaskBinding

class TaskAdapter(private val listener: OnItemClickListener): ListAdapter<Task, TaskAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(private val binding: ItemTaskBinding): RecyclerView.ViewHolder(binding.root) {

        // set up the OnClickListener's here because init block is called only when a ViewHolder is
        // constructed, and a RecyclerView only creates a number of ViewHolder's.
        // whereas if you set up the OnClickListener's in onBindViewHolder(), which is called every
        // time a new item is scrolled into the screen, which can be tons of calls
        init {
            binding.apply {
                // when user clicks anywhere on the whole task item
                root.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val item = getItem(position)
                        listener.onItemClick(item)
                    }
                }

                // when user clicks only on the checkbox
                cbCompleted.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val item = getItem(position)
                        listener.onCheckBoxClick(item, cbCompleted.isChecked)
                    }
                }
            }
        }

        fun bind(task: Task) {
            binding.apply {
                cbCompleted.isChecked = task.is_completed
                tvName.text = task.name
                tvName.paint.isStrikeThruText = task.is_completed
                ivPriority.isVisible = task.is_important
            }
        }
    }

    interface OnItemClickListener {
        fun onItemClick(task: Task)
        fun onCheckBoxClick(task: Task, isChecked: Boolean)
    }

    class DiffCallback: DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Task, newItem: Task) =
            oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemTaskBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }
}