package com.niekam.smartmeter.adapter

import android.view.*
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.niekam.smartmeter.R
import com.niekam.smartmeter.data.model.Measurement
import com.niekam.smartmeter.databinding.HistoryItemLayoutBinding
import com.niekam.smartmeter.fragment.history.HistoryItemViewModel

class HistoryAdapter : RecyclerView.Adapter<HistoryAdapter.HistoryItemViewHolder>() {
    var contextMenuCallback: ContextMenuCallback<Measurement>? = null
    var data = listOf<Measurement>()
        set(value) {
            notifyDataSetChanged()
            field = value
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryItemViewHolder {
        val binding: HistoryItemLayoutBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context), R.layout.history_item_layout, parent, false
        )

        return HistoryItemViewHolder(binding, contextMenuCallback)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: HistoryItemViewHolder, position: Int) {
        val itemViewModel = HistoryItemViewModel(data[position])
        holder.apply {
            bind(itemViewModel)
        }
    }

    inner class HistoryItemViewHolder(
        private val binding: HistoryItemLayoutBinding,
        private val callback: ContextMenuCallback<Measurement>?
    ) :
        RecyclerView.ViewHolder(binding.root),
        View.OnCreateContextMenuListener, MenuItem.OnMenuItemClickListener, PopupMenu.OnMenuItemClickListener {

        override fun onCreateContextMenu(menu: ContextMenu?, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
            PopupMenu(v.context, v).apply {
                menuInflater.inflate(R.menu.context_menu, getMenu())
                setOnMenuItemClickListener(this@HistoryItemViewHolder)
            }.also {
                it.show()
            }
        }

        override fun onMenuItemClick(item: MenuItem?): Boolean {
            return when (item?.itemId) {
                R.id.action_edit -> {
                    callback?.onEdit(data[adapterPosition])
                    true
                }
                R.id.action_delete -> {
                    callback?.onDelete(data[adapterPosition])
                    true
                }
                else -> return false
            }
        }

        fun bind(itemViewModel: HistoryItemViewModel) {
            binding.let {
                it.viewModel = itemViewModel
                it.root.setOnCreateContextMenuListener(this)

                val color = if (itemViewModel.measurement.topUpValue > 0.0) {
                    R.color.md_green_700_a30
                } else {
                    android.R.color.transparent
                }

                it.root.setBackgroundColor(ContextCompat.getColor(it.root.context, color))
            }
        }
    }
}