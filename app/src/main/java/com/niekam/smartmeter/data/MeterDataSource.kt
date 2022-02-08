package com.niekam.smartmeter.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.niekam.smartmeter.data.db.meter.MeasurementRepo
import com.niekam.smartmeter.data.db.meter.MeterRepo
import com.niekam.smartmeter.data.model.Measurement
import com.niekam.smartmeter.data.model.Meter
import com.niekam.smartmeter.data.model.MeterWithMeasurements
import com.niekam.smartmeter.tools.now

class MeterDataSource(private val meterRepo: MeterRepo, private val measurementRepo: MeasurementRepo) : MeterDataModel {
    override suspend fun addMeter(name: String, balance: Double?) {
        val meter = Meter(name = name)
        if (balance == null) {
            meterRepo.addNewMeter(meter)
        } else {
            meterRepo.addNewMeterWithMeasurement(
                meter,
                Measurement(timestamp = now(), value = balance, topUpValue = 0.0)
            )
        }
    }

    override suspend fun addMeasurement(measurement: Measurement): Long {
        return measurementRepo.addNewMeasurement(measurement)
    }

    override suspend fun updateMeter(meter: Meter) {
        meterRepo.update(meter)
    }

    override suspend fun updateMeasurement(measurement: Measurement) {
        measurementRepo.updateMeasurement(measurement)
    }

    override suspend fun getMeterById(meterId: Long): Meter {
        return meterRepo.getMeterById(meterId)
    }

    override fun getAllMeters(): LiveData<List<MeterWithMeasurements>> {
        return Transformations.map(meterRepo.getAll()) { all ->
            all.forEach { meter ->
                meter.measurements = meter.measurements.sortedBy { it.timestamp }
            }

            all
        }
    }

    override fun getAllMeasurements(meterId: Long): LiveData<List<Measurement>> =
        measurementRepo.getAllMeasurements(meterId)

    override suspend fun deleteMeasurement(measurement: Measurement) {
        measurementRepo.deleteMeasurement(measurement)
    }

    override suspend fun deleteMeter(meter: Meter) {
        meterRepo.deleteMeter(meter)
    }
}