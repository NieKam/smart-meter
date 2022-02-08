package com.niekam.smartmeter.data.db.meter

import androidx.lifecycle.LiveData
import com.niekam.smartmeter.data.model.Measurement


class MeasurementRepo(private val meterDao: MeterDao) {

    fun getAllMeasurements(id: Long): LiveData<List<Measurement>> {
        return meterDao.getAllMeasurements(id)
    }

    suspend fun addNewMeasurement(measurement: Measurement): Long {
        return meterDao.insertMeasurement(measurement)
    }

    suspend fun deleteMeasurement(measurement: Measurement) {
        return meterDao.deleteMeasurement(measurement)
    }

    suspend fun updateMeasurement(measurement: Measurement) {
        return meterDao.updateMeasurement(measurement)
    }
}