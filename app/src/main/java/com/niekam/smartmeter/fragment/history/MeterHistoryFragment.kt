package com.niekam.smartmeter.fragment.history

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding2.view.RxView
import com.niekam.smartmeter.R
import com.niekam.smartmeter.adapter.ContextMenuCallback
import com.niekam.smartmeter.adapter.HistoryAdapter
import com.niekam.smartmeter.base.BaseFragment
import com.niekam.smartmeter.data.model.Measurement
import com.niekam.smartmeter.dialog.NewMeasurementDialog
import com.niekam.smartmeter.fragment.details.MeterDetailsViewModel
import com.niekam.smartmeter.tools.METER_ARG
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.util.concurrent.TimeUnit

class MeterHistoryFragment : BaseFragment<MeterDetailsViewModel>() {
    private lateinit var recyclerView: RecyclerView

    private val historyAdapter = HistoryAdapter()
    override val viewModel: MeterDetailsViewModel by inject()
    private val observer = Observer<List<Measurement>> {
        historyAdapter.data = it
        view?.findViewById<View>(R.id.iv_empty)?.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
    }

    private val contextMenuCallback = object : ContextMenuCallback<Measurement> {
        override fun onEdit(obj: Measurement) {
            Timber.i("Edit ${obj.id}")
            showEditDialog(obj)
        }

        override fun onDelete(obj: Measurement) {
            viewModel.delete(obj)
        }
    }

    @SuppressLint("CheckResult")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewModel.init(requireNotNull(arguments?.getParcelable(METER_ARG)))
        styleActionBar(title = getString(R.string.historyTitle, viewModel.meter.name), showBackButton = true)

        val view = inflater.inflate(R.layout.meter_history_fragment, container, false)
        recyclerView = view.findViewById<RecyclerView>(R.id.rv_measurements).apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = historyAdapter
            setHasFixedSize(true)
            addItemDecoration(DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL))
        }
        historyAdapter.contextMenuCallback = contextMenuCallback

        RxView.clicks(view.findViewById(R.id.fab_add_measurement)).throttleFirst(500, TimeUnit.MILLISECONDS)
            .subscribe { showDialog() }

        return view
    }

    private fun showDialog() {
        NewMeasurementDialog(requireView()).show {
            viewModel.addNewMeasurement(it)
        }
    }

    private fun showEditDialog(measurement: Measurement) {
        NewMeasurementDialog(requireView(), measurement).show {
            viewModel.updateMeasurement(it)
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.measurements.observe(this, observer)
        registerForContextMenu(recyclerView)
    }

    override fun onStop() {
        super.onStop()
        viewModel.measurements.removeObserver(observer)
        unregisterForContextMenu(recyclerView)
    }
}
