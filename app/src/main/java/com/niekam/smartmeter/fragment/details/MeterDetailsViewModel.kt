package com.niekam.smartmeter.fragment.details

import androidx.lifecycle.MediatorLiveData
import com.niekam.smartmeter.base.BaseViewModel
import com.niekam.smartmeter.data.MeterDataModel
import com.niekam.smartmeter.data.model.Measurement
import com.niekam.smartmeter.data.model.Meter
import com.niekam.smartmeter.logic.validateNewMeasurement
import com.niekam.smartmeter.logic.validateUpdatedMeasurement
import com.niekam.smartmeter.reminder.NotificationCoordinator
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class MeterDetailsViewModel(
    private val dataModel: MeterDataModel,
    private val notificationCoordinator: NotificationCoordinator
) : BaseViewModel() {
    lateinit var meter: Meter
    val measurements = MediatorLiveData<List<Measurement>>()

    private var dataChanged = false

    fun init(meter: Meter) {
        this.meter = meter
        measurements.addSource(dataModel.getAllMeasurements(meter.uid)) { data ->
            measurements.postValue(data)
            if (dataChanged) {
                rescheduleScheduleNotification(data)
                dataChanged = false
            }
        }
    }

    fun addNewMeasurement(measurement: Measurement) = coroutineScope.launch {
        measurements.value?.let {
            measurement.validateNewMeasurement(it)
        }

        dataChanged = true
        measurement.meterId = meter.uid
        withContext(backgroundDispatcher) {
            dataModel.addMeasurement(measurement)
        }
    }

    fun updateMeasurement(measurement: Measurement) = coroutineScope.launch {
        measurements.value?.let {
            measurement.validateUpdatedMeasurement(it)
        }

        dataChanged = true
        withContext(backgroundDispatcher) {
            dataModel.updateMeasurement(measurement)
        }
    }

    fun delete(measurement: Measurement) = coroutineScope.launch {
        dataChanged = true
        withContext(backgroundDispatcher) {
            dataModel.deleteMeasurement(measurement)
        }
    }

    private fun rescheduleScheduleNotification(measurements: List<Measurement>) {
        if (meter.showNotification) {
            Timber.i("Measurements have been changed, need to reschedule notifications")
            notificationCoordinator.scheduleNotification(meter, measurements)
        }
    }
}
