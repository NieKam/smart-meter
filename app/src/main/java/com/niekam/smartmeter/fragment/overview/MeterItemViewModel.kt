package com.niekam.smartmeter.fragment.overview

import androidx.lifecycle.ViewModel
import com.niekam.smartmeter.data.model.INSUFFICIENT_MEASUREMENTS
import com.niekam.smartmeter.data.model.MeterWithMeasurements
import com.niekam.smartmeter.data.model.NO_BALANCE
import com.niekam.smartmeter.logic.computeRemainingFunds
import com.niekam.smartmeter.logic.computeRemainingTimeMs
import com.niekam.smartmeter.tools.convertToMoney
import com.niekam.smartmeter.tools.formatToCurrencyAndDate

class MeterItemViewModel(val meter: MeterWithMeasurements) : ViewModel() {
    private val remainingFunds = meter.measurements.computeRemainingFunds()
    val remainingTimeMs = meter.measurements.computeRemainingTimeMs()

    val showBalance = remainingFunds != NO_BALANCE
    val showMeasurement = remainingTimeMs != INSUFFICIENT_MEASUREMENTS
    val remainingFundsString = remainingFunds.convertToMoney()
}