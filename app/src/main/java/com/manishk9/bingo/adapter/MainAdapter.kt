package com.manishk9.bingo.adapter

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.manishk9.bingo.R
import com.manishk9.bingo.model.Node
import com.manishk9.bingo.databinding.ItemBingoBinding
import com.manishk9.bingo.model.Letter
import com.manishk9.bingo.model.NodeSelection


class MainAdapter : ListAdapter<Node, MainAdapter.MainAdapterViewHolder>(Companion) {

    class MainAdapterViewHolder(val binding: ItemBingoBinding) :
        RecyclerView.ViewHolder(binding.root)

    private var itemClickListener: ((Node) -> Unit)? = null

    private var bingo: MutableMap<String, Letter> = mutableMapOf()

    companion object : DiffUtil.ItemCallback<Node>() {
        override fun areItemsTheSame(oldItem: Node, newItem: Node): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Node, newItem: Node): Boolean {
            Log.d("MainAdapter", Thread.currentThread().name)
            return oldItem.equals(newItem)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainAdapterViewHolder {
        return MainAdapterViewHolder(
            ItemBingoBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: MainAdapterViewHolder, position: Int) {
        val item = getItem(position)
        holder.binding.node = item
        holder.itemView.isEnabled = !item.isVisited
        holder.binding.number.isSelected = item.isVisited
        holder.itemView.setOnClickListener {
            itemClickListener?.invoke(item)
        }
        if (item.tag.size > 0)
            holder.binding.number.setBackgroundColor(bingo.get(item.tag.get(item.tag.size - 1))!!.color)
        else if (item.isVisited)
            holder.binding.number.setBackgroundColor(
                if (item.selectedBy == NodeSelection.ME) ContextCompat.getColor(
                    holder.itemView.context,
                    R.color.visted_by_me
                ) else ContextCompat.getColor(
                    holder.itemView.context,
                    R.color.visted_by_opponent
                )
            )
        else
            holder.binding.number.setBackgroundColor(Color.parseColor("#00000000"))

        holder.binding.executePendingBindings()
    }

    fun setItemClickListener(listener: (item: Node) -> Unit) {
        itemClickListener = listener
    }

    fun setBoard(bingo: MutableMap<String, Letter>) {
        this.bingo = bingo
    }
}