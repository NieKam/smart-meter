package com.niekam.smartmeter.fragment.details.chart

import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.ValueFormatter
import com.niekam.smartmeter.tools.convertToMoney

class CurrencyValueFormatter : ValueFormatter() {

    override fun getAxisLabel(value: Float, axis: AxisBase?): String {
        return value.toDouble().convertToMoney()
    }
}