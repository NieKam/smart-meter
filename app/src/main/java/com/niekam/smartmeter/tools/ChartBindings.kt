package com.niekam.smartmeter.tools

import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LiveData
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.model.GradientColor
import com.niekam.smartmeter.R
import com.niekam.smartmeter.data.model.Measurement
import com.niekam.smartmeter.fragment.details.chart.DayAxisValueFormatter
import com.niekam.smartmeter.logic.computeAverageUsagePerDay

@BindingAdapter("measurements_data")
fun bindChartData(chart: BarChart, measurementsLiveData: LiveData<List<Measurement>>) {
    val measurements = measurementsLiveData.value
    if (measurements == null || measurements.size < 2) {
        return
    }

    val entries = measurements.drop(1)
        .mapIndexed { i, _ ->
            BarEntry(i.toFloat(), measurements.take(i + 2).computeAverageUsagePerDay().toFloat())
        }

    chart.data.let { data ->
        if (data != null && data.dataSetCount > 0) {
            (data.getDataSetByIndex(0) as BarDataSet).let { set ->
                set.values = entries
                data.notifyDataChanged()
                chart.apply {
                    updateXValuesFormatter(measurements)
                    notifyDataSetChanged()
                    invalidate()
                }
                chart.notifyDataSetChanged()
                chart.invalidate()
            }

            return
        }
    }

    val ctx = chart.context
    val startColors = listOf(
        ContextCompat.getColor(ctx, R.color.md_deep_orange_300),
        ContextCompat.getColor(ctx, R.color.md_blue_300),
        ContextCompat.getColor(ctx, R.color.md_deep_orange_300),
        ContextCompat.getColor(ctx, R.color.md_green_300),
        ContextCompat.getColor(ctx, R.color.md_red_300)
    )

    val endColors = listOf(
        ContextCompat.getColor(ctx, R.color.md_blue_700),
        ContextCompat.getColor(ctx, R.color.md_deep_purple_700),
        ContextCompat.getColor(ctx, R.color.md_green_700),
        ContextCompat.getColor(ctx, R.color.md_red_700),
        ContextCompat.getColor(ctx, R.color.md_deep_orange_700)
    )

    val gradients = startColors.zip(endColors) { startColor, endColor -> GradientColor(startColor, endColor) }

    val set = BarDataSet(entries, ctx.getString(R.string.legend)).apply {
        setDrawIcons(false)
        setValueTextColors(listOf(ContextCompat.getColor(ctx, R.color.text_color)))
        gradientColors = gradients
    }

    chart.apply {
        updateXValuesFormatter(measurements)
        data = BarData(listOf(set)).apply {
            setValueTextSize(10f)
            barWidth = 0.1f
        }
        invalidate()
    }
}

private fun BarChart.updateXValuesFormatter(values: List<Measurement>?) {
    if (this.xAxis.valueFormatter is DayAxisValueFormatter) {
        (this.xAxis.valueFormatter as DayAxisValueFormatter).setXValues(values?.map { it.timestamp })
    }
}