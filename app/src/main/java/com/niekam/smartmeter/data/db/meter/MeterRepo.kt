package com.niekam.smartmeter.data.db.meter

import androidx.lifecycle.LiveData
import com.niekam.smartmeter.data.model.Measurement
import com.niekam.smartmeter.data.model.Meter
import com.niekam.smartmeter.data.model.MeterWithMeasurements

class MeterRepo(private val meterDao: MeterDao) {

    fun getAll(): LiveData<List<MeterWithMeasurements>> {
        return meterDao.getAllLiveData()
    }

    suspend fun addNewMeter(meter: Meter): Long {
        return meterDao.insertMeter(meter)
    }

    suspend fun addNewMeterWithMeasurement(meter: Meter, measurement: Measurement) {
        return meterDao.insertMeterAndMeasurement(meter, measurement)
    }

    suspend fun update(meter: Meter) {
        meterDao.updateMeter(meter)
    }

    suspend fun deleteMeter(meter: Meter) {
        meterDao.deleteMeter(meter)
    }

    suspend fun getMeterById(meterId: Long): Meter {
        return meterDao.getMeter(meterId)
    }
}