package com.niekam.smartmeter.data

import androidx.lifecycle.LiveData
import com.niekam.smartmeter.data.model.Measurement
import com.niekam.smartmeter.data.model.Meter
import com.niekam.smartmeter.data.model.MeterWithMeasurements

interface MeterDataModel {
    suspend fun addMeter(name: String, balance: Double?)

    suspend fun addMeasurement(measurement: Measurement): Long

    suspend fun updateMeter(meter: Meter)

    suspend fun updateMeasurement(measurement: Measurement)

    suspend fun getMeterById(meterId: Long): Meter

    fun getAllMeters(): LiveData<List<MeterWithMeasurements>>

    fun getAllMeasurements(meterId: Long): LiveData<List<Measurement>>

    suspend fun deleteMeasurement(measurement: Measurement)

    suspend fun deleteMeter(meter: Meter)
}