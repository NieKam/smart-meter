package com.niekam.smartmeter.fragment.overview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.google.android.gms.ads.AdRequest
import com.niekam.smartmeter.R
import com.niekam.smartmeter.adapter.ContextMenuCallback
import com.niekam.smartmeter.adapter.MeterAdapter
import com.niekam.smartmeter.base.BaseFragment
import com.niekam.smartmeter.data.model.Meter
import com.niekam.smartmeter.data.model.MeterWithMeasurements
import com.niekam.smartmeter.databinding.MetersOverviewFragmentBinding
import com.niekam.smartmeter.reminder.NotificationCoordinator
import com.niekam.smartmeter.tools.ads.getAdPreferences
import org.koin.android.ext.android.inject
import timber.log.Timber


class MetersOverviewFragment : BaseFragment<MetersOverviewViewModel>() {
    override val viewModel: MetersOverviewViewModel by inject()

    private val notificationCoordinator: NotificationCoordinator by inject()
    private val meterAdapter = MeterAdapter()
    private val metersObserver = Observer<List<MeterWithMeasurements>> { setMetersData(it) }
    private val contextMenuCallback = object : ContextMenuCallback<Meter> {
        override fun onEdit(obj: Meter) {
            Timber.i("Edit ${obj.name}")
            showEditDialog(obj)
        }

        override fun onDelete(obj: Meter) {
            viewModel.deleteMeter(obj)
        }
    }

    private var toast: Toast? = null
    private lateinit var binding: MetersOverviewFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.meters_overview_fragment, container, false)
        with(binding) {
            rvMeters.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = meterAdapter.apply {
                    itemClickListener = { meter -> openMeterDetails(meter) }
                    notificationClickListener = { meter -> onNotificationItemClicked(meter) }
                    contextMenuCallback = this@MetersOverviewFragment.contextMenuCallback
                }
                setHasFixedSize(false)
            }
            viewModel = this@MetersOverviewFragment.viewModel
        }

        styleActionBar(showBackButton = false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (requireContext().getAdPreferences().shouldShowAd()) {
            val adRequest = AdRequest.Builder().build()
            binding.adView.apply {
                visibility = View.VISIBLE
                loadAd(adRequest)
            }
        }
    }

    override fun onDestroyView() {
        binding.rvMeters.adapter = null
        super.onDestroyView()
    }

    override fun onStart() {
        super.onStart()
        viewModel.meters.observe(this, metersObserver)
    }

    override fun onStop() {
        super.onStop()
        viewModel.meters.removeObserver(metersObserver)
        toast?.cancel()
    }

    private fun showEditDialog(meter: Meter) {
        MaterialDialog(requireContext()).show {
            input(allowEmpty = false, prefill = meter.name) { _, newName ->
                meter.name = newName.toString()
                viewModel.updateMeter(meter)
            }
            positiveButton(R.string.update)
            negativeButton(R.string.cancel)
        }
    }

    private fun onNotificationItemClicked(meter: MeterWithMeasurements) {
        if (meter.measurements.size < 2) {
            showToast(getString(R.string.cannotDisplayNotification))
            return
        }
        toast?.cancel()

        meter.let {
            it.meter.apply {
                showNotification = !showNotification
            }

            viewModel.updateMeter(it.meter)
            if (it.meter.showNotification) {
                showToast(getString(R.string.notificationConfirmation))
                notificationCoordinator.scheduleNotification(it.meter, it.measurements)
            } else {
                notificationCoordinator.cancelSchedule(it.meter.uid)
            }
        }
    }

    private fun openMeterDetails(meter: Meter) {
        val action = MetersOverviewFragmentDirections.actionOpenDetails(meter = meter)
        this.findNavController().navigate(action)
    }

    private fun setMetersData(meters: List<MeterWithMeasurements>) {
        meterAdapter.data = meters
        with(binding) {
            if (meters.isEmpty()) {
                rvMeters.visibility = View.GONE
                tvNoMeters.visibility = View.VISIBLE
            } else {
                rvMeters.visibility = View.VISIBLE
                tvNoMeters.visibility = View.GONE
            }
        }
    }

    private fun showToast(message: String) {
        toast = Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).also {
            it.show()
        }
    }
}
