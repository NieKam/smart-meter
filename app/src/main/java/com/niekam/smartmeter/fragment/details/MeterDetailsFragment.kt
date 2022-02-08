package com.niekam.smartmeter.fragment.details

import android.annotation.SuppressLint
import android.graphics.RectF
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.components.YAxis.AxisDependency
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.MPPointF
import com.jakewharton.rxbinding2.view.RxView
import com.niekam.smartmeter.R
import com.niekam.smartmeter.base.BaseFragment
import com.niekam.smartmeter.data.model.Measurement
import com.niekam.smartmeter.databinding.MeterDetailsFragmentBinding
import com.niekam.smartmeter.dialog.NewMeasurementDialog
import com.niekam.smartmeter.fragment.details.chart.CurrencyValueFormatter
import com.niekam.smartmeter.fragment.details.chart.DayAxisValueFormatter
import com.niekam.smartmeter.fragment.details.chart.XYMarkerView
import com.niekam.smartmeter.logic.computeAverageUsagePerDay
import com.niekam.smartmeter.logic.computeRemainingFunds
import com.niekam.smartmeter.logic.computeRemainingTimeMs
import com.niekam.smartmeter.tools.METER_ARG
import com.niekam.smartmeter.tools.bindRemainingMsToDays
import com.niekam.smartmeter.tools.convertToMoney
import com.niekam.smartmeter.tools.formatToCurrencyAndDate
import org.koin.android.ext.android.inject
import java.util.concurrent.TimeUnit

class MeterDetailsFragment : BaseFragment<MeterDetailsViewModel>(), OnChartValueSelectedListener {
    private lateinit var binding: MeterDetailsFragmentBinding

    private val observer = Observer<List<Measurement>> {
        setMeterDetails(it)
    }

    private val onValueSelectedRectF: RectF by lazy {
        RectF()
    }

    override val viewModel: MeterDetailsViewModel by inject()

    @SuppressLint("CheckResult")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewModel.init(requireNotNull(arguments?.getParcelable(METER_ARG)))
        styleActionBar(title = viewModel.meter.name, showBackButton = true)

        binding = DataBindingUtil.inflate(inflater, R.layout.meter_details_fragment, container, false)
        binding.also {
            it.viewModel = viewModel
            it.lifecycleOwner = this
        }

        RxView.clicks(binding.btnShowHistory).throttleFirst(500, TimeUnit.MILLISECONDS).subscribe { showHistory() }
        RxView.clicks(binding.btnAddMeasurement).throttleFirst(500, TimeUnit.MILLISECONDS).subscribe { newDialog() }
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        viewModel.measurements.observe(this, observer)
    }

    override fun onStop() {
        super.onStop()
        viewModel.measurements.removeObserver(observer)
    }

    private fun newDialog() {
        NewMeasurementDialog(requireView().rootView).show { newMeasurement ->
            viewModel.addNewMeasurement(newMeasurement)
        }
    }

    private fun showHistory() {
        val action =
            MeterDetailsFragmentDirections.actionOpenHistory(meter = requireNotNull(arguments?.getParcelable(METER_ARG)))
        this.findNavController().navigate(action)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupChart(binding.mpChart)
    }

    private fun setMeterDetails(measurements: List<Measurement>) {
        if (measurements.isEmpty()) {
            binding.apply {
                lastMeasureViewsGroup.visibility = View.GONE
                tvNoMeasurements.visibility = View.VISIBLE
            }
            return
        }

        val last = measurements.last()
        binding.apply {
            tvNoMeasurements.visibility = View.GONE
            val funds = measurements.computeRemainingFunds()
            tvFunds.text = funds.convertToMoney()
            val txtColor = if (funds <= 0.0) R.color.md_red_400 else R.color.text_color
            tvFunds.setTextColor(ContextCompat.getColor(requireContext(), txtColor))
            tvLastMeasureDate.text = last.formatToCurrencyAndDate()
            tvLastMeasureDays.timestamp = last.timestamp
            lastMeasureViewsGroup.visibility = View.VISIBLE

            if (measurements.size < 2) {
                tvAddMore.visibility = View.VISIBLE
                tvAverageUsagePerDay.visibility = View.GONE
                tvRemainingTimeDate.visibility = View.GONE
                mpChart.visibility = View.GONE
            } else {
                tvAverageUsagePerDay.text =
                    getString(R.string.averageUsage, measurements.computeAverageUsagePerDay().convertToMoney())
                tvAddMore.visibility = View.GONE
                tvAverageUsagePerDay.visibility = View.VISIBLE
                tvRemainingTimeDate.visibility = View.VISIBLE
                mpChart.visibility = View.VISIBLE
                bindRemainingMsToDays(tvRemainingTimeDate, measurements.computeRemainingTimeMs())
            }
        }
    }

    private fun setupChart(chart: BarChart) {
        chart.apply {
            setNoDataText("")
            setDrawBarShadow(false)
            setDrawValueAboveBar(true)
            setMaxVisibleValueCount(60)
            setDrawGridBackground(false)
            description = Description().apply { text = "" }
            animateY(400)
            setOnChartValueSelectedListener(this@MeterDetailsFragment)
        }

        val xAxisFormatter = DayAxisValueFormatter()
        val currencyFormatter = CurrencyValueFormatter()

        with(chart.legend) {
            verticalAlignment = Legend.LegendVerticalAlignment.TOP
            horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
            orientation = Legend.LegendOrientation.HORIZONTAL
            setDrawInside(false)
            form = Legend.LegendForm.SQUARE
            formSize = 9f
            textSize = 11f
            xEntrySpace = 4f
            textColor = ContextCompat.getColor(requireContext(), R.color.text_color)
        }

        with(chart.xAxis) {
            position = XAxis.XAxisPosition.BOTTOM
            setDrawGridLines(false)
            granularity = 1f
            labelCount = 7
            valueFormatter = xAxisFormatter
            textColor = ContextCompat.getColor(requireContext(), R.color.text_color)
        }

        with(chart.axisLeft) {
            setLabelCount(8, false)
            valueFormatter = currencyFormatter
            setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
            spaceTop = 15f
            textColor = ContextCompat.getColor(requireContext(), R.color.text_color)
            axisMinimum = 0f
        }

        with(chart.axisRight) {
            setDrawLabels(false)
        }

        chart.marker = XYMarkerView(requireContext(), xAxisFormatter).apply {
            chartView = chart
        }
    }

    override fun onNothingSelected() {}

    override fun onValueSelected(e: Entry?, h: Highlight?) {
        if (e == null) {
            return
        }

        val bounds = onValueSelectedRectF

        binding.mpChart.let {
            it.getBarBounds(e as BarEntry, bounds)
            val position = it.getPosition(e, AxisDependency.LEFT)
            MPPointF.recycleInstance(position)
        }
    }
}
