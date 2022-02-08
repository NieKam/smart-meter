package com.niekam.smartmeter.adapter

import android.view.*
import androidx.appcompat.widget.PopupMenu
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding2.view.RxView
import com.niekam.smartmeter.R
import com.niekam.smartmeter.data.model.Meter
import com.niekam.smartmeter.data.model.MeterWithMeasurements
import com.niekam.smartmeter.databinding.MeterItemLayoutBinding
import com.niekam.smartmeter.fragment.overview.MeterItemViewModel
import java.util.concurrent.TimeUnit

typealias ClickListener = ((Meter) -> Unit)?
typealias NotificationClickListener = ((MeterWithMeasurements) -> Unit)?

class MeterAdapter : RecyclerView.Adapter<MeterAdapter.MeterItemViewHolder>() {
    var contextMenuCallback: ContextMenuCallback<Meter>? = null
    var itemClickListener: ClickListener = null
    var notificationClickListener: NotificationClickListener = null

    var data = listOf<MeterWithMeasurements>()
        set(value) {
            notifyDataSetChanged()
            field = value
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MeterItemViewHolder {
        val binding: MeterItemLayoutBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context), R.layout.meter_item_layout, parent, false
        )

        return MeterItemViewHolder(binding, contextMenuCallback)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: MeterItemViewHolder, position: Int) {
        val itemViewModel = MeterItemViewModel(data[position])
        holder.apply {
            bind(itemViewModel)
            RxView.clicks(itemView).throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe { itemClickListener?.invoke(data[position].meter) }
            RxView.clicks(binding.ivNotification).throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe { notificationClickListener?.invoke(data[position]) }
        }
    }

    inner class MeterItemViewHolder(
        val binding: MeterItemLayoutBinding,
        private val callback: ContextMenuCallback<Meter>?
    ) :
        RecyclerView.ViewHolder(binding.root),
        View.OnCreateContextMenuListener, MenuItem.OnMenuItemClickListener, PopupMenu.OnMenuItemClickListener {

        override fun onCreateContextMenu(menu: ContextMenu?, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
            PopupMenu(v.context, v).apply {
                menuInflater.inflate(R.menu.context_menu, getMenu())
                setOnMenuItemClickListener(this@MeterItemViewHolder)
            }.also {
                it.show()
            }
        }

        override fun onMenuItemClick(item: MenuItem?): Boolean {
            return when (item?.itemId) {
                R.id.action_edit -> {
                    callback?.onEdit(data[adapterPosition].meter)
                    true
                }
                R.id.action_delete -> {
                    callback?.onDelete(data[adapterPosition].meter)
                    true
                }
                else -> return false
            }
        }

        fun bind(itemViewModel: MeterItemViewModel) {
            binding.let {
                it.viewModel = itemViewModel
                it.root.setOnCreateContextMenuListener(this)
            }
        }
    }
}