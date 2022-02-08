package com.niekam.smartmeter.fragment.history

import com.niekam.smartmeter.data.model.Measurement
import com.niekam.smartmeter.tools.convertToMoney

data class HistoryItemViewModel(val measurement: Measurement) {
    val topUp = measurement.topUpValue.convertToMoney()
}