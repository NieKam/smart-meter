package com.niekam.smartmeter.fragment.details.chart

import com.github.mikephil.charting.formatter.ValueFormatter
import com.niekam.smartmeter.tools.timestampToDayAndMonth

class DayAxisValueFormatter : ValueFormatter() {
    private var timestamps: List<Long> = mutableListOf()

    override fun getFormattedValue(value: Float): String {
        return timestamps.getOrNull(value.toInt())?.timestampToDayAndMonth() ?: ""
    }

    fun setXValues(timestamps: List<Long>?) {
        timestamps?.let {
            this.timestamps = it
        }
    }
}