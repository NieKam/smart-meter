package com.niekam.smartmeter.fragment.overview

import androidx.lifecycle.LiveData
import com.niekam.smartmeter.base.BaseViewModel
import com.niekam.smartmeter.data.MeterDataModel
import com.niekam.smartmeter.data.model.Meter
import com.niekam.smartmeter.data.model.MeterWithMeasurements
import com.niekam.smartmeter.tools.now
import com.niekam.smartmeter.tools.timestampToDate
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MetersOverviewViewModel(private val dataModel: MeterDataModel) : BaseViewModel() {
    val meters: LiveData<List<MeterWithMeasurements>> = dataModel.getAllMeters()
    val todayDate = now().timestampToDate()

    fun updateMeter(meter: Meter) = coroutineScope.launch {
        withContext(backgroundDispatcher) {
            dataModel.updateMeter(meter)
        }
    }

    fun deleteMeter(obj: Meter) = coroutineScope.launch {
        withContext(backgroundDispatcher) {
            dataModel.deleteMeter(obj)
        }
    }
}
